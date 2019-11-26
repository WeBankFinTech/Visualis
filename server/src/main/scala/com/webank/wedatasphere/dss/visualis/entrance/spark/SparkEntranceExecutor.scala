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

import java.util
import java.util.concurrent.ConcurrentHashMap

import com.google.gson.reflect.TypeToken
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.EntranceServer
import com.webank.wedatasphere.linkis.entrance.exception.{EntranceRPCException, QueryFailedException}
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.protocol.query.{RequestPersistTask, RequestQueryTask, ResponsePersist}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState
import com.webank.wedatasphere.linkis.server.{BDPJettyServerHelper, JMap}
import com.webank.wedatasphere.linkis.server.security.SecurityFilter
import com.webank.wedatasphere.linkis.storage.FSFactory
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetReader}
import com.webank.wedatasphere.dss.visualis.configuration.CommonConfig
import com.webank.wedatasphere.dss.visualis.exception.{ResultTypeException, SparkEngineExecuteException, VGErrorException}
import com.webank.wedatasphere.dss.visualis.res.ResultHelper
import com.webank.wedatasphere.dss.visualis.ujes.UJESJob
import com.webank.wedatasphere.dss.visualis.utils.VisualisUtils
import com.webank.wedatasphere.linkis.adapt.LinkisUtils
import edp.core.exception.{ServerException, SourceException}
import edp.core.model._
import edp.core.utils.SqlUtils
import edp.davinci.model.Source
import org.json4s._
import org.json4s.jackson.Serialization.read
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Scope, ScopedProxyMode}
import org.springframework.stereotype.Component
import org.springframework.web.context.request.{RequestContextHolder, ServletRequestAttributes}

import scala.collection.JavaConversions._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._
import org.json4s._
import org.json4s.jackson._
import org.json4s.jackson.Serialization.{read, write}
import org.springframework.context.annotation.Scope
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.context.WebApplicationContext


/**
  * Created by shanhuang on 2019/1/23.
  */
@Component
@Scope("prototype")
class SparkEntranceExecutor extends SqlUtils with Logging{
  private var umUser:String=""
  implicit val formats = DefaultFormats
  @Autowired
  private var entranceServer: EntranceServer = _

  override def init(source: BaseSource): SqlUtils = {
    if(VisualisUtils.isHiveDataSource(source)){
      //info(s"SparkEntranceExecutor is initing, config is: ${source.asInstanceOf[Source].getConfig}")
      val executor = new SparkEntranceExecutor
      executor.jdbcDataSource = this.jdbcDataSource
      executor.jdbcUrl = this.jdbcUrl
      executor.username = this.username
      executor.password = this.password
      executor.entranceServer = this.entranceServer
      if(source.isInstanceOf[Source]){
        val  config = LinkisUtils.gson.fromJson(source.asInstanceOf[Source].getConfig,classOf[util.Map[String,Any]])
        executor.isSchedulerTask = if(null == config.get("isSchedulerTask")) true else config.get("isSchedulerTask").asInstanceOf[Boolean]
      }
      executor
    }  else super.init(source)
  }

  override def init(jdbcUrl: String, username: String, password: String, dbVersion: String, ext: Boolean): SqlUtils = {
    val source = new BaseSource {
      override def getJdbcUrl: String = jdbcUrl
      override def getUsername: String = username
      override def getPassword: String = password
      override def getDatabase: String = ""
      override def getDbVersion: String = dbVersion
      override def isExt: Boolean = ext
    }
    init(source)
  }

  private def executeUntil[T](sql: String, op: VisualisJob => T): T = {
    info(s"$umUser began to executeRealJob script:$sql")
    val input = read[UJESJob](sql)
    var code =input.code
    val jobType = input.jobType
    val source = input.source
    umUser = input.user
    if(jobType.equals(UJESJob.SQL_TYPE)) {
      code = SqlUtils.filterAnnotate(code)
      SqlUtils.checkSensitiveSql(code)
    }
    val requestMap = new JMap[String, Any]
    requestMap.put(TaskConstant.UMUSER, umUser)
    requestMap.put(TaskConstant.REQUESTAPPLICATIONNAME, VisualisUtils.VG_CREATOR.getValue)
    requestMap.put(TaskConstant.EXECUTEAPPLICATIONNAME, VisualisUtils.VG_APP_NAME.getValue)
    requestMap.put(TaskConstant.EXECUTIONCODE, code)
    requestMap.put(TaskConstant.RUNTYPE,jobType)
    requestMap.put(TaskConstant.SOURCE,source)
    requestMap.put(TaskConstant.PARAMS,new util.HashMap())
    val execId = entranceServer.execute(requestMap)
    SparkEntranceExecutor.putJobCache(umUser,execId)//缓存相应的执行ID
    entranceServer.getJob(execId) foreach {
      case job: VisualisJob =>
        job.waitForCompleted()
        if (!SchedulerEventState.isSucceed(job.getState)){
          job.getTask match {
            case t: RequestPersistTask =>
              if(t.getErrCode != null && t.getErrDesc != null){
                throw SparkEngineExecuteException(t.getErrCode, "spark engine run sql failed:" + t.getErrDesc)
              }
            case _ =>
          }
          if(job.getErrorResponse != null){
            throw SparkEngineExecuteException(60001, job.getErrorResponse.message)
          }
          throw SparkEngineExecuteException(60001, "spark engine run sql failed")
        }
        info(s"$umUser finish to executeRealJob script:$sql")
        SparkEntranceExecutor.removeJobCache(umUser,execId)
        return op(job)
      case _ =>
        SparkEntranceExecutor.removeJobCache(umUser,execId)
        throw new VGErrorException(70001, "executeRealJob failed, not supported job type.")
    }
    throw new VGErrorException(70001, s"executeRealJob failed, cannot find the job $execId.")
  }

