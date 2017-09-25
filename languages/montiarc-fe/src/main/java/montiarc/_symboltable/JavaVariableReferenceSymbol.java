/*
 * Copyright (c) 2017 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package montiarc._symboltable;

import de.monticore.symboltable.CommonSymbol;
import de.monticore.symboltable.SymbolKind;
import de.monticore.symboltable.Symbols;

public class JavaVariableReferenceSymbol extends CommonSymbol{

  public static final JavaVariableReferenceKind KIND = new JavaVariableReferenceKind();

  
  /**
   * Constructor for de.monticore.lang.montiarc.ajava._symboltable.JavaExpressionVariableSymbol
   * @param name
   * @param kind
   */
  public JavaVariableReferenceSymbol(String name) {
    super(name, KIND);
  }
  
}
