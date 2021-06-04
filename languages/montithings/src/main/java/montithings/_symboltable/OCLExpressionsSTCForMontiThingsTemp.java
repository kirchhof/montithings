// (c) https://github.com/MontiCore/monticore
package montithings._symboltable;

import de.monticore.ocl.oclexpressions._symboltable.IOCLExpressionsScope;

import java.util.Deque;

public class OCLExpressionsSTCForMontiThingsTemp extends OCLExpressionsSymbolTableCreator {
  public OCLExpressionsSTCForMontiThingsTemp(Deque<? extends IOCLExpressionsScope> scopeStack) {
    super(scopeStack);
  }

  public montithings._symboltable.IMontiThingsScope createScope(boolean shadowing) {
    montithings._symboltable.IMontiThingsScope scope = montithings.MontiThingsMill.scope();
    scope.setShadowing(shadowing);
    return scope;
  }
}
