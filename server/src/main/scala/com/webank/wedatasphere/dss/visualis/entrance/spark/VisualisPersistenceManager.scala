/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.webank.wedatasphere.dss.visualis.entrance.spark

import com.webank.wedatasphere.linkis.entrance.persistence.{PersistenceEngine, QueryPersistenceManager, ResultSetEngine}
import com.webank.wedatasphere.linkis.scheduler.executer.OutputExecuteResponse
import com.webank.wedatasphere.linkis.scheduler.queue.Job
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
  * Created by shanhuang on 2019/1/23.
  */
@Component("persistenceManager")
class VisualisPersistenceManager extends QueryPersistenceManager {

  @Autowired
  var pEngine: PersistenceEngine = _
  @Autowired
  var rEngine: ResultSetEngine = _

  @PostConstruct
  def  init() {
    this.setPersistenceEngine(pEngine)
    this.setResultSetEngine(rEngine)
  }
  override def onResultSetCreated(job: Job, response: OutputExecuteResponse): Unit = {
    job match {
      case j: VisualisJob => j.addResultSet(response.getOutput)
      case _ =>
    }
    super.onResultSetCreated(job, response)
  }

}
