/*
 * Copyright (c) 2018 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package de.montiarcautomaton.generator.codegen.xtend.composed

import de.montiarcautomaton.generator.codegen.xtend.util.ConfigurationParameters
import de.montiarcautomaton.generator.codegen.xtend.util.Generics
import de.montiarcautomaton.generator.codegen.xtend.util.Getter
import de.montiarcautomaton.generator.codegen.xtend.util.Imports
import de.montiarcautomaton.generator.codegen.xtend.util.Init
import de.montiarcautomaton.generator.codegen.xtend.util.Member
import de.montiarcautomaton.generator.codegen.xtend.util.Setter
import de.montiarcautomaton.generator.codegen.xtend.util.Setup
import de.montiarcautomaton.generator.codegen.xtend.util.Update
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
class ComposedComponent {
  

  def static generateComposedComponent(ComponentSymbol comp) {
    var String generics = Generics.print(comp)
    var helper = new ComponentHelper(comp);
    return '''
    package «comp.packageName»;
    
    «Imports.print(comp)»    
    import de.montiarcautomaton.runtimes.timesync.delegation.IComponent;
    import de.montiarcautomaton.runtimes.timesync.delegation.Port;
    
    public class «comp.name»«generics»
    «IF comp.superComponent.present» extends «comp.superComponent.get.fullName»«ENDIF»
    implements IComponent {

      //ports
      «FOR port : comp.ports»
        «Member.print("Port<" + helper.printPortType(port)+">", port.name, "protected")»
        
        «Getter.print("Port<" + helper.printPortType(port) + ">", port.name, "Port" + port.name.toFirstUpper)»
        «Setter.print("Port<" + helper.printPortType(port) + ">", port.name, "Port" + port.name.toFirstUpper)»      
        
      «ENDFOR»   
      
      
      // config parameters
      «FOR param : comp.configParameters»
        «Member.print(helper.printParamTypeName(param), param.name, "private final")»
      «ENDFOR»
      
      // subcomponents
      «FOR subcomp : comp.subComponents»
        «Member.print(helper.getSubComponentTypeName(subcomp), subcomp.name, "private")»
        
        «Getter.print(helper.getSubComponentTypeName(subcomp), subcomp.name, "Component" + subcomp.name.toFirstUpper)»
      «ENDFOR»
      
      public «comp.name»(«ConfigurationParameters.print(comp)») {
        «IF comp.superComponent.present»
          super();
        «ENDIF»
        «FOR param : comp.configParameters»
          this.«param.name» = «param.name»;
        «ENDFOR»
      }
      
      «Init.print(comp)»
      «Setup.print(comp)»
      «Update.print(comp)»
      
      
      @Override
      public void compute() {
      // trigger computation in all subcomponent instances
        «FOR subcomponent : comp.subComponents»
          this.«subcomponent.name».compute();
        «ENDFOR»
      }
    
    }
    
    '''
  }
}