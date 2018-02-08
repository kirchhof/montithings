package montiarc.cocos;

import de.monticore.ast.ASTNode;
import de.monticore.types.TypesPrinter;
import de.se_rwth.commons.StringTransformations;
import de.se_rwth.commons.logging.Log;
import montiarc._ast.*;
import montiarc._cocos.MontiArcASTComponentCoCo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @implements [Wor16] AU1: The name of each state is unique. (p. 97. Lst. 5.8)
 * @implements [Wor16] AU3: The names of all inputs, outputs, and variables are unique. (p. 98. Lst. 5.10)
 */
public class IdentifiersAreUnique implements MontiArcASTComponentCoCo {

  @Override
  public void check(ASTComponent node) {
    HashSet<String> names = new HashSet<>();

    for (ASTElement e : node.getBody().getElements()) {

      // Check variable declarations
      if (e instanceof ASTVariableDeclaration) {
        ASTVariableDeclaration decl = (ASTVariableDeclaration) e;
        for (String variableName : decl.getNames()) {
          checkList(names, variableName, "0xMA035", "variable", e);
        }
      }

      // Check port names
      else if (e instanceof ASTInterface) {
        ASTInterface decl = (ASTInterface) e;
        for (ASTPort port : decl.getPorts()) {
          List<String> portInstanceNames = port.getNames();
          if (portInstanceNames.isEmpty()) {
            String implicitName = TypesPrinter.printType(port.getType());
            portInstanceNames.add(StringTransformations.uncapitalize(implicitName));
          }
          for (String name : portInstanceNames) {
            checkList(names, name, "0xMA053", "port", e);
          }
        }
      }

      // Check constraints
      else if (e instanceof ASTMontiArcInvariant) {
        ASTMontiArcInvariant invariant = (ASTMontiArcInvariant) e;
        String name = invariant.getName();
        checkList(names, name, "0xMA052", "invariant", e);
      }
    }

    // Configuration Parameters
    List<ASTParameter> parameters = node.getHead().getParameters();
    for (ASTParameter parameter : parameters) {
      checkList(names, parameter.getName(), "0xMA069", "configuration parameter", parameter);
    }

  }

  //TODO: Parameters

  public void checkList(HashSet<String> names, String name, String errorCode, String type,
                        ASTNode e) {
    if (names.contains(name)) {
      Log.error(String.format("%s The name of %s '%s' is already used.", errorCode, type, name),
          e.get_SourcePositionStart());
    } else {
      names.add(name);
    }
  }
}
