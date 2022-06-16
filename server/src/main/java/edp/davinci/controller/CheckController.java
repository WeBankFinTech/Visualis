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

import edp.core.annotation.AuthIgnore;
import edp.core.annotation.CurrentUser;
import edp.core.annotation.MethodLog;
import edp.core.enums.HttpCodeEnum;
import edp.core.utils.TokenUtils;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.core.enums.CheckEntityEnum;
import edp.davinci.model.Project;
import edp.davinci.model.User;
import edp.davinci.service.CheckService;
import edp.davinci.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(value = Constants.BASE_API_PATH + "/check", produces = MediaType.APPLICATION_JSON_VALUE)
public class CheckController {

    @Autowired
    private CheckService checkService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TokenUtils tokenUtils;

    /**
     * 检查用户是否存在
     *
     * @param request
     * @return
     */
    @MethodLog
    @AuthIgnore
    @GetMapping("/user")
    public ResponseEntity checkUser(@RequestParam String username,
                                    @RequestParam(required = false) Long id,
                                    HttpServletRequest request) {
        try {
            ResultMap resultMap = checkService.checkSource(username, id, CheckEntityEnum.USER, null, request);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        } catch (Exception e) {
            log.error("check user error: " + e);
            return ResponseEntity.status(HttpCodeEnum.SERVER_ERROR.getCode()).body(HttpCodeEnum.SERVER_ERROR.getMessage());
        }
    }

    /**
     * 检查Organization是否存在
     *
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/organization" )
    public ResponseEntity checkOrganization(@RequestParam String name,
                                            @RequestParam(required = false) Long id,
                                            HttpServletRequest request) {
        try {
            ResultMap resultMap = checkService.checkSource(name, id, CheckEntityEnum.ORGANIZATION, null, request);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        } catch (Exception e) {
            log.error("check organization error: " + e);
            return ResponseEntity.status(HttpCodeEnum.SERVER_ERROR.getCode()).body(HttpCodeEnum.SERVER_ERROR.getMessage());
        }
    }

    /**
     * 检查Project是否存在
     *
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/project")
    public ResponseEntity checkProject(@CurrentUser User user,
                                       @RequestParam String name,
                                       @RequestParam(required = false) Long id,
                                       @RequestParam(required = false) Long orgId, HttpServletRequest request) {
        try {
            ResultMap resultMap = new ResultMap(tokenUtils);
            if (projectService.isExist(name, id, orgId, user.getId())) {
                resultMap = resultMap.failAndRefreshToken(request)
                        .message("the current project name is already taken");
            } else {
                resultMap = resultMap.successAndRefreshToken(request);
            }
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        } catch (Exception e) {
            log.error("check project error: " + e);
            return ResponseEntity.status(HttpCodeEnum.SERVER_ERROR.getCode()).body(HttpCodeEnum.SERVER_ERROR.getMessage());
        }
    }


    /**
     * 检查Disaplay是否存在
     *
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/display")
    public ResponseEntity checkDisplay(@RequestParam String name,
                                       @RequestParam(required = false) Long id,
                                       @RequestParam Long projectId, HttpServletRequest request) {
        try {
            ResultMap resultMap = checkService.checkSource(name, id, CheckEntityEnum.DISPLAY, projectId, request);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        } catch (Exception e) {
            log.error("check display error: " + e);
            return ResponseEntity.status(HttpCodeEnum.SERVER_ERROR.getCode()).body(HttpCodeEnum.SERVER_ERROR.getMessage());
        }
    }

    /**
     * 检查source是否存在
     *
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/source")
    public ResponseEntity checkSource(@RequestParam String name,
                                      @RequestParam(required = false) Long id,
                                      @RequestParam Long projectId, HttpServletRequest request) {
        try {
            ResultMap resultMap = checkService.checkSource(name, id, CheckEntityEnum.SOURCE, projectId, request);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        } catch (Exception e) {
            log.error("check source error: " + e);
            return ResponseEntity.status(HttpCodeEnum.SERVER_ERROR.getCode()).body(HttpCodeEnum.SERVER_ERROR.getMessage());
        }
    }

    /**
     * 检查view是否存在
     *
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/view")
    public ResponseEntity checkView(@RequestParam String name,
                                    @RequestParam(required = false) Long id,
                                    @RequestParam Long projectId, HttpServletRequest request) {
        try {
            ResultMap resultMap = checkService.checkSource(name, id, CheckEntityEnum.VIEW, projectId, request);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        } catch (Exception e) {
            log.error("check view error: " + e);
            return ResponseEntity.status(HttpCodeEnum.SERVER_ERROR.getCode()).body(HttpCodeEnum.SERVER_ERROR.getMessage());
        }
    }


    /**
     * 检查widget是否存在
     *
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/widget")
    public ResponseEntity checkWidget(@RequestParam String name,
                                      @RequestParam(required = false) Long id,
                                      @RequestParam Long projectId, HttpServletRequest request) {
        try {
            ResultMap resultMap = checkService.checkSource(name, id, CheckEntityEnum.WIDGET, projectId, request);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        } catch (Exception e) {
            log.error("check widget error: " + e);
            return ResponseEntity.status(HttpCodeEnum.SERVER_ERROR.getCode()).body(HttpCodeEnum.SERVER_ERROR.getMessage());
        }
    }

    /**
     * 检查dashboardportal是否存在
     *
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/dashboardPortal")
    public ResponseEntity checkDashboardPortal(@RequestParam String name,
                                               @RequestParam(required = false) Long id,
                                               @RequestParam Long projectId, HttpServletRequest request) {
        try {
            ResultMap resultMap = checkService.checkSource(name, id, CheckEntityEnum.DASHBOARDPORTAL, projectId, request);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        } catch (Exception e) {
            log.error("check dashboardPortal error: " + e);
            return ResponseEntity.status(HttpCodeEnum.SERVER_ERROR.getCode()).body(HttpCodeEnum.SERVER_ERROR.getMessage());
        }
    }

    /**
     * 检查dashboard是否存在
     *
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/dashboard")
    public ResponseEntity checkDashboard(@RequestParam String name,
                                         @RequestParam(required = false) Long id,
                                         @RequestParam Long portal, HttpServletRequest request) {
        try {
            ResultMap resultMap = checkService.checkSource(name, id, CheckEntityEnum.DASHBOARD, portal, request);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        } catch (Exception e) {
            log.error("check dashboard error: " + e);
            return ResponseEntity.status(HttpCodeEnum.SERVER_ERROR.getCode()).body(HttpCodeEnum.SERVER_ERROR.getMessage());
        }
    }


    /**
     * 检查工程名是否存在
     *
     * @param keywords
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/projectName")
    public ResponseEntity checkProjectName(@RequestParam(value = "keywords") String keywords,
                                           HttpServletRequest request) {

        Project project = projectService.checkProjectName(keywords);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(project));
    }


    /**
     * 检查cronjob是否存在
     *
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/cronjob")
    public ResponseEntity checkCronJob(@RequestParam String name,
                                       @RequestParam(required = false) Long id,
                                       @RequestParam Long projectId, HttpServletRequest request) {
        try {
            ResultMap resultMap = checkService.checkSource(name, id, CheckEntityEnum.CRONJOB, projectId, request);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        } catch (Exception e) {
            log.error("check cronjob error: " + e);
            return ResponseEntity.status(HttpCodeEnum.SERVER_ERROR.getCode()).body(HttpCodeEnum.SERVER_ERROR.getMessage());
        }
    }

}
