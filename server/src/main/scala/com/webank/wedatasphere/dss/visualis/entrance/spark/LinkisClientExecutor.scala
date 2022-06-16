package com.webank.wedatasphere.dss.visualis.entrance.spark

import java.util
import java.util.concurrent.{Executors, TimeUnit}
import com.google.common.cache.CacheBuilder
import com.webank.wedatasphere.dss.visualis.entrance.spark.LinkisClientExecutor.linkisClient
import com.webank.wedatasphere.dss.visualis.exception.{ResultTypeException, SparkEngineExecuteException, VGErrorException}
import com.webank.wedatasphere.dss.visualis.model.PaginateWithExecStatus
import com.webank.wedatasphere.dss.visualis.res.ResultHelper
import com.webank.wedatasphere.dss.visualis.ujes.UJESJob
import com.webank.wedatasphere.dss.visualis.utils.VisualisUtils
import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.cs.common.utils.CSCommonUtils
import org.apache.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy
import org.apache.linkis.httpclient.dws.config.DWSClientConfigBuilder
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.utils.ZuulEntranceUtils
import org.apache.linkis.scheduler.queue.SchedulerEventState
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.linkis.storage.resultset.{ResultSetFactory, ResultSetReader}
import org.apache.linkis.ujes.client.UJESClient
import org.apache.linkis.ujes.client.request.JobExecuteAction
import org.apache.linkis.ujes.client.response.{JobExecuteResult, JobInfoResult}
import edp.core.exception.{ServerException, SourceException}
import edp.core.model.{BaseSource, PaginateWithQueryColumns, QueryColumn}
import edp.core.utils.SqlUtils
import edp.davinci.model.Source
import org.apache.commons.lang.StringUtils
import org.apache.linkis.adapt.LinkisUtils
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.read
import org.springframework.context.annotation.Scope
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

import scala.collection.JavaConversions
import scala.collection.JavaConversions._
import scala.math.BigDecimal.RoundingMode


@Component
@Scope("prototype")
class LinkisClientExecutor extends SqlUtils with Logging{

  private var umUser:String=""
  implicit val formats = DefaultFormats

