// (c) https://github.com/MontiCore/monticore
#include <iostream>
#include "SourceImpl.h"

namespace montithings {
namespace hierarchy {

SourceResult SourceImpl::getInitialValues(){
    lastValue = 0;
	return {lastValue};
}

SourceResult SourceImpl::compute(SourceInput input){
  std::cout << "Source: " << lastValue << std::endl;
	return {lastValue++};
}

}}