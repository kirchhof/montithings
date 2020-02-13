/* (c) https://github.com/MontiCore/monticore */
package montithings._ast;

import de.monticore.mcexpressions._ast.ASTExpression;
import montiarc._symboltable.PortSymbol;
import montithings.helper.ExpressionUtil;

import java.util.List;

/**
 * TODO
 *
 * @author (last commit) kirchhof
 * @version , 13.02.2020
 * @since
 */
public class ASTGuarantee extends ASTGuaranteeTOP {
  public List<PortSymbol> getPortsInGuardExpression() {
    return ExpressionUtil.getPortsInGuardExpression(getGuard());
  }

  @Override public String toString() {
    return ExpressionUtil.printExpression(this.getGuard());
  }

  /* ============================================================ */
  /* ====================== GENERATED CODE ====================== */
  /* ============================================================ */

  public ASTGuarantee() {
  }

  public ASTGuarantee(ASTExpression guard) {
    super(guard);
  }
}
