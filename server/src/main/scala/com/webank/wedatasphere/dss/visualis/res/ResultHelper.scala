package com.webank.wedatasphere.dss.visualis.res

import java.util
import com.webank.wedatasphere.dss.visualis.configuration.CommonConfig
import org.apache.linkis.storage.domain._
import org.apache.linkis.storage.resultset.table.TableMetaData
import org.apache.linkis.storage.resultset.{ResultSetFactory, ResultSetReader}
import com.webank.wedatasphere.dss.visualis.exception.VGErrorException
import org.apache.linkis.adapt.LinkisUtils
import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.server.BDPJettyServerHelper
import org.apache.linkis.storage.FSFactory
import org.json4s.DefaultFormats

/**
  * Created by johnnwang on 2019/1/22.
  */
object ResultHelper {
  implicit val formats = DefaultFormats
  def getResultByPath(path:String,limit:Long)={
    val resPath = new FsPath(getSchemaPath(path))
    val rsFactory = ResultSetFactory.getInstance
    val resultSet = rsFactory.getResultSetByPath(resPath)
    if(ResultSetFactory.TABLE_TYPE != resultSet.resultSetType()){

    }
  }

  def getSchemaPath(path: String): String = {
    if(path.startsWith(CommonConfig.RESULT_SET_SCHEMA.getValue)){
      path
    } else {
      CommonConfig.RESULT_SET_SCHEMA.getValue + path
    }
  }


  @scala.throws[VGErrorException]
  def getResultType(path:String):Array[Column]={
    val resPath = new FsPath(path)
    val rsFactory = ResultSetFactory.getInstance
    val resultSet = rsFactory.getResultSetByPath(resPath)
    if(ResultSetFactory.TABLE_TYPE != resultSet.resultSetType()){
      throw new VGErrorException(70001,"不支持不是表格的结果集")
    }
    val fs = FSFactory.getFs(resPath)
    fs.init(null)
    val reader = ResultSetReader.getResultSetReader(resultSet,fs.read(resPath))
    val metaData = reader.getMetaData.asInstanceOf[TableMetaData]
    Utils.tryQuietly(reader.close())
    Utils.tryQuietly(fs.close())
    metaData.columns
  }

  def toModelItem(path:String):String ={
    val columns = getResultType(path)
    val res = new util.HashMap[String,ModelItem]()
    columns.foreach{column =>
      val visualType = toVisualType(column.dataType)
      val modelType = if(visualType != NUMBER_TYPE) "category" else "value"
      val modelItem = ModelItem(column.dataType.typeName.toUpperCase,visualType,modelType)
      res.put(column.columnName,modelItem)
    }
    LinkisUtils.gson.toJson(res)
  }

  val NUMBER_TYPE = "number"

  def toVisualType(dataType: DataType): String = dataType match {
    case ShortIntType | IntType | LongType | FloatType | DoubleType | DecimalType | BinaryType => NUMBER_TYPE
    case DateType | TimestampType => "date"
    case _ => "string"
  }

  def toVisualType(sqlType: String): String = sqlType match {
    case "TINYINT" | "SMALLINT" | "MEDIUMINT" | "INT" | "INTEGER" | "BIGINT" | "FLOAT" | "DOUBLE" | "DOUBLE PRECISION" | "REAL" | "DECIMAL" | "BIT" | "SERIAL" | "BOOL" | "BOOLEAN" | "DEC" | "FIXED" | "NUMERIC" => NUMBER_TYPE
    case "DATE" | "DATETIME" | "TIMESTAMP" | "TIME" | "YEAR"  => "date"
    case _ => "string"
  }



}