  override def init(source: BaseSource): SqlUtils = {
    if(source == null || VisualisUtils.isLinkisDataSource(source)){
      //info(s"SparkEntranceExecutor is initing, config is: ${source.asInstanceOf[Source].getConfig}")
      val executor = new LinkisClientExecutor
      executor.jdbcDataSource = this.jdbcDataSource
      executor.jdbcUrl = this.jdbcUrl
      executor.username = this.username
      executor.password = this.password
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

  private def executeUntil[T](sql: String, op: JobExecuteResult => T): T = {
    info(s"$umUser began to executeRealJob script:$sql")
    val jobExecuteResult: JobExecuteResult = submitQuery(sql)
    var jobInfoResult = linkisClient.getJobInfo(jobExecuteResult)
    var status = SchedulerEventState.withName(jobInfoResult.getRequestPersistTask.getStatus)
    while(!SchedulerEventState.isCompleted(status)) {
      Utils.sleepQuietly(500)
      jobInfoResult = linkisClient.getJobInfo(jobExecuteResult)
      status = SchedulerEventState.withName(jobInfoResult.getRequestPersistTask.getStatus)
    }
    if(!SchedulerEventState.isSucceed(status)){
      val jobInfo = linkisClient.getJobInfo(jobExecuteResult)
      val requestPersistTask = jobInfo.getRequestPersistTask
      if(requestPersistTask.getErrCode != null && requestPersistTask.getErrDesc != null){
        throw SparkEngineExecuteException(requestPersistTask.getErrCode, "spark engine run sql failed:" + requestPersistTask.getErrDesc)
      }
      throw SparkEngineExecuteException(60001, "spark engine run sql failed")
    }
    info(s"$umUser finish to executeRealJob script:$sql")
    return op(jobExecuteResult)
  }

  def submitQuery(sql: String): JobExecuteResult = {
    val input = read[UJESJob](sql)
    var code = input.code
    val jobType = input.jobType
    val source = JavaConversions.mapAsJavaMap(input.source.asInstanceOf[Map[String, Any]])
    umUser = input.user
    val params = new util.HashMap[String, Any]()
    val configuration = new util.HashMap[String, Any]()
    val runtime = new util.HashMap[String, Any]()
    // updated cache related params
    if(StringUtils.isNotBlank(input.nodeName)){
      runtime.put(CSCommonUtils.NODE_NAME_STR, input.nodeName)
    }
    if(StringUtils.isNotBlank(input.contextId)){
      runtime.put(CSCommonUtils.CONTEXT_ID_STR, input.contextId)
    }
    runtime.put(TaskConstant.CACHE, input.cache.asInstanceOf[java.lang.Boolean])
    runtime.put(TaskConstant.CACHE_EXPIRE_AFTER, input.cacheExpireAfter.asInstanceOf[java.lang.Long])
    runtime.put(TaskConstant.READ_FROM_CACHE, input.readFromCache.asInstanceOf[java.lang.Boolean])
    runtime.put(TaskConstant.READ_CACHE_BEFORE, input.readCacheBefore.asInstanceOf[java.lang.Long])
    configuration.put(TaskConstant.PARAMS_CONFIGURATION_RUNTIME, runtime)
    params.put(TaskConstant.PARAMS_CONFIGURATION, configuration)
    //    if (jobType.equals(UJESJob.SQL_TYPE)) {
    //      code = SqlUtils.filterAnnotate(code)
    //      SqlUtils.checkSensitiveSql(code)
    //    }
    val builder = JobExecuteAction.builder()
      .setCreator(VisualisUtils.VG_CREATOR.getValue)
      .addExecuteCode(code)
      .setEngineTypeStr(input.engine)
      .setRunTypeStr(jobType)
      .setSource(source)
      .setUser(umUser)
      .setParams(params)

    val jobExecuteResult = linkisClient.execute(builder.build())
    jobExecuteResult
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

  private def querySQLWithJobExecuteResult(sql: String, limit: Int): JobExecuteResult = {
    executeUntil(sql, j => j)
  }


  override def execute(sql: String): Unit = {
    executeUntil(sql, _ => ())
  }

  override def query4List(sql: String, limit: Int): util.List[util.Map[String, AnyRef]] = {
    val jobExecuteResult = querySQLWithJobExecuteResult(sql, limit)
    val jobInfo = linkisClient.getJobInfo(jobExecuteResult)
    getResultSet(VisualisUtils.getResultSetPath(jobInfo))
  }

  override def submit4Exec(sql: String, pageNo: Int, pageSize: Int, totalCount: Int, limit: Int, excludeColumns: util.Set[String]): PaginateWithExecStatus = {
    val paginateWithQueryColumns = new PaginateWithExecStatus
    val jobExecuteResult = submitQuery(sql)
    paginateWithQueryColumns.setExecId(jobExecuteResult.getTaskID())
    paginateWithQueryColumns.setProgress(0.0f)
    return  paginateWithQueryColumns;
  }

  override def getProgress4Exec(execId: String, user: String): PaginateWithExecStatus = {
    val jobExecuteResult = LinkisClientExecutor.getJobExecuteResult(execId, user)
    val jobInfoResult = linkisClient.getJobInfo(jobExecuteResult)
    val paginateWithQueryColumns = new PaginateWithExecStatus
    paginateWithQueryColumns.setExecId(execId)
    paginateWithQueryColumns.setProgress(BigDecimal(jobInfoResult.getRequestPersistTask.getProgress).setScale(2, RoundingMode.HALF_UP).floatValue())
    paginateWithQueryColumns.setStatus(jobInfoResult.getRequestPersistTask.getStatus)
    return paginateWithQueryColumns
  }

  override def kill4Exec(execId: String, user: String): PaginateWithExecStatus = {
    val jobExecuteResult = LinkisClientExecutor.getJobExecuteResult(execId, user)
    var jobInfoResult = LinkisClientExecutor.jobInfoCache.getIfPresent(execId)
    if(null == jobInfoResult){
      jobInfoResult = linkisClient.getJobInfo(jobExecuteResult)
    }
    val status = SchedulerEventState.withName(jobInfoResult.getRequestPersistTask.getStatus)
    if(SchedulerEventState.isCompleted(status)){
      LinkisClientExecutor.saveJobExecuteResult(execId, jobInfoResult)
    } else {
      jobExecuteResult.setExecID(jobInfoResult.getTask.get("strongerExecId").toString)
      linkisClient.kill(jobExecuteResult)
    }
    val paginateWithQueryColumns = new PaginateWithExecStatus
    paginateWithQueryColumns.setExecId(execId)
    paginateWithQueryColumns.setProgress(BigDecimal(jobInfoResult.getRequestPersistTask.getProgress).setScale(2, RoundingMode.HALF_UP).floatValue())
    paginateWithQueryColumns.setStatus(jobInfoResult.getRequestPersistTask.getStatus)
    return paginateWithQueryColumns
  }

  override def getResultSet4Exec(execId: String, user: String): PaginateWithExecStatus = {
    val jobExecuteResult = LinkisClientExecutor.getJobExecuteResult(execId, user)
    var jobInfoResult = linkisClient.getJobInfo(jobExecuteResult)
    jobExecuteResult.setExecID(jobInfoResult.getRequestPersistTask.getExecId)
    var status = SchedulerEventState.withName(jobInfoResult.getRequestPersistTask.getStatus)
    while(!SchedulerEventState.isCompleted(status)) {
      Utils.sleepQuietly(500)
      jobInfoResult = linkisClient.getJobInfo(jobExecuteResult)
      status = SchedulerEventState.withName(jobInfoResult.getRequestPersistTask.getStatus)
    }
    val paginateWithQueryColumns = new PaginateWithExecStatus
    jobInfoResult = linkisClient.getJobInfo(jobExecuteResult)
    paginateWithQueryColumns.setExecId(execId)
    paginateWithQueryColumns.setProgress(BigDecimal(jobInfoResult.getRequestPersistTask.getProgress).setScale(2, RoundingMode.HALF_UP).floatValue())
    paginateWithQueryColumns.setStatus(jobInfoResult.getRequestPersistTask.getStatus)

    val jobInfo = linkisClient.getJobInfo(jobExecuteResult)
    val resultList = getResultSet(VisualisUtils.getResultSetPath(jobInfo))
    paginateWithQueryColumns.setResultList(resultList)
    paginateWithQueryColumns.setTotalCount(resultList.size())
    val columns = ResultHelper.getResultType(VisualisUtils.getResultSetPath(jobInfo))
    paginateWithQueryColumns.setColumns(columns.map(col => new QueryColumn(col.columnName,col.dataType.typeName)).toList)
    return paginateWithQueryColumns
  }

  override def query4Paginate(sql: String, pageNo: Int, pageSize: Int, totalCount: Int, limit: Int, excludeColumns: util.Set[String]): PaginateWithQueryColumns = {
    val paginateWithQueryColumns = new PaginateWithQueryColumns
    val jobExecuteResult = querySQLWithJobExecuteResult(sql, limit)
    val jobInfo = linkisClient.getJobInfo(jobExecuteResult)
    val resultList = getResultSet(VisualisUtils.getResultSetPath(jobInfo))
    paginateWithQueryColumns.setResultList(resultList)
    paginateWithQueryColumns.setTotalCount(resultList.size())

    val columns = ResultHelper.getResultType(VisualisUtils.getResultSetPath(jobInfo))
    paginateWithQueryColumns.setColumns(columns.map(col => new QueryColumn(col.columnName,col.dataType.typeName)).toList)
    return paginateWithQueryColumns
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
    *
    * @param sql
    * @return
    * @throws ServerException
    */
  override def getColumns(sql: String): util.List[QueryColumn] = {
    val jobExecuteResult = querySQLWithJobExecuteResult(sql, 2)
    val jobInfo = linkisClient.getJobInfo(jobExecuteResult)
    val columns = ResultHelper.getResultType(VisualisUtils.getResultSetPath(jobInfo))
    columns.map(col => new QueryColumn(col.columnName,col.dataType.typeName)).toList
  }

  override def testConnection(): Boolean = super.testConnection()

  override def jdbcTemplate(): JdbcTemplate = super.jdbcTemplate()

  override def executeBatch(sql: String, headers: util.Set[QueryColumn], datas: util.List[util.Map[String, AnyRef]]): Unit = super.executeBatch(sql, headers, datas)

}

object LinkisClientExecutor{
  val clientConfig = DWSClientConfigBuilder.newBuilder().addServerUrl(Configuration.getGateWayURL())
    .connectionTimeout(30000).discoveryEnabled(false)
    .maxConnectionSize(1000)
    .retryEnabled(false).readTimeout(30000)
    .setAuthenticationStrategy(new TokenAuthenticationStrategy()).setAuthTokenKey("dss-AUTH")
    .setAuthTokenValue("dss-AUTH").setDWSVersion("v1").build()
  val linkisClient = UJESClient(clientConfig)

  def getJobExecuteResult(taskId: String, user: String) = {
    val jobExecuteResult = new JobExecuteResult
    jobExecuteResult.setUser(user)
    jobExecuteResult.setTaskID(taskId)
    jobExecuteResult
  }

  val jobInfoCache = CacheBuilder
      .newBuilder
      .expireAfterWrite(5, TimeUnit.MINUTES)
      .maximumSize(5000)
      .build[String, JobInfoResult]

  def saveJobExecuteResult(execId: String, jobInfoResult: JobInfoResult) = {
    jobInfoCache.put(execId, jobInfoResult)
  }
}
