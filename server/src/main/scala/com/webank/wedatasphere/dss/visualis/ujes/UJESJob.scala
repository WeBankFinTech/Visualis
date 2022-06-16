package com.webank.wedatasphere.dss.visualis.ujes

import com.webank.wedatasphere.dss.visualis.utils.VisualisUtils


case class UJESJob(var code:String,
                   var user:String,
                   var jobType:String,
                   var source:Any,
                   var creator: String = VisualisUtils.VG_CREATOR.getValue,
                   var engine: String = VisualisUtils.SPARK.getValue,
                   var nodeName: String = "",
                   var contextId: String = "",
                   var cache: Boolean = false,
                   var cacheExpireAfter : Long = 300L,
                   var readFromCache: Boolean = false,
                   var readCacheBefore: Long = 300L
                  )

object UJESJob{
  val SQL_TYPE = "sql"
  val PSQL_TYPE = "psql"
  val SCALA_TYPE ="scala"
  val SPARK_ENGINE ="spark"
  val PRESTO_ENGINE ="presto"
  val CONTEXT_ID ="contextId"

  def apply(code: String,
            user: String,
            jobType: String,
            source: Any): UJESJob = new UJESJob(code, user, jobType, source)
  def apply(code: String,
            user: String,
            jobType: String,
            source: Any,
            cache: Boolean,
            cacheExpireAfter : Long,
            readFromCache: Boolean,
            readCacheBefore: Long): UJESJob = {
    val ujesJob = new UJESJob(code, user, jobType, source)
    ujesJob.cache = cache
    ujesJob.cacheExpireAfter = cacheExpireAfter
    ujesJob.readFromCache = readFromCache
    ujesJob.readCacheBefore = readCacheBefore
    ujesJob
  }
}
