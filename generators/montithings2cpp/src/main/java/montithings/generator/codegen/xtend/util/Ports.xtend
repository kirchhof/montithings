// (c) https://github.com/MontiCore/monticore
package montithings.generator.codegen.xtend.util

import java.util.Collection
import montiarc._symboltable.PortSymbol
import montithings.generator.helper.ComponentHelper
import montiarc._symboltable.ComponentSymbol
import montithings._symboltable.ResourcePortSymbol
import montithings.generator.codegen.xtend.util.Utils

class Ports {
	
	def static printVars(Collection<PortSymbol> ports) {
	return	'''
    «FOR port : ports»
    «var type = ComponentHelper.getRealPortCppTypeString(port.component.get, port)»
    «var name = port.name»
    
   Port<«type»>* «name» = new Port<«type»>;
    
    «ENDFOR»
    '''
		
	}
	
	def static printResourcePortVars(Collection<ResourcePortSymbol> ports) {
	return	'''
«FOR port : ports»
«var type = ComponentHelper.getResourcePortType(port)»
«var name = port.name»
«IF (port.ipc && !port.outgoing)»
Port<«type»>* «name» = new IncomingIPCPort<«type»>("«port.uri»");
«ENDIF»
«IF (port.ipc && port.outgoing)»
OutgoingIPCPort<«type»>* «name» = new OutgoingIPCPort<«type»>("«port.uri»");
«ENDIF»
«IF (port.webSocket && !port.outgoing)»
Port<«type»>* «name» = new IncomingWSPort<«type»>("«port.uri»");
«ENDIF»
«IF (port.webSocket && port.outgoing)»
OutgoingWSPort<«type»>* «name» = new OutgoingWSPort<«type»>("«port.uri»");
«ENDIF»
«ENDFOR»
    '''
		
	}
	
	def static printResourcePortMethodHeaders(Collection<ResourcePortSymbol> ports){
	return	'''
    «FOR port : ports»
    «var type = ComponentHelper.getResourcePortType(port)»
    «var name = port.name»
    «IF port.incoming»
    Port<«type»>* getPort«name.toFirstUpper»();
    «ENDIF»
    void setPort«name.toFirstUpper»(Port<«type»>* «name»);
    «ENDFOR»
    '''
	}
	
		def static printResourcePortMethodBodies(Collection<ResourcePortSymbol> ports, ComponentSymbol comp, String compname){
	return	'''
    «FOR port : ports»
    «var type = ComponentHelper.getResourcePortType(port)»
    «var name = port.name»
    «IF port.incoming»
    «Utils.printTemplateArguments(comp)»
    Port<«type»>* «compname»«Utils.printFormalTypeParameters(comp)»::getPort«name.toFirstUpper»(){
    	return «name»;
    }

    «Utils.printTemplateArguments(comp)»
    void «compname»«Utils.printFormalTypeParameters(comp)»::setPort«name.toFirstUpper»(Port<«type»>* port){
    	«name» = port;
    }
    «ELSE»
    «Utils.printTemplateArguments(comp)»
    void «compname»«Utils.printFormalTypeParameters(comp)»::setPort«name.toFirstUpper»(Port<«type»>* port){
    	«name»->setPort(port);
    }
    «ENDIF»
    «ENDFOR»
    '''
	}
	
	def static printMethodHeaders(Collection<PortSymbol> ports){
	return	'''
    «FOR port : ports»
    «var type = ComponentHelper.getRealPortCppTypeString(port.component.get, port)»
    «var name = port.name»
    Port<«type»>* getPort«name.toFirstUpper»();
    void setPort«name.toFirstUpper»(Port<«type»>* «name»);
    «ENDFOR»
    '''
	}
	
	def static printMethodBodies(Collection<PortSymbol> ports, ComponentSymbol comp, String compname){
	return	'''
    «FOR port : ports»
    «var type = ComponentHelper.getRealPortCppTypeString(port.component.get, port)»
    «var name = port.name»
    «Utils.printTemplateArguments(comp)»
    Port<«type»>* «compname»«Utils.printFormalTypeParameters(comp)»::getPort«name.toFirstUpper»(){
    	return «name»;
    }

    «Utils.printTemplateArguments(comp)»
    void «compname»«Utils.printFormalTypeParameters(comp)»::setPort«name.toFirstUpper»(Port<«type»>* port){
    	«IF comp.atomic»
    	port->registerPort(uuid);
    	«ENDIF»
    	«name» = port;
    }
    
    «ENDFOR»
    '''
	}
	

	
	
	
}