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

import com.webank.wedatasphere.linkis.entrance.job.EntranceExecutionJob
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState.SchedulerEventState

import scala.collection.mutable.ArrayBuffer
/**
  * Created by shanhuang on 2019/1/23.
  */
class VisualisJob extends EntranceExecutionJob {
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
