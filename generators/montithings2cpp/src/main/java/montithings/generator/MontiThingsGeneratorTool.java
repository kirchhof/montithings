// (c) https://github.com/MontiCore/monticore
package montithings.generator;

import arcbasis._symboltable.ComponentTypeSymbol;
import arcbasis._symboltable.PortSymbol;
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
import montithings._ast.ASTMTComponentType;
import montithings._symboltable.IMontiThingsScope;
import montithings._symboltable.MontiThingsGlobalScope;
import montithings.cocos.PortConnection;
import montithings.generator.cd2cpp.CppGenerator;
import montithings.generator.codegen.ConfigParams;
import montithings.generator.codegen.MTGenerator;
import montithings.generator.data.Models;
import montithings.generator.helper.ComponentHelper;
import montithings.generator.helper.GeneratorHelper;
import montithings.generator.visitor.FindTemplatedPortsVisitor;
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
import java.util.stream.Collectors;

import static montithings.generator.helper.FileHelper.*;

public class MontiThingsGeneratorTool extends MontiThingsTool {

  protected static final String LIBRARY_MODELS_FOLDER = "target/librarymodels/";

  protected static final String TOOL_NAME = "MontiThingsGeneratorTool";

  public void generate(File modelPath, File target, File hwcPath, File testPath,
    ConfigParams config) {

    //Log.initWARN();

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

    for (String model : models.getMontithings()) {
      // Parse model
      String qualifiedModelName = Names.getQualifier(model) + "." + Names.getSimpleName(model);
      Log.info("Parsing model: " + qualifiedModelName, TOOL_NAME);
      ComponentTypeSymbol comp = symTab.resolveComponentType(qualifiedModelName).get();

      // Find ports with templates
      FindTemplatedPortsVisitor vistor = new FindTemplatedPortsVisitor(config.getHwcTemplatePath());
      comp.getAstNode().accept(vistor);
      config.getTemplatedPorts().addAll(vistor.getTemplatedPorts());
    }


    /* ============================================================ */
    /* ====================== Check Models ======================== */
    /* ============================================================ */
    Log.info("Checking models", TOOL_NAME);

    checkCds(models.getClassdiagrams(), cdSymTab);
    checkMtModels(models.getMontithings(), symTab, config);
    checkCdExtensionModels(models.getCdextensions(), modelPath, config, cdExtensionTool);
    checkBindings(models.getBindings(), config, bindingsTool, binTab);
    checkPhyprops(models.getPhyprops(), phypropsTool.initSymbolTable(modelPath));

    /* ============================================================ */
    /* ====================== Generate Code ======================= */
    /* ============================================================ */

    for (String model : models.getMontithings()) {
      File compTarget = target;

      if (config.getSplittingMode() != ConfigParams.SplittingMode.OFF) {
        compTarget = Paths.get(target.getAbsolutePath(), model).toFile();
        generateCppForSubcomponents(model, modelPath, models.getMontithings(), symTab,
          compTarget, hwcPath, config);
        MTGenerator.generateMakeFileForSubdirs(target, models.getMontithings());

        if (config.getSplittingMode() == ConfigParams.SplittingMode.LOCAL) {
          ComponentTypeSymbol comp = modelToSymbol(model, symTab);
          MTGenerator.generatePortJson(compTarget, comp, config);
        }
      }
      if (config.getMessageBroker() == ConfigParams.MessageBroker.DDS) {
        MTGenerator.generateDDSDCPSConfig(compTarget, config);
      }
      
      generateCppForComponent(model, symTab, compTarget, hwcPath, config);
      generateCMakeForComponent(model, symTab, modelPath, compTarget, hwcPath, config, models);
    }
    generateCDEAdapter(target, config);
    generateCD(modelPath, target);
    MTGenerator.generateBuildScript(target, config);

    if (testPath != null && !testPath.toString().equals("")) {
      if (config.getSplittingMode() != ConfigParams.SplittingMode.OFF) {
        config.setSplittingMode(ConfigParams.SplittingMode.OFF);
        generate(modelPath, Paths
          .get(Paths.get(target.getAbsolutePath()).getParent().toString(), "generated-test-sources")
          .toFile(), hwcPath, testPath, config);
      }
      else {
        for (String model : models.getMontithings()) {
          ComponentTypeSymbol comp = modelToSymbol(model, symTab);
          if (ComponentHelper.isApplication(comp)) {
            generateTests(modelPath, testPath, target, hwcPath, comp, config);
          }
        }
      }
    }
  }

  /* ============================================================ */
  /* ====================== Check Models ======================== */
  /* ============================================================ */

