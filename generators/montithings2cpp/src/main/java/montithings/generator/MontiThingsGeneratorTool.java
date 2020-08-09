// (c) https://github.com/MontiCore/monticore
package montithings.generator;

import arcbasis._symboltable.ComponentTypeSymbol;
import bindings.BindingsTool;
import bindings._ast.ASTBindingRule;
import bindings._ast.ASTBindingsCompilationUnit;
import bindings._cocos.BindingsCoCos;
import bindings._parser.BindingsParser;
import bindings._symboltable.BindingsGlobalScope;
import cdlangextension.CDLangExtensionTool;
import cdlangextension._ast.ASTCDLangExtensionUnit;
import cdlangextension._cocos.CDLangExtensionCoCos;
import cdlangextension._parser.CDLangExtensionParser;
import cdlangextension._symboltable.CDLangExtensionGlobalScope;
import cdlangextension._symboltable.CDLangExtensionUnitSymbol;
import cdlangextension._symboltable.ICDLangExtensionScope;
import de.monticore.cd.CD4ACoCos;
import de.monticore.cd.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.cd.cd4analysis._parser.CD4AnalysisParser;
import de.monticore.cd.cd4analysis._symboltable.CD4AnalysisGlobalScope;
import de.monticore.cd.cd4analysis._symboltable.CD4AnalysisGlobalScopeBuilder;
import de.monticore.cd.cd4analysis._symboltable.CD4AnalysisLanguage;
import de.monticore.cd.cd4analysis._symboltable.CD4AnalysisModelLoader;
import de.monticore.io.paths.ModelPath;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Log;
import montiarc.util.Modelfinder;
import montithings.MontiThingsTool;
import montithings._symboltable.IMontiThingsScope;
import montithings._symboltable.MontiThingsGlobalScope;
import montithings.generator.cd2cpp.CppGenerator;
import montithings.generator.codegen.ConfigParams;
import montithings.generator.codegen.xtend.MTGenerator;
import montithings.generator.data.Models;
import montithings.generator.helper.ComponentHelper;
import phyprops.PhypropsTool;
import phyprops._ast.ASTPhypropsUnit;
import phyprops._cocos.PhypropsCoCos;
import phyprops._parser.PhypropsParser;
import phyprops._symboltable.PhypropsGlobalScope;
import phyprops._symboltable.PhypropsLanguage;
import phyprops._symboltable.PhypropsModelLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static montithings.generator.helper.FileHelper.copyHwcToTarget;
import static montithings.generator.helper.FileHelper.getSubPackagesPath;

public class MontiThingsGeneratorTool extends MontiThingsTool {

  private static final String LIBRARY_MODELS_FOLDER = "target/librarymodels/";

  private static final String TOOL_NAME = "MontiThingsGeneratorTool";

  public void generate(File modelPath, File target, File hwcPath, ConfigParams config) {

    /* ============================================================ */
    /* ==================== Copy HWC to target ==================== */
    /* ============================================================ */
    copyHwcToTarget(target, hwcPath, config);

    /* ============================================================ */
    /* ======================== Find Models ======================= */
    /* ============================================================ */
    Models models = new Models(modelPath);

    /* ============================================================ */
    /* ===================== Set up Symbol Tabs =================== */
    /* ============================================================ */
    Log.info("Initializing symboltable", TOOL_NAME);

    CD4AnalysisGlobalScope cdSymTab = new CD4AnalysisGlobalScopeBuilder()
      .setModelPath(new ModelPath((Paths.get(modelPath.getAbsolutePath()))))
      .setCD4AnalysisLanguage(new CD4AnalysisLanguage())
      .build();

    this.setCdGlobalScope(cdSymTab);
    IMontiThingsScope symTab = initSymbolTable(modelPath,
        //Paths.get(basedir + LIBRARY_MODELS_FOLDER).toFile(),
        hwcPath);

    CDLangExtensionTool cdExtensionTool = new CDLangExtensionTool();
    cdExtensionTool.setCdGlobalScope(cdSymTab);

    BindingsTool bindingsTool = new BindingsTool();
    bindingsTool.setMtGlobalScope((MontiThingsGlobalScope) symTab);
    BindingsGlobalScope binTab = bindingsTool.initSymbolTable(modelPath);

    PhypropsTool phypropsTool = new PhypropsTool();
    phypropsTool.setMtGlobalScope((MontiThingsGlobalScope) symTab);


    /* ============================================================ */
    /* ====================== Check Models ======================== */
    /* ============================================================ */
    Log.info("Checking models", TOOL_NAME);

    checkCds(models.getClassdiagrams(), cdSymTab);
    checkMtModels(models.getMontithings(), symTab);
    checkCdExtensionModels(models.getCdextensions(), modelPath, config, cdExtensionTool);
    checkBindings(models.getBindings(), config, bindingsTool, binTab);
    checkPhyprops(models.getPhyprops(),  phypropsTool.initSymbolTable(modelPath));

    /* ============================================================ */
    /* ====================== Generate Code ======================= */
    /* ============================================================ */

    for (String model : models.getMontithings()) {
      generateCppForComponent(model, symTab, target, hwcPath, config);
      generateCMakeForComponent(model, symTab, modelPath, target, hwcPath, config);
    }
    generateCDEAdapter(target, config);
    generateCD(modelPath, target);
  }