  private def getHistoryQuery(sql: String): Option[RequestPersistTask] = {
    val ujesJob = read[UJESJob](sql)
    val code = ujesJob.code
    val sender = Sender.getSender(CommonConfig.QUERY_PERSISTENCE_SPRING_APPLICATION_NAME.getValue)
    val task = new RequestQueryTask
    //    task.setUmUser(getCurrentUser)
    task.setUmUser(ujesJob.user)
    task.setExecutionCode(code)
    var responsePersist:ResponsePersist = null
    Utils.tryThrow(
      responsePersist = sender.ask(task).asInstanceOf[ResponsePersist]
    )(e => {
      val errorException =  new EntranceRPCException(60010, "sender rpc failed")
      errorException.initCause(e)
      throw errorException
    })
    if (responsePersist != null){
      val  status = responsePersist.getStatus()
      val  message = responsePersist.getMsg()
      if (status != 0 ){
        throw new QueryFailedException(60011, "insert task failed, reason: " + message)
      }
      val data = responsePersist.getData()
      val inputJson=LinkisUtils.gson.toJson(data.get(TaskConstant.TASK))
      //      val extractJson = parse(inputJson)
      val typeToken = new TypeToken[java.util.List[RequestPersistTask]](){}.getType
      val ResponseRequestPersistTasks = LinkisUtils.gson.fromJson(inputJson,typeToken).asInstanceOf[java.util.List[RequestPersistTask]]
      //      val ResponseRequestPersistTasks = extractJson.extract[List[RequestPersistTask]]
      if (ResponseRequestPersistTasks == null){
        throw new QueryFailedException(60012, "query task failed, reason: " + message)
      }
      val now =System.currentTimeMillis()
      val sortedTasks = ResponseRequestPersistTasks.filter( x=>{
        now - x.getCreatedTime.getTime    < CommonConfig.QUERY_PERSISTENCE_SPRING_TIME.getValue
      }).sortWith((x,y)=>x.getCreatedTime.getTime > y.getCreatedTime.getTime)
      if(sortedTasks.size >0){
        Some(sortedTasks(0))
      }else{
        None
      }
    }else{
      None
    }

  }

  /**
    * 将一个结果集文件或结果集解析成一个List
    * @param resultSet 结果集文件或结果集
    * @return
    */
  private def getResultSet(resultSet: String): util.List[util.Map[String, AnyRef]] = {
    info(s"$umUser began to get the result of execution :$resultSet")
    val rsFactory= ResultSetFactory.getInstance
    val res = new util.ArrayList[util.Map[String, AnyRef]]()
    if(rsFactory.isResultSet(resultSet)){
      val resultSetModel =  rsFactory.getResultSetByContent(resultSet)
      if(ResultSetFactory.TABLE_TYPE != resultSetModel.resultSetType()){
        throw new VGErrorException(60013,"不支持不是表格的结果集")
      }
      val reader =ResultSetReader.getResultSetReader(resultSetModel,resultSet)
      val metaData = reader.getMetaData.asInstanceOf[TableMetaData]
      while (reader.hasNext) {
        val record = reader.getRecord.asInstanceOf[TableRecord]
        val lineMap = new util.LinkedHashMap[String, AnyRef]()
        val columns = metaData.columns
        val row = record.row
        if (columns.size > 0) {
          for (x <- 0 until columns.size) {
            lineMap.put(columns(x).columnName, parseValue(row(x)))
          }
        }
        res.add(lineMap)
      }
      info(s"$umUser finish to get the result of execution :$resultSet")
      res
    }else if(rsFactory.isResultSetPath(resultSet)){
      val resPath = new FsPath(resultSet)
      val resultSetContent =  rsFactory.getResultSetByPath(resPath)
      if(ResultSetFactory.TABLE_TYPE != resultSetContent.resultSetType()){
        throw new VGErrorException(60014,"不支持不是表格的结果集")
      }
      val fs = FSFactory.getFs(resPath)
      fs.init(null)
      val reader =ResultSetReader.getResultSetReader(resultSetContent,fs.read(resPath))
      val metaData = reader.getMetaData.asInstanceOf[TableMetaData]
      while (reader.hasNext){
        val record = reader.getRecord.asInstanceOf[TableRecord]
        val lineMap = new util.LinkedHashMap[String,AnyRef]()
        val columns = metaData.columns
        val row = record.row
        if(columns.size>0) {
          for (x <- 0 until columns.size) {
            lineMap.put(columns(x).columnName, parseValue(row(x)))
          }
        }
        res.add(lineMap)
      }
      info(s"$umUser finish to get the result of execution :$resultSet")
      res
    }else{
      throw new ResultTypeException(60015,"结果集类型异常:"+resultSet)
    }
  }

