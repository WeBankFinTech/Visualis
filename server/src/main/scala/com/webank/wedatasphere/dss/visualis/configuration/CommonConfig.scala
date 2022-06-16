package com.webank.wedatasphere.dss.visualis.configuration

import org.apache.linkis.common.conf.CommonVars

object CommonConfig {
  /**
   * 接口越权检测
   * */
  val CHECK_PROJECT_USER = CommonVars("wds.dss.visualis.check.project.user", false)

  val ENGINE_DEFAULT_LIMIT = CommonVars("wds.dss.engine.default.limit", 5000)
  /**
   * this is the configuration to get the hive database source
   */
  val GATEWAY_IP = CommonVars("wds.dss.visualis.gateway.ip", "")

  val GATEWAY_PORT = CommonVars("wds.dss.visualis.gateway.port", "")

  val GATEWAY_PROTOCOL = CommonVars("wds.dss.visualis.gateway.protocol", "http://")

  val DB_URL_SUFFIX = CommonVars("wds.dss.visualis.database.url", "/api/rest_j/v1/datasource/dbs")

  /**
   * Linkis换成Apache版本后，更换了ticket id
   * key: wds.linkis.session.ticket.key
   * linkis_user_session_ticket_id_v1
   * */
  val TICKET_ID_STRING = CommonVars("wds.dss.visualis.ticketid", "linkis_user_session_ticket_id_v1")

  val TABLE_URL_SUFFIX = CommonVars("wds.dss.visualis.table.url", "/api/rest_j/v1/datasource/tables")

  val COLUMN_URL_SUFFIX = CommonVars("wds.dss.visualis.column.url", "/api/rest_j/v1/datasource/columns")

  val HIVE_DATASOURCE_URL = CommonVars("wds.dss.visualis.hive.datasource.url", "test")

  val HIVE_DATASOURCE_NAME = CommonVars("wds.dss.visualis.hive.datasource.name", "hive")

  val RESULT_SET_SCHEMA = CommonVars("wds.dss.visualis.result.set.schema", "hdfs://")

  val DEFAULT_PROJECT_NAME = CommonVars("wds.dss.visualis.default.project.name", "默认可视化项目")

  val EXPORT_PROJECT_DIR = CommonVars("wds.dss.visualis.export.project.dir", "/data/dss/dss/visualis-server/userfiles/export/")

  val JDBC_CACHE_FLUSH_WRITE = CommonVars("wds.dss.visualis.jdbc.cache.flush.write", 30L)

  val DEPLOY_ENV = CommonVars("wds.dss.visualis.deploy.env", "DEV")
  val ACCESS_ENV = CommonVars("wds.dss.visualis.access.env", "")

  val ENABLE_PASSWORD_ENCRYPT = CommonVars("wds.dss.visualis.enable.password.encrypt", false)

  val LINKIS_MYSQL_PUB_KEY = CommonVars("wds.linkis.mysql.pub.key", "MIIBIjANBgkqhki" + "G9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvBulc+/VDSKuwMUdtrZ1vYm9FU64E4l5EOQ5LozdRZoAxv1nlwEMKW5crGHBA" + "T3rmdOOEHow67r55zjXks6mMDyuU+y32TWsphR6haUMsRcfeBWp5h3csQBaaDT2di2pL+rxMXvhodAoI9U1bSf4U5q8mcJn" + "Cln1twOUky3BCS8VH95QawHYvTe+1NINL+aJG3W4g9JfEwoFPnDOQHGFryotNMs1zZBt3PDyNsMrPloBVLFVUAT7RpmXkEmjfpqfzDvdO4F" + "9MSBan6sk2jQyWJUg4FKXgsqeXUz+OYpbNW8Dw0Q5E5JDGMtrx1kzX8mVooheoS7SpiPJsgi26FPSEwIDAQAB")

