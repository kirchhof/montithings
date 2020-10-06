${tc.signature("comp","compname","config")}
<#assign Utils = tc.instantiate("montithings.generator.codegen.util.Utils")>
${Utils.printTemplateArguments(comp)}
void ${compname}${Utils.printFormalTypeParameters(comp)}::compute(){
if (shouldCompute()) {

${tc.includeArgs("template.componentGenerator.printComputeInputs", [comp, compname, false])}
<#list comp.incomingPorts as port>
<#-- ${ValueCheck.printPortValuecheck(comp, port)} -->
</#list>
${tc.includeArgs("template.componentGenerator.printPreconditionsCheck", [comp, compname])}

<#if config.getSplittingMode().toString() == "OFF">
    <#list comp.subComponents as subcomponent >
        this->${subcomponent.getName()}.compute();
    </#list>
</#if>

${tc.includeArgs("template.componentGenerator.printComputeResults", [comp, compname, true])}
<#list comp.getOutgoingPorts() as port>
<#-- ${ValueCheck.printPortValuecheck(comp, port)} -->
</#list>
${tc.includeArgs("template.componentGenerator.printPostconditionsCheck", [comp, compname])}
}
}