package com.webank.wedatasphere.dss.visualis.entrance.spark

import java.util
import com.google.gson.reflect.TypeToken
import com.webank.wedatasphere.dss.visualis.configuration.CommonConfig
import com.webank.wedatasphere.dss.visualis.exception.{ResultTypeException, SparkEngineExecuteException, VGErrorException}
import com.webank.wedatasphere.dss.visualis.model.PaginateWithExecStatus
import com.webank.wedatasphere.dss.visualis.res.ResultHelper
import com.webank.wedatasphere.dss.visualis.ujes.UJESJob
import com.webank.wedatasphere.dss.visualis.utils.VisualisUtils
import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.cs.common.utils.CSCommonUtils
import org.apache.linkis.entrance.EntranceServer
import org.apache.linkis.entrance.exception.{EntranceRPCException, QueryFailedException}
import org.apache.linkis.governance.common.entity.task.{RequestPersistTask, RequestQueryTask}
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.queue.SchedulerEventState
import org.apache.linkis.server.JMap
import org.apache.linkis.server.security.SecurityFilter
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.linkis.storage.resultset.{ResultSetFactory, ResultSetReader}
import edp.core.exception.{ServerException, SourceException}
import edp.core.model._
import edp.core.utils.SqlUtils
import edp.davinci.model.Source
import org.apache.commons.lang.StringUtils
import org.apache.linkis.adapt.LinkisUtils
import org.json4s._
import org.json4s.jackson.Serialization.read
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.web.context.request.{RequestContextHolder, ServletRequestAttributes}

import scala.collection.JavaConversions
import scala.collection.JavaConversions._
import scala.math.BigDecimal.RoundingMode



class SparkEntranceExecutor extends SqlUtils with Logging{
  private var umUser:String=""
  implicit val formats = DefaultFormats
  @Autowired
  var entranceServer: EntranceServer = _

