package de.montiarcautomaton.cocos;

import java.util.ArrayList;
import java.util.List;

import de.monticore.symboltable.ImportStatement;
import de.se_rwth.commons.logging.Log;
import montiarc._ast.ASTComponent;
import montiarc._cocos.MontiArcASTComponentCoCo;
import montiarc._symboltable.ComponentSymbol;

public class NoJavaImportsForCPPGenerator implements MontiArcASTComponentCoCo {

	/*
	 * Checks that no Java imports are in components that are generated with the CPP
	 * generator.
	 */
	@Override
	public void check(ASTComponent node) {
		if (!node.getSymbolOpt().isPresent()) {
			Log.error(String.format("0xMA010 ASTComponent node \"%s\" has no " + "symbol. Did you forget to run the "
					+ "SymbolTableCreator before checking cocos?", node.getName()));
			return;
		}
		ComponentSymbol symbol = (ComponentSymbol) node.getSymbolOpt().get();
		
		//For some reason java.lang and java.util are always automatically imported, so we only count 
		//additional imports
		if (symbol.getImports().size() > 2) {
			Log.error(String.format(
					"0xMA301 Components generated by the CPP generator " + "should not contain Java import statements.",
					node.getName()));
		}

	}
}