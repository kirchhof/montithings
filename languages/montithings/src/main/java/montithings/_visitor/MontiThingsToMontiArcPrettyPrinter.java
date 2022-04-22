// (c) https://github.com/MontiCore/monticore
package montithings._visitor;

import arcbasis._ast.ASTArcBasisNode;
import com.google.common.base.Preconditions;
import de.monticore.expressions.commonexpressions._ast.ASTBracketExpression;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.scbasis._ast.ASTStatechart;
import de.monticore.statements.mcexceptionstatements._ast.ASTCatchClause;
import genericarc._ast.ASTGenericArcNode;
import genericarc._ast.ASTGenericComponentHead;
import genericarc._visitor.GenericArcHandler;
import montithings._ast.*;
import org.codehaus.commons.nullanalysis.NotNull;
import prepostcondition._ast.ASTConstantsPrePostCondition;
import prepostcondition._ast.ASTPostcondition;
import prepostcondition._ast.ASTPostconditionBuilder;
import prepostcondition._ast.ASTPrePostConditionNode;

import java.util.Iterator;
import java.util.List;

public class MontiThingsToMontiArcPrettyPrinter implements MontiThingsHandler {

  protected MontiThingsTraverser traverser;

  protected IndentPrinter printer;

  public MontiThingsToMontiArcPrettyPrinter() {
    this.printer = new IndentPrinter();
  }

  public MontiThingsToMontiArcPrettyPrinter(@NotNull IndentPrinter printer) {
    Preconditions.checkArgument(printer != null);
    this.printer = printer;
  }

  @Override
  public MontiThingsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(@NotNull MontiThingsTraverser traverser) {
    Preconditions.checkArgument(traverser != null);
    this.traverser = traverser;
  }

  public IndentPrinter getPrinter() {
    return this.printer;
  }

  public <T extends ASTArcBasisNode> void acceptSeperatedList(@NotNull List<T> list) {
    if (list.isEmpty()) {
      return;
    }
    Iterator<T> iterator = list.iterator();
    iterator.next().accept(getTraverser());
    while (iterator.hasNext()) {
      this.getPrinter().print(", ");
      iterator.next().accept(getTraverser());
    }
  }

  @Override
  public void handle(@NotNull ASTMTComponentType node) {
    node.getMTComponentModifier().accept(getTraverser());
    this.getPrinter().print(node.getName());
    node.getHead().accept(getTraverser());
    acceptSeperatedList(node.getComponentInstanceList());
    node.getBody().accept(getTraverser());
  }

  @Override
  public void handle(@NotNull ASTMTComponentModifier node) {
    //MontiArc does not support interface components
    this.getPrinter().print("component ");
  }

  @Override
  public void handle(@NotNull ASTBehavior node) {
    // intentionally left empty - not covered by MontiArc
  }
  
  @Override
  public void handle(@NotNull ASTInitBehavior node) {
    // intentionally left empty - not covered by MontiArc
  }
  
  @Override
  public void handle(@NotNull ASTArcStatechart node) {
    // intentionally left empty - not covered by MontiArc
  }
  
  @Override
  public void handle(@NotNull ASTIsPresentExpression node) {
    // intentionally left empty - not covered by MontiArc
  }

  @Override public void handle(ASTPublishPort node) {
    // intentionally left empty - not covered by MontiArc
  }
  
  @Override
  public void handle(ASTMTEveryBlock node) {
    // intentionally left empty - not covered by MontiArc
  }
  
}