  /* ============================================================ */
  /* ====================== Check Models ======================== */
  /* ============================================================ */

  protected void checkMtModels(List<String> foundModels, IMontiThingsScope symTab) {
    for (String model : foundModels) {
      String qualifiedModelName = Names.getQualifier(model) + "." + Names.getSimpleName(model);

      // parse + resolve model
      Log.info("Parsing model:" + qualifiedModelName, TOOL_NAME);
      ComponentTypeSymbol comp = symTab.resolveComponentType(qualifiedModelName).get();

      // check cocos
      Log.info("Check model: " + qualifiedModelName, TOOL_NAME);
      checkCoCos(comp.getAstNode());
    }
  }

  protected void checkCds(List<String> foundModels, CD4AnalysisGlobalScope symTab) {
    for (String model : foundModels) {
      ASTCDCompilationUnit cdAST = null;
      try {
        cdAST = new CD4AnalysisParser().parseCDCompilationUnit(model)
          .orElseThrow(() -> new NullPointerException("0xMT1111 Failed to parse: " + model));
      }
      catch (IOException e) {
        Log.error("File '" + model + "' CD4A artifact was not found");
      }

      // parse + resolve model
      Log.info("Parsing model:" + model, "MontiThingsGeneratorTool");
      new CD4AnalysisModelLoader(new CD4AnalysisLanguage())
        .createSymbolTableFromAST(cdAST, model, symTab);

      // check cocos
      Log.info("Check model: " + model, "MontiThingsGeneratorTool");
      new CD4ACoCos().getCheckerForAllCoCos().checkAll(cdAST);
    }
  }

  protected void checkCdExtensionModels(List<String> foundCDExtensionModels, File modelPath,
    ConfigParams config, CDLangExtensionTool cdExtensionTool) {
    for (String model : foundCDExtensionModels) {
      ASTCDLangExtensionUnit cdExtensionAST = null;
      try {
        cdExtensionAST = new CDLangExtensionParser().parseCDLangExtensionUnit(model)
          .orElseThrow(() -> new NullPointerException("0xMT1111 Failed to parse: " + model));
      }
      catch (IOException e) {
        Log.error("File '" + model + "' CDLangExtension artifact was not found");
      }

      // parse + resolve model
      Log.info("Parsing model:" + model, "MontiThingsGeneratorTool");
      if (config.getCdLangExtensionScope() == null) {
        config
          .setCdLangExtensionScope(cdExtensionTool.createSymboltable(cdExtensionAST, modelPath));
      }
      else {
        cdExtensionTool.createSymboltable(cdExtensionAST,
          (CDLangExtensionGlobalScope) config.getCdLangExtensionScope());
      }

      // check cocos
      Log.info("Check model: " + model, "MontiThingsGeneratorTool");
      new CDLangExtensionCoCos().createChecker().checkAll(cdExtensionAST);
    }
  }

  protected void checkBindings(List<String> foundBindings, ConfigParams config,
    BindingsTool bindingsTool, BindingsGlobalScope binTab) {
    for (String binding : foundBindings) {
      ASTBindingsCompilationUnit bindingsAST = null;
      try {
        bindingsAST = new BindingsParser().parseBindingsCompilationUnit(binding)
          .orElseThrow(() -> new NullPointerException("0xMT1111 Failed to parse: " + binding));
      }
      catch (IOException e) {
        Log.error("File '" + binding + "' Bindings artifact was not found");
      }
      Log.info("Parsing model:" + binding, TOOL_NAME);
      bindingsTool.createSymboltable(bindingsAST, binTab);

      Log.info("Check Binding: " + binding, "MontiArcGeneratorTool");
      BindingsCoCos.createChecker().checkAll(bindingsAST);

      for (bindings._ast.ASTElement rule : bindingsAST.getElementList()) {
        if (rule instanceof ASTBindingRule) {
          config.getComponentBindings().add((ASTBindingRule) rule);
        }
      }
    }
  }

