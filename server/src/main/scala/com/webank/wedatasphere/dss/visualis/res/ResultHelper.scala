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
package com.webank.wedatasphere.dss.visualis.res

import java.util

import com.webank.wedatasphere.linkis.storage.domain._
import com.webank.wedatasphere.linkis.storage.resultset.table.TableMetaData
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetReader}
import com.webank.wedatasphere.dss.visualis.exception.VGErrorException
import com.webank.wedatasphere.linkis.adapt.LinkisUtils
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import org.json4s.DefaultFormats

/**
  * Created by johnnwang on 2019/1/22.
  */
object ResultHelper {
  implicit val formats = DefaultFormats
  def getResultByPath(path:String,limit:Long)={
    val resPath = new FsPath(path)
    val rsFactory = ResultSetFactory.getInstance
    val resultSet = rsFactory.getResultSetByPath(resPath)
    if(ResultSetFactory.TABLE_TYPE != resultSet.resultSetType()){

    }
  }


  @scala.throws[VGErrorException]
  def getResultType(path:String):Array[Column]={
    /*val resPath = new FsPath(path)
    val rsFactory = ResultSetFactory.getInstance
    val resultSet = rsFactory.getResultSetByPath(resPath)
    if(ResultSetFactory.TABLE_TYPE != resultSet.resultSetType()){
        throw new VGErrorException(70001,"不支持不是表格的结果集")
    }
    val fs = FSFactory.getFs(resPath)
    fs.init(null)
    val reader = ResultSetReader.getResultSetReader(resultSet,fs.read(resPath))*/
    val reader = ResultSetReader.getTableResultReader(path)
    val metaData = reader.getMetaData.asInstanceOf[TableMetaData]
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

}
