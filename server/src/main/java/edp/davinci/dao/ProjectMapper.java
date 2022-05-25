/*
 * <<
 *  Davinci
 *  ==
 *  Copyright (C) 2016 - 2019 EDP
 *  ==
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  >>
 *
 */

package edp.davinci.dao;

import edp.davinci.dto.organizationDto.OrganizationInfo;
import edp.davinci.dto.projectDto.ProjectDetail;
import edp.davinci.dto.projectDto.ProjectWithCreateBy;
import edp.davinci.model.Project;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public interface ProjectMapper {


    List<ProjectWithCreateBy> getProejctsByUser(@Param("userId") Long userId);

    List<ProjectWithCreateBy> getFavoriteProjects(@Param("userId") Long userId);

    List<ProjectWithCreateBy> getProjectsByOrgWithUser(@Param("orgId") Long orgId, @Param("userId") Long userId, @Param("keyword") String keyword);

    List<ProjectWithCreateBy> getProjectsByKewordsWithUser(@Param("keywords") String keywords, @Param("userId") Long userId, @Param("orgList") List<OrganizationInfo> list);

    @Select("select user_id from visualis_project p where id = #{projectId}")
    Integer getProjectUserId(@Param("projectId") Long projectId);

    @Select({"select id from visualis_project where org_id = #{orgId} and `name` = #{name}"})
    Long getByNameWithOrgId(@Param("name") String name, @Param("orgId") Long orgId);

    int insert(Project project);

    @Select({"select * from visualis_project where id = #{id}"})
    Project getById(@Param("id") Long id);

    ProjectDetail getProjectDetail(@Param("id") Long id);

    @Select({"select * from visualis_project where id = #{id} and user_id = #{userId}"})
    Project getByProject(Project project);

    @Update({"update visualis_project set `name` = #{name}, description = #{description}, visibility = #{visibility}, update_time = #{updateTime}, update_by = #{updateBy}  where id = #{id}"})
    int updateBaseInfo(Project project);

    @Update({"update visualis_project set `org_id` = #{orgId} where id = #{id}"})
    int changeOrganization(Project project);


    @Update({"update visualis_project set `is_transfer` = #{isTransfer, jdbcType=TINYINT} where id = #{id}"})
    int changeTransferStatus(@Param("isTransfer") Boolean isTransfer, @Param("id") Long id);

    @Delete({"delete from visualis_project where id = #{id}"})
    int deleteById(@Param("id") Long id);

    @Select({"select * from visualis_project where org_id = #{orgId}"})
    List<Project> getByOrgId(@Param("orgId") Long orgId);

    @Select({"SELECT p.* FROM visualis_project p INNER JOIN display d on p.id = d.project_id where d.id = #{displayId}"})
    Project getByDisplayId(@Param("displayId") Long displayId);


    @Update({"update visualis_project set star_num = star_num + 1 where id = #{id}"})
    int starNumAdd(@Param("id") Long id);


    @Update({"update visualis_project set star_num = IF(star_num > 0,star_num - 1, 0) where id = #{id}"})
    int starNumReduce(@Param("id") Long id);

    Set<Long> getProjectIdsByAdmin(@Param("userId") Long userId);

    int deleteBeforOrgRole(@Param("projectId") Long projectId, @Param("orgId") Long orgId);

    @Select({
            "select * from visualis_project p",
            "WHERE  p.user_id= #{userId} AND p.name = #{name}"
    })
    List<Project> getProjectByNameWithUserId(@Param("name") String name, @Param("userId") Long userId);

    @Select("select `project_id` from widget where `id` = #{widgetId}")
    Long getProjectIdByWidgetId(@Param("widgetId") Long widgetId);


    @Select("select `project_id` from display where id = #{displayId}")
    Long getProjectByDisplayId(@Param("displayId") Long displayId);


    @Select("select project_id from dashboard_portal where id = #{dashboardId}")
    Long getProjectIdByDashboardId(@Param("dashboardId") Long dashboardId);

    @Select("select project_id from view where id = #{viewId}")
    Long getProjectIdByViewId(@Param("viewId") Long viewId);

    @Select("select * from visualis_project where `name` = #{keywords} limit 1")
    Project getProjectByName(@Param("keywords") String keywords);
}