  val LINKIS_MYSQL_PRIV_KEY = CommonVars("wds.linkis.mysql.pri.key", "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC8G6Vz7" + "9UNIq7AxR22tnW9ib0VTrgTiXkQ5DkujN1FmgDG/WeXAQwpblysYcEBPeuZ044QejDruvnnONeS" + "zqYwPK5T7LfZNaymFHqFpQyxFx94FanmHdyxAFpoNPZ2Lakv6vExe+Gh0Cgj1TVtJ/hTmryZwmcKWfW3A5STLcEJ" + "LxUf3lBrAdi9N77U0g0v5okbdbiD0l8TCgU+cM5AcYWvKi00yzXNkG3c8PI2wys+WgFUsVVQBPtGmZeQSaN+mp/MO907gX" + "0xIFqfqyTaNDJYlSDgUpeCyp5dTP45ils1bwPDRDkTkkMYy2vHWTNfyZWiiF6hLtKmI8myCLboU9ITAgMBAAECggEANxpqJ" + "0I0SPrF8lZL1AAzEWjN6PX8WkzFGDuivI4rK35nh+Mne0alR2W65Axmu3RmFdOxJAaHWiaVmjQ+ghTi/fJoptELMifVAXmyQoAM7bt" + "2TnkaIfzRb1BJK4mIQSozC4RpTzOY7wvJFmYYlndE+Ui0wt39zTx5DDmSRmL6zzNoTG5pPgmN/zq2icbhXqD0DP8wxw4AFKJWdrc" + "MkkjRfKkByqA03bymfQqIz5uHril60o3xuuTyBPR74bnPdJE4ONahQHIgvWj/aQYqNyaapJJ8C194Acin0hl1QRv30+syM95QBdLEbLPAU" + "Ho2ClWRJZ6cqDPZe2a2N5YpbXtIQQKBgQD0iremr8O42yENqfAXK06Cek30E6wHTJ/RYipcveVg34rXsJ+yTls7SV6tyCqNknZzMoRw" + "QrIs74TN+KDF52wudrTpbsf5IKJQyExOz7LxTJ76h2OVA9zM1/MPtOHLt5mnwhLTtmxhVZ54CXbkw237pSagG+HhLyrO8S4mIwe" + "H8QKBgQDE7AEClojuj5cwRH46ic2s/oIuBObNFeJcRvxx+ONNdlOWOKRi6FhfHlhzLoFDUci2bjn1fvP1EMYZ+KkXATyezgIjJ" + "nnClXFpsORhUWh0SiqS3gVJeSIEDKeuh9esRPXk/cyPa3V8o5HouWWDitday0Xsnw51/sVTbN3b5z0eQwKBgEys9hqgv+jFZJ7JKwv" + "Iu2wz9x9Rz73WK8JWWlwL+tEeJoWsztX0taxoO/SXb6hGRTennlkowH9Qdr6yd462GniTJfSPlMorjllwBGUtwLjiQnLhYrsFpATi" + "rUa+e5IJtncgZhDWATOfyflvVkUyddjSlsLbGz8lL/IFM2gn0aOxAoGADJlQ4zqAXkr/kE4BiXtBlnzeFVWo8pwg1GiSRDR5Tn5wkJ7" + "lHZLh/IvzesMR8B2uasWYnbVWpGpDUmwPXXJtz3c8ucT/a0ymae2wXu2XckFAgg8EZZQDciDhJZB5YwMyfEkkqlRkuum4LxyVexo" + "J9zwkKCRxB2madGD1vNkJlwMCgYANF7fiG6k3D45Nopu8iTbi3S9oOnhTWxpwxSJWTUij4HFmtSXjJfgOPG9rVvO5QCaHWDWHE/LyZ" + "/Y51ustAV3uj5UmnGwXQDNEgNZUFe4vwzYq7ikXoE6zCTzs70DT/4llos5g1rs8feuWrJK19DPKrenxyOLI6pPA9GjMgC1aEg==")

  val ENABLE_JDBC_WHITELIST = CommonVars[Boolean]("wds.dss.visualis.enable.jdbc.whitelist", false)

  val JDBC_WHITELIST = CommonVars("wds.dss.visualis.jdbc.whitelist", "")

  val JDBC_ENCRYPT_PARAMETER = CommonVars("wds.dss.visualis.jdbc.encrypt.parameter", "encrypt=true")
}
