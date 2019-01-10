/*
 * Copyright (c) 2018 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package de.montiarcautomaton.generator.codegen.xtend

import de.montiarcautomaton.generator.codegen.xtend.util.Imports
import de.montiarcautomaton.generator.codegen.xtend.util.Member
import de.montiarcautomaton.generator.codegen.xtend.util.TypeParameters
import de.montiarcautomaton.generator.helper.ComponentHelper
import montiarc._symboltable.ComponentSymbol

/**
 * TODO: Write me!
 *
 * @author  (last commit) $Author$
 * @version $Revision$,
 *          $Date$
 * @since   TODO: add version number
 *
 */
class Input {
    def static generateInput(ComponentSymbol comp) {
    var ComponentHelper helper = new ComponentHelper(comp)
    
    return '''
      package «comp.packageName»;
      
      «Imports.print(comp)»
      import de.montiarcautomaton.runtimes.timesync.implementation.IInput;
      
      
      public class «comp.name»Input«TypeParameters.printFormalTypeParameters(comp)»
      «IF comp.superComponent.present» extends 
            «comp.superComponent.get.fullName»Input
            «IF comp.superComponent.get.hasFormalTypeParameters»<
            «FOR scTypeParams : helper.superCompActualTypeArguments SEPARATOR ','»
                «scTypeParams»
                «ENDFOR» > «ENDIF»
            «ENDIF»
      implements IInput 
       {
        
        «FOR port : comp.incomingPorts»
          «Member.print(helper.getRealPortTypeString(port), port.name, "protected")»
        «ENDFOR»
        
        public «comp.name»Input() {
          «IF comp.superComponent.isPresent»
            super();
          «ENDIF»
        }
        
        «IF !comp.allIncomingPorts.empty»
          public «comp.name»Input(«FOR port : comp.allIncomingPorts SEPARATOR ','» «helper.getRealPortTypeString(port)» «port.name» «ENDFOR») {
            «IF comp.superComponent.present»
              super(«FOR port : comp.superComponent.get.allIncomingPorts» «port.name» «ENDFOR»);
            «ENDIF»
            «FOR port : comp.incomingPorts»
              this.«port.name» = «port.name»; 
            «ENDFOR»
            
          }
        «ENDIF»
        
      «FOR port : comp.incomingPorts»
        public «helper.getRealPortTypeString(port)» get«port.name.toFirstUpper»() {
          return this.«port.name»;
        }
      «ENDFOR»
      
      @Override
      public String toString() {
        String result = "[";
        «FOR port : comp.incomingPorts»
          result += "«port.name»: " + this.«port.name» + " ";
        «ENDFOR»
        return result + "]";
      }  
        
      } 
      
    '''
  }
}