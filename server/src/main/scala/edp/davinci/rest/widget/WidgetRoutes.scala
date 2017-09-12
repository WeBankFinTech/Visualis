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

package edp.davinci.rest.widget

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.persistence.entities.{PostWidgetInfo, PutWidgetInfo, Widget}
import edp.davinci.rest._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.JsonProtocol._
import edp.davinci.util.ResponseUtils._
import io.swagger.annotations._
import org.apache.log4j.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@Api(value = "/widgets", consumes = "application/json", produces = "application/json")
@Path("/widgets")
class WidgetRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {
  val routes: Route = getAllWidgetsRoute ~ postWidgetRoute ~ deleteWidgetByIdRoute ~ putWidgetRoute ~ getWholeSqlByWidgetIdRoute
  private lazy val logger = Logger.getLogger(this.getClass)
  private lazy val routeName = "widgets"

  @ApiOperation(value = "list all widgets", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "active", value = "true or false", required = false, dataType = "boolean", paramType = "query")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "widgets not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getAllWidgetsRoute: Route = path(routeName) {
    get {
      parameter('active.as[Boolean].?) { active =>
        authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
          session => getAllWidgetsComplete(session, active.getOrElse(true))
        }
      }
    }
  }

  private def getAllWidgetsComplete(session: SessionClass, active: Boolean): Route = {
    onComplete(WidgetService.getAll(session)) {
      case Success(widgetSeq) =>
        val responseSeq: Seq[PutWidgetInfo] = widgetSeq.map(r => PutWidgetInfo(r._1, r._2, r._3, r._4, r._5.getOrElse(""), r._6, r._7, r._8, r._9))
        complete(OK, ResponseJson[Seq[PutWidgetInfo]](getHeader(200, session), responseSeq))
      case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
    }
  }

  @ApiOperation(value = "Add a new widget to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widget", value = "Widget object to be added", required = true, dataType = "edp.davinci.rest.PostWidgetInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "widgets not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def postWidgetRoute: Route = path(routeName) {
    post {
      entity(as[PostWidgetInfoSeq]) {
        widgetSeq =>
          authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
            session => postWidgetComplete(session, widgetSeq.payload)
          }
      }
    }
  }

  private def postWidgetComplete(session: SessionClass, postWidgetSeq: Seq[PostWidgetInfo]): Route = {
    if (session.admin) {
      val widgetSeq = postWidgetSeq.map(post => Widget(0, post.widgetlib_id, post.flatTable_id, post.name, Some(post.adhoc_sql), post.desc, post.chart_params, post.query_params, post.publish, active = true, currentTime, session.userId, currentTime, session.userId))
      onComplete(modules.widgetDal.insert(widgetSeq)) {
        case Success(widgets) =>
          val putWidgets = widgets.map(w => PutWidgetInfo(w.id, w.widgetlib_id, w.flatTable_id, w.name, w.adhoc_sql.getOrElse(""), w.desc, w.chart_params, w.query_params, w.publish, Some(w.active)))
          complete(OK, ResponseSeqJson[PutWidgetInfo](getHeader(200, session), putWidgets))
        case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
      }
    } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
  }


  @ApiOperation(value = "update widgets in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widget", value = "Widget object to be updated", required = true, dataType = "edp.davinci.rest.PutWidgetInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "widgets not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def putWidgetRoute: Route = path(routeName) {
    put {
      entity(as[PutWidgetInfoSeq]) {
        widgetSeq =>
          authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
            session => putWidgetComplete(session, widgetSeq.payload)
          }
      }
    }
  }

  private def putWidgetComplete(session: SessionClass, putWidgetSeq: Seq[PutWidgetInfo]): Route = {
    if (session.admin) {
      val future = WidgetService.update(putWidgetSeq, session)
      onComplete(future) {
        case Success(_) => complete(OK, ResponseJson[String](getHeader(200, session), ""))
        case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
      }
    } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
  }

  @Path("/{widget_id}")
  @ApiOperation(value = "delete widget by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widget_id", value = "widget id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def deleteWidgetByIdRoute: Route = path(routeName / LongNumber) {
    widgetId =>
      delete {
        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
          session =>
            if (session.admin) {
              val operation = for {
                user <- WidgetService.deleteWidget(widgetId)
                relGU <- WidgetService.deleteRelDW(widgetId)
              } yield (user, relGU)
              onComplete(operation) {
                case Success(_) => complete(OK, ResponseJson[String](getHeader(200, session), ""))
                case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
              }
            } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
        }
      }
  }

  @Path("/{widget_id}/sqls")
  @ApiOperation(value = "get whole sql by widget id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widget_id", value = "widget id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getWholeSqlByWidgetIdRoute: Route = path(routeName / LongNumber / "sqls") { widgetId =>
    get {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session => getWholeSqlComplete(session, widgetId)
      }
    }

  }

  private def getWholeSqlComplete(session: SessionClass, widgetId: Long): Route = {
    onComplete(WidgetService.getSql(widgetId)) {
      case Success(sqlSeq) =>
        val resultSql = formatSql(sqlSeq.head)
        complete(OK, ResponseJson[SqlInfo](getHeader(200, session), SqlInfo(resultSql)))
      case Failure(ex) => complete(InternalServerError, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
    }
  }

  def formatSql(sqlInfo: (String, String, String)): Array[String] = {
    val (olapSql, sqlTmpl, _) = sqlInfo
    (sqlTmpl + ";" + olapSql).split(";")
  }


}
