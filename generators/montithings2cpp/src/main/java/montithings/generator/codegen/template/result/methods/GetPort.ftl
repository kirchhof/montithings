<#-- (c) https://github.com/MontiCore/monticore -->
${tc.signature("port", "comp", "config", "existsHWC")}
<#include "/template/result/helper/GeneralPreamble.ftl">

<#assign name = port.getName()>
<#assign type = ComponentHelper.getRealPortCppTypeString(comp, port, config)>
<#assign typeWrapped = tc.includeArgs("template.logtracing.hooks.ReplaceTypeIfEnabled", [comp, config, type])>

${Utils.printTemplateArguments(comp)}
tl::optional<${type}>
${className}${Utils.printFormalTypeParameters(comp, false)}::get${name?cap_first}() const
{
return ${name};
}

${tc.includeArgs("template.logtracing.hooks.GetPortWrappedDefinition", [comp, config, port])}