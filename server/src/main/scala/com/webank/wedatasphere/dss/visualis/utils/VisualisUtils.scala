package com.webank.wedatasphere.dss.visualis.utils

import java.util.Date
import com.google.common.collect.Lists
import com.webank.wedatasphere.dss.visualis.model.{DWCResultInfo, PaginateWithExecStatus}
import com.webank.wedatasphere.dss.visualis.rpc.{RequestJDBCResult, RequestKillTask}
import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.governance.common.entity.task.{RequestOneTask, RequestPersistTask}
import org.apache.linkis.manager.label.entity.engine.RunType
import org.apache.linkis.protocol.query.cache.RequestDeleteCache
import org.apache.linkis.rpc.Sender
import org.apache.linkis.ujes.client.response.JobInfoResult
import edp.core.model.{BaseSource, PaginateWithQueryColumns}
import edp.davinci.model.View
import org.apache.linkis.adapt.LinkisUtils

/**
  * Created by johnnwang on 2019/1/23.
  */
object VisualisUtils {

  val sender = Sender.getSender(EntranceConfiguration.JOBHISTORY_SPRING_APPLICATION_NAME.getValue)

  val HIVE_DATA_SOURCE_TOKEN = CommonVars("wds.dss.visualis.hive.datasource.token","hiveDataSource-token")
  val HIVE_DATA_SOURCE_ID =  CommonVars("wds.dss.visualis.hive.datasource.id",1)
  val PRESTO_DATA_SOURCE_TOKEN = CommonVars("wds.dss.visualis.presto.datasource.token","prestoDataSource-token")
  val PRESTO_DATA_SOURCE_ID =  CommonVars("wds.dss.visualis.presto.datasource.id",210)
  val TMP_VIEW_NAME =  CommonVars("wds.dss.visualis.view.tmp.name","tmpView")
  val DEFAULT_PROJECT_NAME = CommonVars("wds.dss.visualis.project.name","Public Project")
  val DEFAULT_PROJECT_ID = CommonVars[Long]("wds.dss.visualis.project.id",-1)

  val DWC_RESULT_INFO = CommonVars("wds.dss.visualis.result.info","dwcResultInfo")
  val VG_CREATOR = CommonVars("wds.dss.visualis.creator", "Visualis")
  val SPARK = CommonVars("wds.dss.visualis.spark.name", "spark")
  val PRESTO = CommonVars("wds.dss.visualis.presto.name", "presto")
  val RESULT_FILE_NAME = CommonVars("wds.dss.visualis.result.file.name", "/_0.dolphin")
  val SCALA_RESULT_FILE_NAME = CommonVars("wds.dss.visualis.result.file.name", "/_1.dolphin")
  val HA_EXEC_ID_SEPARATOR = CommonVars("wds.dss.visualis.ha.exec.id.separator", "@")
  val TASK_SEARCH_TIME = CommonVars("wds.dss.visualis.task.search.time", 1000*60*60*24L)
  val AVAILABLE_ENGINE_TYPES = CommonVars("wds.dss.visualis.available.engine.types", "spark;presto")

  def getDataSourceName(engineType: String) = {
    if(SPARK.getValue.equals(engineType)){
      "hive"
    } else {
      PRESTO.getValue
    }
  }

  def getCreator(engineType: String) = {
    if(SPARK.getValue.equals(engineType)){
      VG_CREATOR.getValue
    } else {
      "IDE"
    }
  }

  def isLinkisDataSource(source:BaseSource):Boolean ={
    isHiveDataSource(source) || isPrestoDataSource(source)
  }

  def isPrestoDataSource(source:BaseSource):Boolean ={
    val prestoDataSourceToken  = PRESTO_DATA_SOURCE_TOKEN.getValue
    prestoDataSourceToken.equals(source.getUsername)
  }

  def getPrestoDataSourceId():Long = {
    PRESTO_DATA_SOURCE_ID.getValue
  }

  def getResultSetPath(jobInfo: JobInfoResult) = {
    if(RunType.SCALA.toString.equalsIgnoreCase(jobInfo.getRequestPersistTask.getRunType)){
      jobInfo.getRequestPersistTask.getResultLocation + VisualisUtils.SCALA_RESULT_FILE_NAME.getValue
    } else {
      jobInfo.getRequestPersistTask.getResultLocation + VisualisUtils.RESULT_FILE_NAME.getValue
    }
  }

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

  def buildScala(sql: String, resultLocation: String, tableName:String):String ={
    val scala: String = "val sql = \"\"\" " + sql +"\"\"\"" + s""" \norg.apache.spark.sql.execution.datasources.csv.DolphinToSpark.createTempView""" +
      s"""(spark,"$tableName","${resultLocation}", true);show(spark.sql(sql))"""
    scala
  }

  def getUserTicketKV(username: String): (String, String) = {
    LinkisUtils.getUserTicketKV(username)
  }

  def deleteCache(executionCode: String, user: String) = {
    sender.ask(new RequestDeleteCache(executionCode, SPARK.getValue, Lists.newArrayList(user)))
  }

  def getHAExecId(execId: String) : String = {
    execId + HA_EXEC_ID_SEPARATOR.getValue + Sender.getThisInstance
  }

  def getInstanceByHAExecId(haExecId: String) : String = {
    haExecId.split(HA_EXEC_ID_SEPARATOR.getValue)(1)
  }

  def getExecIdByHAExecId(haExecId: String) : String = {
    haExecId.split(HA_EXEC_ID_SEPARATOR.getValue)(0)
  }

  def getQueryTask(instance: String, execId: String) : RequestPersistTask = {
    val requestOneTask = new RequestOneTask
    requestOneTask.setInstance(instance)
    requestOneTask.setExecId(VisualisUtils.getExecIdByHAExecId(execId))
    requestOneTask.setExecuteApplicationName(VisualisUtils.SPARK.getValue)
    val currentTimeMillis = System.currentTimeMillis()
    requestOneTask.setStartTime(new Date(currentTimeMillis - TASK_SEARCH_TIME.getValue))
    requestOneTask.setEndTime(new Date(currentTimeMillis))
    sender.ask(requestOneTask).asInstanceOf[RequestPersistTask]
  }

  def killHA(instance: String, execId: String) : PaginateWithExecStatus = {
    val requestKillTask = new RequestKillTask(execId)
    Sender.getSender(ServiceInstance(Sender.getThisServiceInstance.getApplicationName, instance))
      .ask(requestKillTask).asInstanceOf[PaginateWithExecStatus]
  }

  def getJDBCResult(instance: String, execId: String) : PaginateWithQueryColumns = {
    Sender.getSender(ServiceInstance(Sender.getThisServiceInstance.getApplicationName, instance))
      .ask(new RequestJDBCResult(execId)).asInstanceOf[PaginateWithQueryColumns]
  }

}
