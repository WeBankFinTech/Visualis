/*-
 * <<
 * Davinci
 * ==
 * Copyright (C) 2016 - 2017 EDP
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package edp.davinci

import akka.http.scaladsl.model.{ContentTypes, HttpCharsets, MediaTypes}

object DavinciConstants extends DavinciConstants with SeparatorConstants with ContentType


trait DavinciConstants {
  lazy val flatTable = "flattable"
  lazy val defaultEncode = "UTF-8"
  lazy val groupVar = "group@var"
  lazy val queryVar = "query@var"
  lazy val updateVar = "update@var"
}


trait SeparatorConstants {
  lazy val conditionSeparator = ","
  lazy val sqlSeparator = ";"
  lazy val sqlUrlSeparator = "&"
  lazy val CSVHeaderSeparator = ':'
  lazy val delimiterStartChar = '<'
  lazy val delimiterEndChar = '>'
  lazy val assignmentChar = '='
  lazy val dollarDelimiter = '$'
  lazy val STStartChar = '{'
  lazy val STEndChar = '}'
}

trait ContentType {
  lazy val textHtml = MediaTypes.`text/html` withCharset HttpCharsets.`UTF-8`
  lazy val textCSV = MediaTypes.`text/csv` withCharset HttpCharsets.`UTF-8`
  lazy val appJson = ContentTypes.`application/json`
}
