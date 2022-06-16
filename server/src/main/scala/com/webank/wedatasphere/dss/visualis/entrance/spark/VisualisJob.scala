package com.webank.wedatasphere.dss.visualis.entrance.spark

import org.apache.linkis.entrance.job.EntranceExecutionJob
import org.apache.linkis.scheduler.queue.SchedulerEventState
import org.apache.linkis.scheduler.queue.SchedulerEventState.SchedulerEventState

import scala.collection.mutable.ArrayBuffer

class VisualisJob(persistenceManager: VisualisPersistenceManager) extends EntranceExecutionJob(persistenceManager) {
  private val resultSets = ArrayBuffer[String]()

  def addResultSet(resultSet: String): Unit = resultSets += resultSet
  def getResultSets: Array[String] = {
    resultSets.sortBy(_.toString).toArray
  }

  def waitForCompleted(): Unit = {
    if(SchedulerEventState.isCompleted(this.getState)) return
    resultSets synchronized {
      while(!SchedulerEventState.isCompleted(this.getState)) resultSets.wait()
    }
  }

  override def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {
    super.afterStateChanged(fromState, toState)
    if(SchedulerEventState.isCompleted(this.getState)) resultSets synchronized resultSets.notify()
  }
}
