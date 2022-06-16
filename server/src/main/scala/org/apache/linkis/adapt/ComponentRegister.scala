package org.apache.linkis.adapt

import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component

@Component
@ComponentScan(basePackages = Array("edp", "com.webank.wedatasphere.dss"))
class ComponentRegister {

}
