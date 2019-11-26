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
package com.webank.wedatasphere.dss.visualis.configuration

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import sun.misc.BASE64Encoder
/**
  * Created by allenlliu on 2019/1/26.
  */
object CommonConfig {
  val QUERY_PERSISTENCE_SPRING_APPLICATION_NAME = CommonVars("wds.dss.query.application.name", "cloud-query")
  val QUERY_PERSISTENCE_SPRING_TIME = CommonVars("wds.dss.visualis.query.time,", 10000)
  val ENGINE_DEFAULT_LIMIT = CommonVars("wds.dss.engine.default.limit", 5000)
  /**
  this is the configuration to get the hive database source
    */
  val GATEWAY_IP = CommonVars("wds.dss.visualis.gateway.ip", "")

  val GATEWAY_PORT = CommonVars("wds.dss.visualis.gateway.port", "")

  val GATEWAY_PROTOCOL = CommonVars("wds.dss.visualis.gateway.protocol", "http://")

  val DB_URL_SUFFIX = CommonVars("wds.dss.visualis.database.url", "/api/rest_j/v1/datasource/dbs")

  val TICKET_ID_STRING = CommonVars("wds.dss.visualis.ticketid", "bdp-user-ticket-id")

  val TABLE_URL_SUFFIX = CommonVars("wds.dss.visualis.table.url", "/api/rest_j/v1/datasource/tables")

  val COLUMN_URL_SUFFIX = CommonVars("wds.dss.visualis.column.url", "/api/rest_j/v1/datasource/columns")

  val HIVE_DATASOURCE_URL = CommonVars("wds.dss.visualis.hive.datasource.url", "test")

  val HIVE_DATASOURCE_NAME = CommonVars("wds.dss.visualis.hive.datasource.name", "hive")

}
