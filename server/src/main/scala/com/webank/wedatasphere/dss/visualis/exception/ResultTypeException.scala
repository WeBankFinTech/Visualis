package com.webank.wedatasphere.dss.visualis.exception

import org.apache.linkis.common.exception.ErrorException

/**
  * Created by allenlliu on 2019/1/24.
  */
class ResultTypeException (errCode: Int, desc: String) extends ErrorException(errCode, desc) {

}


case class SparkEngineExecuteException(errCode:Int, desc:String) extends ErrorException(errCode, desc)