package com.webank.wedatasphere.dss.visualis.rpc

import com.webank.wedatasphere.dss.visualis.query.utils.JdbcAsyncUtils
import org.apache.linkis.rpc.{Receiver, Sender}
import edp.core.utils.SqlUtils

import scala.concurrent.duration.Duration

class VisualisReceiver(sqlUtils: SqlUtils) extends Receiver {

  override def receive(message: Any, sender: Sender): Unit = {}

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case k: RequestKillTask =>
      sqlUtils.kill4Exec(k.execId, null)
    case j: RequestJDBCResult =>
      JdbcAsyncUtils.getResult(j.execId)
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {}
}
