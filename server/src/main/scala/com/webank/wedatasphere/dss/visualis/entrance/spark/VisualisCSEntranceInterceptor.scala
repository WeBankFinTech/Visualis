package com.webank.wedatasphere.dss.visualis.entrance.spark

import java.lang

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.entrance.cs.CSEntranceHelper
import org.apache.linkis.entrance.interceptor.impl.CSEntranceInterceptor
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.entity.task.RequestPersistTask
import org.apache.linkis.protocol.task.Task

class VisualisCSEntranceInterceptor extends CSEntranceInterceptor {

  override def apply(task: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    task match {
      case jobRequest : JobRequest =>
        logger.info("Start to execute CSEntranceInterceptor")
        Utils.tryAndWarn(CSEntranceHelper.addCSVariable(jobRequest))
        //Utils.tryAndWarn(CSEntranceHelper.resetCreator(requestPersistTask))
        Utils.tryAndWarn(CSEntranceHelper.initNodeCSInfo(jobRequest))
        logger.info("Finished to execute CSEntranceInterceptor")
      case _ =>
    }
    task
  }

}