  private def parseValue(original: Any) : AnyRef = {
    original match {
      case bigDecimal: BigDecimal => bigDecimal.toDouble.asInstanceOf[AnyRef]
      case bool: Boolean => bool.toString
      case boolean: java.lang.Boolean => boolean.toString
      case _ => original.asInstanceOf[AnyRef]
    }
  }

  override def querySQLWithResultSetLocation(sql: String, limit: Int) : Array[String] = {
//    getHistoryQuery(sql).map(_.getResultLocation).map{l =>
//      val fsPath = new FsPath(l)
//      val fs = FSFactory.getFs(fsPath)
//      fs.init(null)
//      Utils.tryCatch(fs.list(fsPath).map(_.getPath).toArray){t:Throwable =>
//        info("Failed to getResultLocation:",t)
//        Array()
//      }
//    }.filter(_.isEmpty).getOrElse(
      executeUntil(sql, { job =>
      job.getTask match {
        case t: RequestPersistTask => Array(t.getResultLocation + VisualisUtils.RESULT_FILE_NAME.getValue)
        case _ => Array()
      }
    })
    //)
  }

  private def querySQLWithResultSetPaths(sql: String, limit: Int): Array[String] =
  //TODO 需要与UJES那边兼容，考虑limit语法和全量导出功能
//    getHistoryQuery(sql).map(_.getResultLocation).map{l =>
//      val fsPath = new FsPath(l)
//      val fs = FSFactory.getFs(fsPath)
//      fs.init(null)
//      Utils.tryCatch(fs.list(fsPath).map(_.getPath).toArray){t:Throwable =>
//        info("Failed to getResultLocation:",t)
//        Array()
//      }
//    }.filter(_.isEmpty).getOrElse(
      executeUntil(sql, _.getResultSets)
    //)

  override def execute(sql: String): Unit = executeUntil(sql, _ => ())

  override def query4List(sql: String, limit: Int): util.List[util.Map[String, AnyRef]] = {
    val resultSets = querySQLWithResultSetPaths(sql, limit)
    if(resultSets.isEmpty) new util.ArrayList[util.Map[String, AnyRef]] else getResultSet(resultSets(resultSets.length - 1))
  }

  override def query4Paginate(sql: String, pageNo: Int, pageSize: Int, totalCount: Int, limit: Int, excludeColumns: util.Set[String]): PaginateWithQueryColumns = {
    val paginateWithQueryColumns = new PaginateWithQueryColumns
    val resultSets = querySQLWithResultSetPaths(sql, limit)

    if(resultSets.isEmpty){
      paginateWithQueryColumns.setResultList(new util.ArrayList[util.Map[String, AnyRef]])
    } else {
      paginateWithQueryColumns.setResultList(getResultSet(resultSets(resultSets.length - 1)))
      val columns = ResultHelper.getResultType(resultSets(resultSets.length - 1))
      paginateWithQueryColumns.setColumns(columns.map(col => new QueryColumn(col.columnName,col.dataType.typeName)).toList)
    }
    return  paginateWithQueryColumns;
  }

  /**
    * 判断表是否存在
    *
    * @param tableName
    * @return
    * @throws SourceException
    */
  override def tableIsExist(tableName: String): Boolean = super.tableIsExist(tableName)

  /**
    * 根据sql查询列
    *update by johnnwang
    * @param sql
    * @return
    * @throws ServerException
    */
  override def getColumns(sql: String): util.List[QueryColumn] = {
    val resultSets = querySQLWithResultSetPaths(sql, 2)
    if(resultSets.isEmpty) null else {
      val columns = ResultHelper.getResultType(resultSets(resultSets.length - 1))
      columns.map(col => new QueryColumn(col.columnName,col.dataType.typeName)).toList
    }
  }

  override def testConnection(): Boolean = super.testConnection()

  override def jdbcTemplate(): JdbcTemplate = super.jdbcTemplate()

  override def executeBatch(sql: String, headers: util.Set[QueryColumn], datas: util.List[util.Map[String, AnyRef]]): Unit = super.executeBatch(sql, headers, datas)

  def getCurrentUser(): String ={
    val request = RequestContextHolder.getRequestAttributes().asInstanceOf[ServletRequestAttributes].getRequest
    val user = SecurityFilter.getLoginUsername(request)
    info("Get current user is "+ user)
    user
  }
}

object SparkEntranceExecutor{
  val QUERY_JOB_CACHE = new ConcurrentHashMap[String, String]()

  def putJobCache(user:String,execId:String): Unit ={
    QUERY_JOB_CACHE.put(user,execId)
  }

  def removeJobCache(user:String,execId:String):Unit = {
    QUERY_JOB_CACHE.remove(user, execId)
  }

  def getJobCache(user:String):String = {
    QUERY_JOB_CACHE.get(user)
  }
}