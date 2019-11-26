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
package com.webank.wedatasphere.dss.visualis.utils

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.util

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.dss.visualis.configuration.CommonConfig
import com.webank.wedatasphere.dss.visualis.model.DWCResultInfo
import com.webank.wedatasphere.linkis.adapt.LinkisUtils
import edp.core.model.BaseSource
import edp.davinci.dao.ProjectMapper
import edp.davinci.model.{Project, View}
import javax.imageio.ImageIO

/**
  * Created by johnnwang on 2019/1/23.
  */
object VisualisUtils {

  val HIVE_DATA_SOURCE_TOKEN = CommonVars("wds.dss.visualis.hive.datasource.token","hiveDataSource-token")
  val HIVE_DATA_SOURCE_ID =  CommonVars("wds.dss.visualis.hive.datasource.id",1)
  val TMP_VIEW_NAME =  CommonVars("wds.dss.visualis.view.tmp.name","tmpView")
  val DEFAULT_PROJECT_NAME = CommonVars("wds.dss.visualis.project.name","Public Project")
  val DEFAULT_PROJECT_ID = CommonVars[Long]("wds.dss.visualis.project.id",-1)

  val DWC_RESULT_INFO = CommonVars("wds.dss.visualis.result.info","dwcResultInfo")
  val BDP_DWC_INSTANCE = CommonVars[Integer]("wds.dss.instance",1)
  val BDP_DWC_VG_QUERY_TIMEOUT = CommonVars[Long]("wds.dss.visualis.query.timeout",1000 * 60 * 10)
  val CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME = CommonVars("wds.dss.visualis.conf.application.name", "cloud-configuration")
  val VG_CREATOR = CommonVars("wds.dss.visualis.creator", "Visualis")
  val VG_APP_NAME = CommonVars("wds.dss.visualis.app.name", "spark")
  val RESULT_FILE_NAME = CommonVars("wds.dss.visualis.result.file.name,", "/_0.dolphin")

  def isHiveDataSource(source:BaseSource):Boolean ={
    val hiveDataSourceToken  = HIVE_DATA_SOURCE_TOKEN.getValue
    hiveDataSourceToken.equals(source.getUsername)
  }

  def getHiveDataSourceId():Long = {
    HIVE_DATA_SOURCE_ID.getValue
  }

  def createTmpViewName(userName:String):String ={
    TMP_VIEW_NAME.getValue + "_" + userName + "_" + System.currentTimeMillis()
  }

  def isFirstTime(view: View):Boolean = {
    val tmpViewName  = TMP_VIEW_NAME.getValue
    view.getName.startsWith(tmpViewName)
  }

  def buildScala(sql: String,dwcResultInfo:DWCResultInfo,tableName:String):String ={
    val scala: String = "val sql = \"\"\" " + sql +"\"\"\"" + s""" \norg.apache.spark.sql.execution.datasources.csv.DolphinToSpark.createTempView""" +
      s"""(spark,"$tableName","${dwcResultInfo.getResultPath}", true);show(spark.sql(sql))"""
    scala
  }

  def getUserTicketKV(username: String): (String, String) = {
    LinkisUtils.getUserTicketKV(username)
  }

}
