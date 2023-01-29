${tc.signature("packageName", "compname", "batchesOfNamesOfInputPorts", "batchesOfNamesOfOutputPorts")}
#include "${compname}Impl.h"
#include <iostream>

namespace montithings {
    namespace ${packageName} {
        ${compname}Result
        ${compname}Impl::getInitialValues()
        {
          return {};
        }

        ${compname}Result
        ${compname}Impl::compute(${compname}Input input)
        {
          ${compname}Result result;

          <#list 0..batchesOfNamesOfInputPorts?size-1 as i>
          std::vector<float> inputs_${i}(${batchesOfNamesOfInputPorts[i]?size});

          <#list batchesOfNamesOfInputPorts[i] as nameOfInputPort>
          if (input.get${nameOfInputPort?cap_first}())
          {
          float ${nameOfInputPort} = (float)input.get${nameOfInputPort?cap_first}().value();
          inputs_${i}.push_back(${nameOfInputPort});
          }
          </#list>

          state.add_past_value_${i}(inputs_${i});

          std::vector<bool> is_anomaly_${i} = this->ad->is_anomaly(inputs_${i}, state.get_past_values_${i}());

          <#list 0..batchesOfNamesOfOutputPorts[i]?size-1 as j>
          if (!is_anomaly_${i}[${j}])
          {
            result.set${batchesOfNamesOfOutputPorts[i][j]?cap_first}(input.get${batchesOfNamesOfInputPorts[i][j]?cap_first}().value());
          }
          else
          {
            std::cout << "${compname}: Input is anomaly. Block sending." << std::endl;
          }
          </#list>
          </#list>

          return result;
        }
    } // namespace ${packageName}
} // namespace montithings