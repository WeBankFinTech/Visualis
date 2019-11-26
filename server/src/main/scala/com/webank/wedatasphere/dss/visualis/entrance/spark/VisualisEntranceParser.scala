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

import com.webank.wedatasphere.linkis.entrance.parser.CommonEntranceParser
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.protocol.task.Task
import com.webank.wedatasphere.linkis.scheduler.queue.Job
import org.springframework.stereotype.Component
/**
  * Created by shanhuang on 2019/1/23.
  */
@Component("entranceParser")
class VisualisEntranceParser extends CommonEntranceParser {

   override def parseToJob(task: Task): Job = {
      task match {
         case requestPersistTask:RequestPersistTask => val job = new VisualisJob
            job.setTask(task)
            job.setUser(requestPersistTask.getUmUser)
            job.setCreator(requestPersistTask.getRequestApplicationName)
            job.setEntranceListenerBus(getEntranceContext.getOrCreateEventListenerBus)
            job.setProgress(0.0f)
            job
         case _ => null
      }
   }

}