  override def init(source: BaseSource): SqlUtils = {
    if(source == null || VisualisUtils.isLinkisDataSource(source)){
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
    val execId: String = submitQuery(sql).toString()
    //SparkEntranceExecutor.putJobCache(umUser,execId)//缓存相应的执行ID
    entranceServer.getJob(execId) foreach {
      case job: VisualisJob =>
        job.waitForCompleted()
        if (!SchedulerEventState.isSucceed(job.getState)){
          job.getJobRequest match {
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
        //SparkEntranceExecutor.removeJobCache(umUser,execId)
        return op(job)
      case _ =>
        //SparkEntranceExecutor.removeJobCache(umUser,execId)
        throw new VGErrorException(70001, "executeRealJob failed, not supported job type.")
    }
    throw new VGErrorException(70001, s"executeRealJob failed, cannot find the job $execId.")
  }

  def submitQuery(sql: String) = {
    val input = read[UJESJob](sql)
    var code = input.code
    val jobType = input.jobType
    val source = JavaConversions.mapAsJavaMap(input.source.asInstanceOf[Map[String, String]])
    umUser = input.user
//    if (jobType.equals(UJESJob.SQL_TYPE)) {
//      code = SqlUtils.filterAnnotate(code)
//      SqlUtils.checkSensitiveSql(code)
//    }
    val requestMap = new JMap[String, AnyRef]
    requestMap.put(TaskConstant.UMUSER, umUser)
    requestMap.put(TaskConstant.REQUESTAPPLICATIONNAME, VisualisUtils.VG_CREATOR.getValue)// input.creator)
    requestMap.put(TaskConstant.EXECUTEAPPLICATIONNAME, input.engine)
    requestMap.put(TaskConstant.EXECUTIONCODE, code)
    requestMap.put(TaskConstant.RUNTYPE, jobType)
    requestMap.put(TaskConstant.SOURCE, source)
    val params = new util.HashMap[String, Object]()
    val configuration = new util.HashMap[String, Object]()
    val runtime = new util.HashMap[String, Object]()
    // updated cache related params
    runtime.put(TaskConstant.CACHE, input.cache.asInstanceOf[java.lang.Boolean])
    runtime.put(TaskConstant.CACHE_EXPIRE_AFTER, input.cacheExpireAfter.asInstanceOf[java.lang.Long])
    runtime.put(TaskConstant.READ_FROM_CACHE, input.readFromCache.asInstanceOf[java.lang.Boolean])
    runtime.put(TaskConstant.READ_CACHE_BEFORE, input.readCacheBefore.asInstanceOf[java.lang.Long])
    if(StringUtils.isNotBlank(input.contextId)){
      runtime.put(CSCommonUtils.CONTEXT_ID_STR, input.contextId)
    }
    configuration.put(TaskConstant.PARAMS_CONFIGURATION_RUNTIME, runtime)
    params.put(TaskConstant.PARAMS_CONFIGURATION, configuration)
    requestMap.put(TaskConstant.PARAMS, params)
    val execId = entranceServer.execute(requestMap)
    execId
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
      Utils.tryQuietly(reader.close())
      //info(s"$umUser finish to get the result of execution :$resultSet")
      res
    }else if(rsFactory.isResultSetPath(resultSet)){
      val resPath = new FsPath(ResultHelper.getSchemaPath(resultSet))
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
      Utils.tryQuietly(reader.close())
      Utils.tryQuietly(fs.close())
      //info(s"$umUser finish to get the result of execution :$resultSet")
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
      job.getJobRequest match {
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

  override def submit4Exec(sql: String, pageNo: Int, pageSize: Int, totalCount: Int, limit: Int, excludeColumns: util.Set[String]): PaginateWithExecStatus = {
    val paginateWithQueryColumns = new PaginateWithExecStatus
    val execId = submitQuery(sql).toString()
    paginateWithQueryColumns.setExecId(VisualisUtils.getHAExecId(execId))
    paginateWithQueryColumns.setProgress(0.0f)
    return  paginateWithQueryColumns;
  }

  override def getProgress4Exec(execId: String, user: String): PaginateWithExecStatus = {
    val instance = VisualisUtils.getInstanceByHAExecId(execId)
    val realExecId = VisualisUtils.getExecIdByHAExecId(execId)
    if(instance.equals(Sender.getThisInstance)){
      entranceServer.getJob(realExecId) foreach {
        case job: VisualisJob =>
          val paginateWithQueryColumns = new PaginateWithExecStatus
          paginateWithQueryColumns.setExecId(execId)
          paginateWithQueryColumns.setProgress(BigDecimal(job.getProgress).setScale(2, RoundingMode.HALF_UP).floatValue())
          paginateWithQueryColumns.setStatus(job.getState.toString)
          return paginateWithQueryColumns
        case _ =>
          throw new VGErrorException(70001, "executeRealJob failed, not supported job type.")
      }
      throw new VGErrorException(70001, s"executeRealJob failed, cannot find the job $execId.")
    } else {
      val task = VisualisUtils.getQueryTask(instance, realExecId)
      val paginateWithQueryColumns = new PaginateWithExecStatus
      paginateWithQueryColumns.setExecId(execId)
      paginateWithQueryColumns.setProgress(BigDecimal(task.getProgress).setScale(2, RoundingMode.HALF_UP).floatValue())
      paginateWithQueryColumns.setStatus(task.getStatus)
      return paginateWithQueryColumns
    }
  }

  override def kill4Exec(execId: String, user: String): PaginateWithExecStatus = {
    val instance = VisualisUtils.getInstanceByHAExecId(execId)
    val realExecId = VisualisUtils.getExecIdByHAExecId(execId)
    if(instance.equals(Sender.getThisInstance)){
      entranceServer.getJob(realExecId) foreach {
        case job: VisualisJob =>
          if(!SchedulerEventState.isCompleted(job.getState)){
            job.kill();
          }
          val paginateWithQueryColumns = new PaginateWithExecStatus
          paginateWithQueryColumns.setExecId(execId)
          paginateWithQueryColumns.setProgress(job.getProgress)
          paginateWithQueryColumns.setStatus(job.getState.toString)
          return paginateWithQueryColumns
        case _ =>
          throw new VGErrorException(70001, "kill job failed, not supported job type.")
      }
      throw new VGErrorException(70001, s"kill job failed, cannot find the job $execId.")
    } else {
      VisualisUtils.killHA(instance, execId)
    }
  }

  override def getResultSet4Exec(execId: String, user: String): PaginateWithExecStatus = {
    val instance = VisualisUtils.getInstanceByHAExecId(execId)
    val realExecId = VisualisUtils.getExecIdByHAExecId(execId)
    val paginateWithQueryColumns = new PaginateWithExecStatus
    if(instance.equals(Sender.getThisInstance)){
      entranceServer.getJob(realExecId) foreach {
        case job: VisualisJob =>
          job.waitForCompleted()

          //TODO consider if necessary here
          if (!SchedulerEventState.isSucceed(job.getState)){
            job.getJobRequest match {
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

          paginateWithQueryColumns.setExecId(execId)
          paginateWithQueryColumns.setProgress(job.getProgress)
          paginateWithQueryColumns.setStatus(job.getState.toString)
          val resultSets = job.getResultSets
          if(resultSets.isEmpty){
            paginateWithQueryColumns.setResultList(new util.ArrayList[util.Map[String, AnyRef]])
            paginateWithQueryColumns.setTotalCount(0)
          } else {
            val resultList = getResultSet(resultSets(resultSets.length - 1))
            paginateWithQueryColumns.setResultList(resultList)
            paginateWithQueryColumns.setTotalCount(resultList.size())
            val columns = ResultHelper.getResultType(resultSets(resultSets.length - 1))
            paginateWithQueryColumns.setColumns(columns.map(col => new QueryColumn(col.columnName,col.dataType.typeName)).toList)
          }
          return paginateWithQueryColumns;
        case _ =>
          //SparkEntranceExecutor.removeJobCache(umUser,execId)
          throw new VGErrorException(70001, "executeRealJob failed, not supported job type.")
      }
      throw new VGErrorException(70001, s"executeRealJob failed, cannot find the job $execId.")
    } else {
      val task = VisualisUtils.getQueryTask(instance, realExecId)
      paginateWithQueryColumns.setExecId(execId)
      paginateWithQueryColumns.setProgress(task.getProgress)
      paginateWithQueryColumns.setStatus(task.getStatus)
      val resultList = getResultSet(task.getResultLocation + VisualisUtils.RESULT_FILE_NAME.getValue)
      paginateWithQueryColumns.setResultList(resultList)
      paginateWithQueryColumns.setTotalCount(resultList.size())
      val columns = ResultHelper.getResultType(task.getResultLocation + VisualisUtils.RESULT_FILE_NAME.getValue)
      paginateWithQueryColumns.setColumns(columns.map(col => new QueryColumn(col.columnName,col.dataType.typeName)).toList)
      return paginateWithQueryColumns
    }
  }

  override def query4Paginate(sql: String, pageNo: Int, pageSize: Int, totalCount: Int, limit: Int, excludeColumns: util.Set[String]): PaginateWithQueryColumns = {
    val paginateWithQueryColumns = new PaginateWithQueryColumns
    val resultSets = querySQLWithResultSetPaths(sql, limit)

    if(resultSets.isEmpty){
      paginateWithQueryColumns.setResultList(new util.ArrayList[util.Map[String, AnyRef]])
      paginateWithQueryColumns.setTotalCount(0)
    } else {
      val resultList = getResultSet(resultSets(resultSets.length - 1))
      paginateWithQueryColumns.setResultList(resultList)
      paginateWithQueryColumns.setTotalCount(resultList.size())
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
//  val QUERY_JOB_CACHE = new ConcurrentHashMap[String, String]()
//
//  def putJobCache(user:String,execId:String): Unit ={
//    QUERY_JOB_CACHE.put(user,execId)
//  }
//
//  def removeJobCache(user:String,execId:String):Unit = {
//    QUERY_JOB_CACHE.remove(user, execId)
//  }
//
//  def getJobCache(user:String):String = {
//    QUERY_JOB_CACHE.get(user)
//  }
}