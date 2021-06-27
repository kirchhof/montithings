// (c) https://github.com/MontiCore/monticore
package de.monticore.lang.sd4componenttesting._cocos;

import de.monticore.lang.sd4componenttesting._ast.ASTSD4CConnection;
import de.monticore.lang.sd4componenttesting.util.SD4ComponentTestingError;
import de.se_rwth.commons.logging.Log;

public class SD4CConnectionValid implements SD4ComponentTestingASTSD4CConnectionCoCo {
  @Override
  public void check(ASTSD4CConnection node) {
    //Case 1:  ->  : VALUE;
    if (!node.isPresentSource() && node.getTargetList().isEmpty()) {
      Log.error(String.format(SD4ComponentTestingError.CONNECTION_NOT_VALID.toString(), node));
    }

    if (node.getTargetList().size() != node.getValueList().size()) {
      if (node.getValueList().size() != 1) {
        Log.error(String.format(SD4ComponentTestingError.CONNECTION_NOT_VALID_WRONG_VALUE_AMOUNT.toString(), node));
      }
    }
  }
}
