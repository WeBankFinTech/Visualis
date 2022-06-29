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


import com.webank.wedatasphere.dss.visualis.auth.ProjectAuth;
import edp.core.annotation.CurrentUser;
import edp.core.annotation.MethodLog;
import edp.davinci.common.controller.BaseController;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.dto.dashboardDto.*;
import edp.davinci.model.Dashboard;
import edp.davinci.model.DashboardPortal;
import edp.davinci.model.MemDashboardWidget;
import edp.davinci.model.User;
import edp.davinci.service.DashboardPortalService;
import edp.davinci.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = Constants.BASE_API_PATH + "/dashboardPortals", produces = MediaType.APPLICATION_JSON_VALUE)
public class DashboardController extends BaseController {

    @Autowired
    private DashboardPortalService dashboardPortalService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ProjectAuth projectAuth;

    /**
     * 获取dashboardPortal列表
     *
     * @param projectId
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping
    public ResponseEntity getDashboardPortals(@RequestParam Long projectId,
                                              @CurrentUser User user,
                                              HttpServletRequest request) {
        if (invalidId(projectId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }
        List<DashboardPortal> dashboardPortals = dashboardPortalService.getDashboardPortals(projectId, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(dashboardPortals));
    }


    /**
     * 获取dashboard列表
     *
     * @param id
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/{id}/dashboards")
    public ResponseEntity getDashboards(@PathVariable Long id,
                                        @CurrentUser User user,
                                        HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        List<Dashboard> dashboards = dashboardService.getDashboards(id, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(dashboards));
    }


    /**
     * 获取Dashboard 排除访问的团队列表
     *
     * @param id
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/dashboard/{id}/exclude/roles")
    public ResponseEntity getDashboardExcludeRoles(@PathVariable Long id,
                                                   HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(dashboardService.getExcludeRoles(id)));
    }


    /**
     * 获取Dashboardportal 排除访问的团队列表
     *
     * @param id
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/{id}/exclude/roles")
    public ResponseEntity getPortalExcludeRoles(@PathVariable Long id,
                                                HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(dashboardPortalService.getExcludeRoles(id)));
    }


    /**
     * 获取dashboard下widgets关联信息列表
     *
     * @param portalId
     * @param dashboardId
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/{portalId}/dashboards/{dashboardId}")
    public ResponseEntity getDashboardMemWidgets(@PathVariable("portalId") Long portalId,
                                                 @PathVariable("dashboardId") Long dashboardId,
                                                 @CurrentUser User user,
                                                 HttpServletRequest request) {
        if (invalidId(portalId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid dashboard portal id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (invalidId(dashboardId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid dashboard id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        DashboardWithMem dashboardMemWidgets = dashboardService.getDashboardMemWidgets(portalId, dashboardId, user);

        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(dashboardMemWidgets));
    }

    /**
     * 新建dashboardPortal
     *
     * @param dashboardPortal
     * @param bindingResult
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createDashboardPortal(@Valid @RequestBody DashboardPortalCreate dashboardPortal,
                                                BindingResult bindingResult,
                                                @CurrentUser User user,
                                                HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if(!projectAuth.isPorjectOwner(dashboardPortal.getProjectId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DashboardPortal portal = dashboardPortalService.createDashboardPortal(dashboardPortal, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(portal));
    }


    /**
     * 更新dashboardPortal
     *
     * @param id
     * @param dashboardPortalUpdate
     * @param bindingResult
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateDashboardPortal(@PathVariable Long id,
                                                @Valid @RequestBody DashboardPortalUpdate dashboardPortalUpdate,
                                                BindingResult bindingResult,
                                                @CurrentUser User user,
                                                HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (invalidId(id) || !dashboardPortalUpdate.getId().equals(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        DashboardPortal dashboardPortal = dashboardPortalService.updateDashboardPortal(dashboardPortalUpdate, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(dashboardPortal));
    }


    /**
     * 删除dashboardPortal
     *
     * @param id
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping("/{id}")
    public ResponseEntity deleteDashboardPortal(@PathVariable Long id,
                                                @CurrentUser User user,
                                                HttpServletRequest request) {

        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        dashboardPortalService.deleteDashboardPortal(id, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 新建dashboard
     *
     * @param portalId
     * @param dashboardCreate
     * @param bindingResult
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(value = "/{id}/dashboards", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createDashboard(@PathVariable("id") Long portalId,
                                          @Valid @RequestBody DashboardCreate dashboardCreate,
                                          BindingResult bindingResult,
                                          @CurrentUser User user,
                                          HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (invalidId(portalId) || !dashboardCreate.getDashboardPortalId().equals(portalId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid dashboard portal id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        Dashboard dashboard = dashboardService.createDashboard(dashboardCreate, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(dashboard));
    }

    /**
     * 修改dashboard
     *
     * @param portalId
     * @param dashboards
     * @param bindingResult
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PutMapping(value = "{id}/dashboards", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateDashboards(@PathVariable("id") Long portalId,
                                           @Valid @RequestBody DashboardDto[] dashboards,
                                           BindingResult bindingResult,
                                           @CurrentUser User user,
                                           HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        for (Dashboard dashboard : dashboards) {
            if (!dashboard.getDashboardPortalId().equals(portalId)) {
                ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid dashboard portal id");
                return ResponseEntity.status(resultMap.getCode()).body(resultMap);
            }
        }

        dashboardService.updateDashboards(portalId, dashboards, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 删除dashboard
     *
     * @param dashboardId
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @DeleteMapping("/dashboards/{dashboardId}")
    public ResponseEntity deleteDashboard(@PathVariable Long dashboardId,
                                          @CurrentUser User user,
                                          HttpServletRequest request) {

        if (invalidId(dashboardId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        dashboardService.deleteDashboard(dashboardId, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 在dashboard下新建widget关联
     *
     * @param portalId
     * @param dashboardId
     * @param memDashboardWidgetCreates
     * @param bindingResult
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(value = "/{portalId}/dashboards/{dashboardId}/widgets", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createMemDashboardWidget(@PathVariable("portalId") Long portalId,
                                                   @PathVariable("dashboardId") Long dashboardId,
                                                   @Valid @RequestBody MemDashboardWidgetCreate[] memDashboardWidgetCreates,
                                                   BindingResult bindingResult,
                                                   @CurrentUser User user,
                                                   HttpServletRequest request) {
        if (invalidId(portalId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid dashboard portal id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (null == memDashboardWidgetCreates || memDashboardWidgetCreates.length < 1) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("dashboard widgets info cannot be EMPTY");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        for (MemDashboardWidgetCreate memDashboardWidgetCreate : memDashboardWidgetCreates) {
            if (invalidId(dashboardId) || !dashboardId.equals(memDashboardWidgetCreate.getDashboardId())) {
                ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid dashboard id");
                return ResponseEntity.status(resultMap.getCode()).body(resultMap);
            }
        }

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        List<MemDashboardWidget> memDashboardWidget = dashboardService.createMemDashboardWidget(portalId, dashboardId, memDashboardWidgetCreates, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(memDashboardWidget));
    }


    /**
     * 修改dashboard下的widget关联信息
     *
     * @param bindingResult
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PutMapping(value = "/{portalId}/dashboards/widgets", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateMemDashboardWidget(@PathVariable("portalId") Long portalId,
                                                   @Valid @RequestBody MemDashboardWidgetDto[] memDashboardWidgets,
                                                   BindingResult bindingResult,
                                                   @CurrentUser User user,
                                                   HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        for (MemDashboardWidget memDashboardWidget : memDashboardWidgets) {
            if (invalidId(memDashboardWidget.getId())) {
                ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
                return ResponseEntity.status(resultMap.getCode()).body(resultMap);
            }

            if (invalidId(memDashboardWidget.getDashboardId())) {
                ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid dashboard id");
                return ResponseEntity.status(resultMap.getCode()).body(resultMap);
            }

            if (invalidId(memDashboardWidget.getWidgetId())) {
                ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid widget id");
                return ResponseEntity.status(resultMap.getCode()).body(resultMap);
            }

            if (memDashboardWidget.getPolling() && memDashboardWidget.getFrequency() < 1) {
                ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid frequency");
                return ResponseEntity.status(resultMap.getCode()).body(resultMap);
            }
        }

        dashboardService.updateMemDashboardWidgets(portalId, user, memDashboardWidgets);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 删除dashboard下的widget关联信息
     *
     * @param relationId
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @DeleteMapping(value = "/dashboards/widgets/{relationId}")
    public ResponseEntity deleteMemDashboardWidget(@PathVariable Long relationId,
                                                   @CurrentUser User user,
                                                   HttpServletRequest request) {
        dashboardService.deleteMemDashboardWidget(relationId, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 分享dashboard
     *
     * @param dashboardId
     * @param username
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/dashboards/{dashboardId}/share")
    public ResponseEntity shareDashboard(@PathVariable Long dashboardId,
                                         @RequestParam(required = false) String username,
                                         @CurrentUser User user,
                                         HttpServletRequest request) {

        if (invalidId(dashboardId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid  id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        String shareToken = dashboardService.shareDashboard(dashboardId, username, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(shareToken));
    }

}
