package montiarc.cocos;

import de.se_rwth.commons.logging.Log;
import montiarc._ast.ASTAlternative;
import montiarc._cocos.MontiArcASTAlternativeCoCo;

/**
 * Context condition that forbids the usage of multiple alternatives.
 *
 * @author Gerrit Leonhardt, Andreas Wortmann
 */
public class UseOfAlternatives implements MontiArcASTAlternativeCoCo {
  
  @Override
  public void check(ASTAlternative node) {
    if (node.getValueLists().size() > 1) {
      Log.error("0xMA062 Multiple alternatives are not supported.", node.get_SourcePositionStart());
    }
  }
}