  protected void checkPhyprops(List<String> foundModels, PhypropsGlobalScope symTab) {
    for (String model : foundModels) {
      ASTPhypropsUnit ast = null;
      try {
        ast = new PhypropsParser().parsePhypropsUnit(model)
            .orElseThrow(() -> new NullPointerException("0xMT1111 Failed to parse: " + model));
      }
      catch (IOException e) {
        Log.error("File '" + model + "' Phyprops artifact was not found");
      }

      // parse + resolve model
      Log.info("Parsing model:" + model, "MontiThingsGeneratorTool");
      new PhypropsModelLoader(new PhypropsLanguage())
          .createSymbolTableFromAST(ast, model, symTab);

      // check cocos
      Log.info("Check model: " + model, "MontiThingsGeneratorTool");
      new PhypropsCoCos().createChecker().checkAll(ast);
    }
  }

  /* ============================================================ */
  /* ===================== Generate Code ======================== */
  /* ============================================================ */

  protected void generateCppForComponent(String model, IMontiThingsScope symTab, File target,
    File hwcPath, ConfigParams config) {
    String qualifiedModelName = Names.getQualifier(model) + "." + Names.getSimpleName(model);
    ComponentTypeSymbol comp = symTab.resolveComponentType(qualifiedModelName).get();
    Log.info("Generate model: " + qualifiedModelName, TOOL_NAME);

    // check if component is implementation
    if (config.isImplementation(comp)) {
      // Dont generate files for implementation. They are generated when interface is there
      return;
    }

    String compname = comp.getName();

    // Check if component is interface
    Optional<ComponentTypeSymbol> implementation = config.getBinding(comp);
    if (implementation.isPresent()) {
      compname = implementation.get().getName();
    }

    // Generate Files
    MTGenerator.generateAll(
      Paths.get(target.getAbsolutePath(), Names.getPathFromPackage(comp.getPackageName()))
        .toFile(), hwcPath, comp, compname, config);
  }

  protected void generateCMakeForComponent(String model, IMontiThingsScope symTab, File modelPath,
    File target, File hwcPath, ConfigParams config) {
    String qualifiedModelName = Names.getQualifier(model) + "." + Names.getSimpleName(model);
    ComponentTypeSymbol comp = symTab.resolveComponentType(qualifiedModelName).get();

    if (ComponentHelper.isApplication(comp)) {
      File libraryPath = Paths.get(target.getAbsolutePath(), "montithings-RTE").toFile();
      // Check for Subpackages
      File[] subPackagesPath = getSubPackagesPath(modelPath.getAbsolutePath());

      // 6 generate make file
      if (config.getTargetPlatform()
        != ConfigParams.TargetPlatform.ARDUINO) { // Arduino uses its own build system
        Log.info("Generate CMake file", "MontiThingsGeneratorTool");
        MTGenerator.generateMakeFile(target, comp, hwcPath, libraryPath,
          subPackagesPath, config);
      }
    }
  }

  protected void generateCD(File modelPath, File targetFilepath) {
    List<String> foundModels = Modelfinder
      .getModelsInModelPath(modelPath, CD4AnalysisLanguage.FILE_ENDING);
    for (String model : foundModels) {
      String simpleName = Names.getSimpleName(model);
      String packageName = Names.getQualifier(model);

      Path outDir = Paths.get(targetFilepath.getAbsolutePath());
      new CppGenerator(outDir, Paths.get(modelPath.getAbsolutePath()), model,
        Names.getQualifiedName(packageName, simpleName)).generate();
    }
  }

  protected void generateCDEAdapter(File targetFilepath, ConfigParams config) {
    if(config.getCdLangExtensionScope() != null) {
      for(ICDLangExtensionScope subScope:config.getCdLangExtensionScope().getSubScopes()) {
        for (CDLangExtensionUnitSymbol unit : subScope.getCDLangExtensionUnitSymbols().values()) {
          String simpleName = unit.getAstNode().getName();
          String packageName = Names.getQualifiedName(unit.getAstNode().getPackageList());

          MTGenerator.generateAdapter(Paths.get(targetFilepath.getAbsolutePath(), Names.getPathFromPackage(packageName)).toFile(), simpleName, config);
        }
      }
    }
  }

}