  protected void checkMtModels(List<String> foundModels, IMontiThingsScope symTab, ConfigParams config) {
    for (String model : foundModels) {
      String qualifiedModelName = Names.getQualifier(model) + "." + Names.getSimpleName(model);

      // parse + resolve model
      ComponentTypeSymbol comp = symTab.resolveComponentType(qualifiedModelName).get();

      // check cocos
      Log.info("Check model: " + qualifiedModelName, TOOL_NAME);
      checker.addCoCo(new PortConnection(config.getTemplatedPorts()));
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
      Log.info("Parsing model: " + model, "MontiThingsGeneratorTool");
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
      Log.info("Parsing model: " + model, "MontiThingsGeneratorTool");
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
      Log.info("Parsing model: " + binding, TOOL_NAME);
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
      Log.info("Parsing model: " + model, "MontiThingsGeneratorTool");
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
    generateCppForComponent(model, symTab, target, hwcPath, config, true);
  }

  protected void generateCppForComponent(String model, IMontiThingsScope symTab, File target,
    File hwcPath, ConfigParams config, boolean generateDeploy) {
    ComponentTypeSymbol comp = modelToSymbol(model, symTab);
    Log.info("Generate model: " + comp.getFullName(), TOOL_NAME);

    // check if component is implementation
    if (comp.getAstNode() instanceof ASTMTComponentType &&
      ((ASTMTComponentType) comp.getAstNode()).getMTComponentModifier().isInterface()) {
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
        .toFile(), hwcPath, comp, compname, config, generateDeploy);

    generateHwcPort(target, config, comp);

    if (config.getSplittingMode() != ConfigParams.SplittingMode.OFF) {
      copyHwcToTarget(target, hwcPath, model, config);
    }
  }

  protected void generateCppForSubcomponents(String model, File modelPath, List<String> mtModels,
    IMontiThingsScope symTab, File target, File hwcPath, ConfigParams config) {
    // Find subcomponent types
    ComponentTypeSymbol comp = modelToSymbol(model, symTab);
    List<ComponentTypeSymbol> subcomponentTypes = comp.getSubComponents().stream()
      .map(c -> c.getType().getLoadedSymbol()).collect(Collectors.toList());

    // Generate code for each subcomponent type
    for (ComponentTypeSymbol subcomp : subcomponentTypes) {
      generateCppForComponent(subcomp.getFullName(), symTab, target, hwcPath, config, false);
      generateCppForSubcomponents(subcomp.getFullName(), modelPath, mtModels, symTab, target,
        hwcPath, config);
    }
  }

  protected void generateCMakeForComponent(String model, IMontiThingsScope symTab, File modelPath,
    File target, File hwcPath, ConfigParams config, Models models) {
    ComponentTypeSymbol comp = modelToSymbol(model, symTab);

    if (ComponentHelper.isApplication(comp)
      || config.getSplittingMode() != ConfigParams.SplittingMode.OFF) {
      File libraryPath = Paths.get(target.getAbsolutePath(), "montithings-RTE").toFile();
      // Check for Subpackages
      File[] subPackagesPath = getSubPackagesPath(modelPath.getAbsolutePath());

      // 6 generate make file
      if (config.getTargetPlatform()
        != ConfigParams.TargetPlatform.ARDUINO) { // Arduino uses its own build system
        Log.info("Generate CMake file", "MontiThingsGeneratorTool");
        MTGenerator.generateMakeFile(target, comp, hwcPath, libraryPath,
          subPackagesPath, config);
        if (config.getSplittingMode() != ConfigParams.SplittingMode.OFF) {
          MTGenerator.generateScripts(target, comp, config, models.getMontithings());
        }
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
    if (config.getCdLangExtensionScope() != null) {
      for (ICDLangExtensionScope subScope : config.getCdLangExtensionScope().getSubScopes()) {
        for (CDLangExtensionUnitSymbol unit : subScope.getCDLangExtensionUnitSymbols().values()) {
          String simpleName = unit.getAstNode().getName();
          List<String> packageName = unit.getAstNode().getPackageList();

          MTGenerator.generateAdapter(Paths.get(targetFilepath.getAbsolutePath(),
            Names.getPathFromPackage(Names.getQualifiedName(packageName))).toFile(), packageName,
            simpleName, config);
        }
      }
    }
  }

  protected void generateTests(File modelPath, File testFilepath, File targetFilepath, File hwcPath,
    ComponentTypeSymbol comp, ConfigParams config) {
    if (testFilepath != null && targetFilepath != null && comp != null) {
      /* ============================================================ */
      /* ====== Copy generated-sources to generated-test-sources ==== */
      /* ============================================================ */
      copyGeneratedToTarget(targetFilepath);
      copyTestToTarget(testFilepath, targetFilepath, comp);
      if (ComponentHelper.isApplication(comp)) {
        Path target = Paths.get(Paths.get(targetFilepath.getAbsolutePath()).getParent().toString(),
          "generated-test-sources");
        File libraryPath = Paths.get(target.toString(), "montithings-RTE").toFile();
        // Check for Subpackages
        File[] subPackagesPath = getSubPackagesPath(modelPath.getAbsolutePath());

        // 6 generate make file
        if (config.getTargetPlatform()
          != ConfigParams.TargetPlatform.ARDUINO) { // Arduino uses its own build system
          MTGenerator.generateTestMakeFile(target.toFile(), comp, hwcPath, libraryPath,
            subPackagesPath, config);
        }
      }
    }
  }

  public void generateHwcPort(File target, ConfigParams config, ComponentTypeSymbol comp) {
    for(PortSymbol port : comp.getPorts()) {
      if(config.getTemplatedPorts().contains(port)) {
        Optional<String> portType = GeneratorHelper.getPortHwcTemplateName(port, config.getHwcTemplatePath());
        if (portType.isPresent()) {
          MTGenerator.generateAdditionalPort(config.getHwcTemplatePath(), new File(target + File.separator + "hwc" + File.separator + Names.getPathFromPackage(Names.getQualifier(portType.get()))), portType.get());
        }
      }
    }
  }

  protected ComponentTypeSymbol modelToSymbol(String model, IMontiThingsScope symTab) {
    String qualifiedModelName = Names.getQualifier(model) + "." + Names.getSimpleName(model);
    return symTab.resolveComponentType(qualifiedModelName).get();
  }
}
