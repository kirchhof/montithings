// (c) https://github.com/MontiCore/monticore
package montithings.types.check;

import behavior.types.check.DeriveSymTypeOfBehavior;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.literals.mccommonliterals._ast.ASTSignedLiteral;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLExpressions;
import de.monticore.ocl.types.check.DeriveSymTypeOfSetExpressions;
import de.monticore.types.check.*;
import montithings._visitor.MontiThingsDelegatorVisitor;
import types.check.DeriveSymTypeOfSetDefinitions;

import java.util.Optional;

public class DeriveSymTypeOfMontiThingsCombine extends MontiThingsDelegatorVisitor implements ITypesCalculator {

  private MontiThingsDelegatorVisitor realThis;

  private DeriveSymTypeOfAssignmentExpressionsForMT deriveSymTypeOfAssignmentExpressions;

  private DeriveSymTypeOfCommonExpressionsForMT deriveSymTypeOfCommonExpressions;

  private DeriveSymTypeOfOCLExpressions deriveSymTypeOfOCLExpressions;

  private DeriveSymTypeOfExpressionForMT deriveSymTypeOfExpression;

  private DeriveSymTypeOfLiterals deriveSymTypeOfLiterals;

  private DeriveSymTypeOfSIUnitLiterals deriveSymTypeOfSIUnitLiterals;

  private DeriveSymTypeOfMCCommonLiterals deriveSymTypeOfMCCommonLiterals;

  private DeriveSymTypeOfMontiThings deriveSymTypeOfMontiThings;

  private DeriveSymTypeOfSetDefinitions deriveSymTypeOfSetDefinitions;

  private DeriveSymTypeOfSetExpressions deriveSymTypeOfSetExpressions;

  private DeriveSymTypeOfBehavior deriveSymTypeOfBehavior;

  private TypeCheckResult typeCheckResult = new TypeCheckResult();


  public DeriveSymTypeOfMontiThingsCombine() {
    this.realThis = this;
    init();
  }

  /**
   * main method to calculate the type of an expression
   */
  public Optional<SymTypeExpression> calculateType(ASTExpression e) {
    e.accept(realThis);
    Optional<SymTypeExpression> result = Optional.empty();
    if (typeCheckResult.isPresentCurrentResult()) {
      result = Optional.ofNullable(typeCheckResult.getCurrentResult());
    }
    typeCheckResult.setCurrentResultAbsent();
    return result;
  }

  @Override
  public MontiThingsDelegatorVisitor getRealThis() {
    return realThis;
  }

  /**
   * set the last result of all calculators to the same object
   */
  public void setTypeCheckResult(TypeCheckResult typeCheckResult) {
    deriveSymTypeOfAssignmentExpressions.setTypeCheckResult(typeCheckResult);
    deriveSymTypeOfMCCommonLiterals.setTypeCheckResult(typeCheckResult);
    deriveSymTypeOfCommonExpressions.setTypeCheckResult(typeCheckResult);
    deriveSymTypeOfExpression.setTypeCheckResult(typeCheckResult);
    deriveSymTypeOfLiterals.setTypeCheckResult(typeCheckResult);
    deriveSymTypeOfSIUnitLiterals.setTypeCheckResult(typeCheckResult);
    deriveSymTypeOfMontiThings.setTypeCheckResult(typeCheckResult);
    deriveSymTypeOfOCLExpressions.setTypeCheckResult(typeCheckResult);
    deriveSymTypeOfSetDefinitions.setTypeCheckResult(typeCheckResult);
    deriveSymTypeOfSetExpressions.setTypeCheckResult(typeCheckResult);
    deriveSymTypeOfBehavior.setTypeCheckResult(typeCheckResult);
  }

  /**
   * initialize the typescalculator
   */
  @Override
  public void init() {
    deriveSymTypeOfCommonExpressions = new DeriveSymTypeOfCommonExpressionsForMT();
    deriveSymTypeOfAssignmentExpressions = new DeriveSymTypeOfAssignmentExpressionsForMT();
    deriveSymTypeOfMCCommonLiterals = new DeriveSymTypeOfMCCommonLiterals();
    deriveSymTypeOfExpression = new DeriveSymTypeOfExpressionForMT();
    deriveSymTypeOfLiterals = new DeriveSymTypeOfLiterals();
    deriveSymTypeOfSIUnitLiterals = new DeriveSymTypeOfSIUnitLiterals();
    deriveSymTypeOfMontiThings = new DeriveSymTypeOfMontiThings();
    deriveSymTypeOfOCLExpressions = new DeriveSymTypeOfOCLExpressions();
    deriveSymTypeOfSetDefinitions = new DeriveSymTypeOfSetDefinitions();
    deriveSymTypeOfSetExpressions = new DeriveSymTypeOfSetExpressions();
    deriveSymTypeOfBehavior = new DeriveSymTypeOfBehavior();

    setCommonExpressionsVisitor(deriveSymTypeOfCommonExpressions);
    setAssignmentExpressionsVisitor(deriveSymTypeOfAssignmentExpressions);
    setExpressionsBasisVisitor(deriveSymTypeOfExpression);
    setMCLiteralsBasisVisitor(deriveSymTypeOfLiterals);
    setMCCommonLiteralsVisitor(deriveSymTypeOfMCCommonLiterals);
    setSIUnitLiteralsVisitor(deriveSymTypeOfSIUnitLiterals);
    setMontiThingsVisitor(deriveSymTypeOfMontiThings);
    setOCLExpressionsVisitor(deriveSymTypeOfOCLExpressions);
    setSetDefinitionsVisitor(deriveSymTypeOfSetDefinitions);
    setSetExpressionsVisitor(deriveSymTypeOfSetExpressions);
    setBehaviorVisitor(deriveSymTypeOfBehavior);

    setTypeCheckResult(typeCheckResult);
  }

  /**
   * main method to calculate the type of a literal
   */
  @Override
  public Optional<SymTypeExpression> calculateType(ASTLiteral lit) {
    lit.accept(realThis);
    Optional<SymTypeExpression> result = Optional.empty();
    if (typeCheckResult.isPresentCurrentResult()) {
      result = Optional.ofNullable(typeCheckResult.getCurrentResult());
    }
    typeCheckResult.setCurrentResultAbsent();
    return result;
  }

  /**
   * main method to calculate the type of a signed literal
   */
  @Override
  public Optional<SymTypeExpression> calculateType(ASTSignedLiteral lit) {
    lit.accept(realThis);
    Optional<SymTypeExpression> result = Optional.empty();
    if (typeCheckResult.isPresentCurrentResult()) {
      result = Optional.ofNullable(typeCheckResult.getCurrentResult());
    }
    typeCheckResult.setCurrentResultAbsent();
    return result;
  }

  public TypeCheckResult getTypeCheckResult() {
    return typeCheckResult;
  }
}
