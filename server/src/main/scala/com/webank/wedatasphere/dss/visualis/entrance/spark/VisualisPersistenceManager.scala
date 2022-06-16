package com.webank.wedatasphere.dss.visualis.entrance.spark

import org.apache.linkis.entrance.persistence.{PersistenceEngine, QueryPersistenceManager, ResultSetEngine}
import org.apache.linkis.scheduler.executer.OutputExecuteResponse
import org.apache.linkis.scheduler.queue.Job
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

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
