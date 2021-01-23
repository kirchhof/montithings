<#-- (c) https://github.com/MontiCore/monticore -->
${tc.signature("comp","config")}
<#assign ComponentHelper = tc.instantiate("montithings.generator.helper.ComponentHelper")>

<#list comp.getIncomingPorts() as p>
    // incoming port ${p.getName()}
    MqttPort<${ComponentHelper.getRealPortCppTypeString(p.getComponent().get(), p, config)}> *${p.getName()} = new MqttPort<${ComponentHelper.getRealPortCppTypeString(p.getComponent().get(), p, config)}>(this->getInstanceName () + "/${p.getName()}");
    ${p.getName()}->subscribe(this->getInstanceName () + "/${p.getName()}");
    getPort${p.getName()?cap_first} ()->attach (this);
    this->addInPort${p.getName()?cap_first} (${p.getName()});

    <#if !comp.isAtomic()>
      // additional outgoing port for port incoming port ${p.getName()}
      // to forward data to subcomponents
      this->addOutPort${p.getName()?cap_first}(new MqttPort<${ComponentHelper.getRealPortCppTypeString(p.getComponent().get(), p, config)}>(this->getInstanceName () + "/${p.getName()}", false));
    </#if>
</#list>
