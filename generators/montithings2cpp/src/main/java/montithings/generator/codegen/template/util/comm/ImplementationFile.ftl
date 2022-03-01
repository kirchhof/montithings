<#-- (c) https://github.com/MontiCore/monticore -->
${tc.signature("comp", "config", "existsHWC")}
<#include "/template/Preamble.ftl">
<#include "/template/util/comm/helper/GeneralPreamble.ftl">
<#include "/template/Copyright.ftl">

#include "${className}.h"
#include "messages/PortToSocket.h"
<#if splittingModeIsLocal>
  #include "json/json.hpp"
  #include ${"<fstream>"}
</#if>

${Utils.printNamespaceStart(comp)}

<#if splittingModeIsLocal>
  using json = nlohmann::json;
</#if>

${tc.includeArgs("template.util.comm.methods.Constructor", [comp, config, existsHWC])}
${tc.includeArgs("template.util.comm.methods.Process", [comp, config, existsHWC])}
${tc.includeArgs("template.util.comm.methods.InitializePorts", [comp, config, existsHWC])}
${tc.includeArgs("template.util.comm.methods.SearchForSubComps", [comp, config, existsHWC])}

${Utils.printNamespaceEnd(comp)}