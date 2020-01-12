// (c) https://github.com/MontiCore/monticore
/* generated from model MontiThings */
/* generated by template ast.AstInterface*/



package montithings._ast;


/* generated by template ast.ASTNodeBase*/

import de.monticore.ast.ASTNode;
import montiarc._ast.ASTMontiArcNode;
import montithings._visitor.MontiThingsVisitor;

/**
 * Interface for all AST nodes of the MontiThings language.
 */
public interface ASTMontiThingsNode extends ASTMontiArcNode {

  public void accept(MontiThingsVisitor visitor);

}


