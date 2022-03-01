<#-- (c) https://github.com/MontiCore/monticore -->
${tc.signature("comp", "className")}
<#include "/template/state/helper/GeneralPreamble.ftl">
<#include "/template/Preamble.ftl">

${Utils.printTemplateArguments(comp)}
void ${className}${generics}::setup (<#if brokerIsMQTT>MqttClient* passedMqttClientInstance</#if>)
{
<#if brokerIsMQTT>
  std::string instanceNameTopic = replaceDotsBySlashes (this->instanceName);
  mqttClientInstance = passedMqttClientInstance;
  mqttClientInstance->addUser (this);
  mqttClientInstance->subscribe ("/state/" + instanceNameTopic);
  mqttClientInstance->subscribe ("/replayFinished/" + instanceNameTopic);
</#if>
<#list ComponentHelper.getArcFieldVariables(comp) as var>
    <#assign varName = var.getName()>
    <#assign type = TypesPrinter.printCPPTypeName(var.getType(), comp, config)>
    <#if ComponentHelper.hasAgoQualification(comp, var)>
      dequeOf__${varName?cap_first}.push_back(std::make_pair(std::chrono::system_clock::now(), ${Utils.getInitialValue(var)}));
    </#if>
</#list>
}