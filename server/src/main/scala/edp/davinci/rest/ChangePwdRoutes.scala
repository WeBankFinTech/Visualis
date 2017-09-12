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

package edp.davinci.rest

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule}
import edp.davinci.persistence.entities.User
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.ResponseUtils._
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._
import org.apache.log4j.Logger
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

@Api(value = "/changepwd", consumes = "application/json")
@Path("/changepwd")
class ChangePwdRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule) extends Directives {

  val routes: Route = changeLoginPwdRoute ~ changeUserPwdRoute
  private lazy val logger = Logger.getLogger(this.getClass)

  @Path("/login")
  @ApiOperation(value = "change login user's pwd", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "changePwd", value = "change pwd information", required = true, dataType = "edp.davinci.rest.ChangePwdClass", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 404, message = "user not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 402, message = "unspecified error")
  ))
  def changeLoginPwdRoute: Route = path("changepwd" / "login") {
    post {
      entity(as[ChangePwdClass]) { changePwd =>
        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
          session =>
            onComplete(modules.userDal.findById(session.userId).mapTo[Option[User]]) {
              case Success(userOpt) => userOpt match {
                case Some(user) =>
                  if (user.password == changePwd.oldPass) {
                    onComplete(modules.userDal.update(updatePass(user, changePwd.newPass)).mapTo[Int]) {
                      case Success(r) => complete(OK, ResponseJson[Int](getHeader(200, session),r))
                      case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session),""))
                    }
                  } else complete(BadRequest, ResponseJson[String](getHeader(400, "old pwd is wrong", session),""))
                case None => complete(BadRequest, ResponseJson[String](getHeader(400, session),""))
              }
              case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session),""))
            }
        }
      }
    }
  }

  @Path("/users")
  @ApiOperation(value = "change user's pwd", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "changePwd", value = "change pwd information", required = true, dataType = "edp.davinci.rest.ChangeUserPwdClass", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 404, message = "user not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 402, message = "unspecified error"),
    new ApiResponse(code = 403, message = "internal service error")))
  def changeUserPwdRoute: Route = path("changepwd" / "users") {
    post {
      entity(as[ChangeUserPwdClass]) { changePwd =>
        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
          session =>
            onComplete(modules.userDal.findById(changePwd.id).mapTo[Option[User]]) {
              case Success(userOpt) => userOpt match {
                case Some(user) =>
                  if (user.password == changePwd.oldPass) {
                    onComplete(modules.userDal.update(updatePass(user, changePwd.newPass)).mapTo[Int]) {
                      case Success(r) => complete(OK, ResponseJson[Int](getHeader(200, session),r))
                      case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session),""))
                    }
                  } else complete(BadRequest, ResponseJson[String](getHeader(400, "old pwd is wrong", session),""))
                case None => complete(BadRequest, ResponseJson[String](getHeader(400, "not found",session),""))
              }
              case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session),""))
            }
        }
      }
    }

  }

  private def updatePass(user: User, pwd: String): User = {
    User(user.id, user.email, pwd, user.title, user.name, user.admin,
      user.active, user.create_time, user.create_by, user.update_time, user.update_by)
  }

}
