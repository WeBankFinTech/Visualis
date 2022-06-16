package org.apache.linkis.adapt

import com.google.gson._
import org.apache.linkis.server.BDPJettyServerHelper
import org.apache.linkis.server.security.SSOUtils

import java.lang
import java.lang.reflect.Type


object LinkisUtils {

  val gson = BDPJettyServerHelper.gson

  val gsonNoContert = new GsonBuilder().disableHtmlEscaping().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").serializeNulls
    .registerTypeAdapter(classOf[java.lang.Double], new JsonSerializer[java.lang.Double] {
      override def serialize(t: lang.Double, `type`: Type, jsonSerializationContext: JsonSerializationContext): JsonElement =
        if (t == t.longValue()) new JsonPrimitive(t.longValue()) else new JsonPrimitive(t)
    }).create

  def getUserTicketKV(username: String) = {
    SSOUtils.getUserTicketKV(username)
  }

}
