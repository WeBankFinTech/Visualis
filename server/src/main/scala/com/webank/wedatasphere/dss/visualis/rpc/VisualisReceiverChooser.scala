package com.webank.wedatasphere.dss.visualis.rpc

import org.apache.linkis.rpc.{RPCMessageEvent, Receiver, ReceiverChooser}
import edp.core.utils.SqlUtils
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class VisualisReceiverChooser extends ReceiverChooser {

  @Autowired
  private var sqlUtils: SqlUtils = _
  private var receiver: Option[VisualisReceiver] = _

  @PostConstruct
  def init(): Unit = receiver = Some(new VisualisReceiver(sqlUtils))

  override def chooseReceiver(event: RPCMessageEvent): Option[Receiver] = event.message match {
    case _: VisualisProtocol => receiver
    case _ => None
  }
}
