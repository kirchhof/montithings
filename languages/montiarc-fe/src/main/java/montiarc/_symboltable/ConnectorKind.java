/* generated from model null*/
/* generated by template symboltable.SymbolKind*/

package montiarc._symboltable;

import de.monticore.symboltable.SymbolKind;

public class ConnectorKind implements SymbolKind {

  private static final String NAME = ConnectorKind.class.getName();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isKindOf(SymbolKind kind) {
    return NAME.equals(kind.getName()) || SymbolKind.super.isKindOf(kind);
  }

}