package com.webank.wedatasphere.dss.visualis.entrance.spark

import org.apache.linkis.entrance.parser.CommonEntranceParser
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.entity.task.RequestPersistTask
import org.apache.linkis.protocol.task.Task
import org.apache.linkis.scheduler.queue.Job
import org.springframework.stereotype.Component

@Component("entranceParser")
class VisualisEntranceParser(persistenceManager : VisualisPersistenceManager) extends CommonEntranceParser(persistenceManager) {

   override def parseToJob(task: JobRequest): Job = {
      task match {
         case requestPersistTask:RequestPersistTask => val job = new VisualisJob(persistenceManager)
            job.setJobRequest(task)
            job.setUser(requestPersistTask.getUmUser)
            job.setCreator(requestPersistTask.getRequestApplicationName)
            job.setParams(task.asInstanceOf[RequestPersistTask].getParams.asInstanceOf[java.util.Map[String, AnyRef]])
            job.setEntranceListenerBus(getEntranceContext.getOrCreateEventListenerBus)
            job.setProgress(0.0f)
            job
         case _ => null
      }
   }

}
