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

package edp.davinci.rest.group

import edp.davinci.ModuleInstance
import edp.davinci.module.DbModule._
import edp.davinci.persistence.entities.PutGroupInfo
import edp.davinci.rest.SessionClass
import edp.davinci.util.ResponseUtils
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

object GroupService extends GroupService

trait GroupService {
  private lazy val modules = ModuleInstance.getModule

  def getAll(session: SessionClass): Future[Seq[(Long, String, Option[String])]] = {
    if (session.admin)
      db.run(modules.groupQuery.map(r => (r.id, r.name, r.desc)).result)
    else
      db.run(modules.groupQuery.filter(g => g.id inSet session.groupIdList).map(r => (r.id, r.name, r.desc)).result)
  }

  def update(groupSeq: Seq[PutGroupInfo], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(groupSeq.map(r => {
      modules.groupQuery.filter(_.id === r.id).map(group => (group.name, group.desc, group.update_by, group.update_time)).update(r.name, Some(r.desc), session.userId, ResponseUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def deleteGroup(groupId: Long): Future[Int] = {
    modules.groupDal.deleteById(groupId)
  }

  def deleteRelGF(groupId: Long): Future[Int] = {
    modules.relGroupViewDal.deleteByFilter(_.group_id === groupId)
  }

  def deleteRelGU(groupId: Long): Future[Int] = {
    modules.relUserGroupDal.deleteByFilter(_.group_id === groupId)
  }

}
