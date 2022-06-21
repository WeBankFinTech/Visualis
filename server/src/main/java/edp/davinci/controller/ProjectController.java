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

package edp.davinci.controller;

import com.github.pagehelper.PageInfo;
import edp.core.annotation.CurrentUser;
import edp.core.annotation.MethodLog;
import edp.davinci.common.controller.BaseController;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.dto.organizationDto.OrganizationTransfer;
import edp.davinci.dto.projectDto.*;
import edp.davinci.dto.roleDto.RoleBaseInfo;
import edp.davinci.dto.roleDto.RoleProject;
import edp.davinci.dto.roleDto.RoleWithProjectPermission;
import edp.davinci.model.User;
import edp.davinci.service.ProjectService;
import edp.davinci.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = Constants.BASE_API_PATH + "/projects", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectController extends BaseController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RoleService roleService;


    /**
     * 获取项目列表：用户创建和用户所在组可访问的
     *
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping
    public ResponseEntity getProjects(@CurrentUser User user, HttpServletRequest request) {
        List<ProjectInfo> projects = projectService.getProjects(user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(projects));
    }

    @MethodLog
    @GetMapping("/{id}/roles")
    public ResponseEntity getRolesOfProject(@CurrentUser User user,
                                            @PathVariable Long id,
                                            HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        List<RoleBaseInfo> list = roleService.getRolesByProjectId(id, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(list));
    }

    @MethodLog
    @GetMapping("/{id}/roles/{roleId}")
    public ResponseEntity getRoleOfProject(@CurrentUser User user,
                                           @PathVariable Long id,
                                           @PathVariable Long roleId,
                                           HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        RoleWithProjectPermission permission = roleService.getRoleByProject(id, roleId, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(permission));
    }


    /**
     * 获取项目列表：用户创建和用户所在组可访问的
     *
     * @param id
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/{id}")
    public ResponseEntity getProjectInfo(@PathVariable Long id,
                                         @CurrentUser User user,
                                         HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(projectService.getProjectInfo(id, user)));
    }

    /**
     * 获取项目列表：用户创建和用户所在组可访问的
     *
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/{id}/admins")
    public ResponseEntity getAdmins(@PathVariable Long id,
                                    @CurrentUser User user,
                                    HttpServletRequest request) {
        List<RelProjectAdminDto> admins = projectService.getAdmins(id, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(admins));
    }

    @MethodLog
    @GetMapping("/search")
    public ResponseEntity searchProjects(@RequestParam(value = "keywords", required = false) String keywords,
                                         @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                         @CurrentUser User user,
                                         HttpServletRequest request) {

        PageInfo<ProjectWithCreateBy> pageInfo = projectService.searchProjects(keywords, user, pageNum, pageSize);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(pageInfo));
    }

    /**
     * 创建项目
     *
     * @param projectCreat
     * @param user
     * @param bindingResult
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createProject(@Valid @RequestBody ProjectCreat projectCreat,
                                        BindingResult bindingResult,
                                        @CurrentUser User user,
                                        HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        ProjectInfo project = projectService.createProject(projectCreat, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(project));
    }

    /**
     * 移交项目
     *
     * @param id
     * @param organizationTransfer
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PutMapping(value = "/{id}/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity transferProject(@PathVariable Long id,
                                          @Valid @RequestBody OrganizationTransfer organizationTransfer,
                                          BindingResult bindingResult,
                                          @CurrentUser User user,
                                          HttpServletRequest request) {


        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        projectService.transferPeoject(id, organizationTransfer.getOrgId(), user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }

    /**
     * 删除project
     *
     * @param id
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping("/{id}")
    public ResponseEntity deleteProject(@PathVariable Long id,
                                        @CurrentUser User user,
                                        HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }
        // 把任务进行归档，不进行真实删除
        projectService.setProjectToArchive(id, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 更新项目基本信息
     *
     * @param id
     * @param projectUpdate
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateProjectBaseInfo(@PathVariable Long id,
                                                @Valid @RequestBody ProjectUpdate projectUpdate,
                                                BindingResult bindingResult,
                                                @CurrentUser User user,
                                                HttpServletRequest request) {

        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        projectService.updateProject(id, projectUpdate, user);

        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 收藏project
     *
     * @param id
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(value = "/favorite/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity favoriteProject(@PathVariable Long id,
                                          @CurrentUser User user,
                                          HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        projectService.favoriteProject(id, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 获取已收藏project
     *
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping(value = "/favorites")
    public ResponseEntity getFavoriteProjects(@CurrentUser User user,
                                              HttpServletRequest request) {
        List<ProjectInfo> favoriteProjects = projectService.getFavoriteProjects(user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(favoriteProjects));
    }

    /**
     * 获取已收藏project
     *
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @DeleteMapping(value = "/remove/favorites")
    public ResponseEntity removeFavoriteProjects(@CurrentUser User user,
                                                 @RequestBody Long[] projectIds,
                                                 HttpServletRequest request) {
        for (Long id : projectIds) {
            if (invalidId(id)) {
                ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
                return ResponseEntity.status(resultMap.getCode()).body(resultMap);
            }
        }

        projectService.removeFavoriteProjects(user, projectIds);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }

    /**
     * 为project 添加管理员
     *
     * @param id
     * @param adminIds
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(value = "/{id}/admins")
    public ResponseEntity addProjectAdmin(@PathVariable Long id,
                                          @RequestBody Long[] adminIds,
                                          @CurrentUser User user,
                                          HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (adminIds.length == 0) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid admin ids");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        List<RelProjectAdminDto> list = projectService.addAdmins(id, Arrays.asList(adminIds), user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(list));
    }

    /**
     * 删除project admin
     *
     * @param id
     * @param relationId
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @DeleteMapping(value = "/{id}/admin/{relationId}")
    public ResponseEntity removeProjectAdmin(@PathVariable Long id,
                                             @PathVariable Long relationId,
                                             @CurrentUser User user,
                                             HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (invalidId(relationId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid admin id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        projectService.removeAdmin(relationId, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 批量添加role
     *
     * @param id
     * @param roleIds
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(value = "/{id}/roles", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addRoles(@PathVariable Long id,
                                   @RequestBody Long[] roleIds,
                                   @CurrentUser User user,
                                   HttpServletRequest request) {

        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        List<RoleProject> list = projectService.postRoles(id, Arrays.asList(roleIds), user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(list));
    }
}
