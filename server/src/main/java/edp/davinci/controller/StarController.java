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

import edp.core.annotation.CurrentUser;
import edp.core.annotation.MethodLog;
import edp.core.enums.HttpCodeEnum;
import edp.davinci.common.controller.BaseController;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.model.User;
import edp.davinci.service.StarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(value = Constants.BASE_API_PATH + "/star", produces = MediaType.APPLICATION_JSON_VALUE)
public class StarController extends BaseController {

    @Autowired
    private StarService starService;

    @MethodLog
    @PostMapping("/project/{id}")
    public ResponseEntity starProject(@PathVariable Long id,
                                      @CurrentUser User user,
                                      HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        try {
            ResultMap resultMap = starService.starAndUnstar(Constants.STAR_TARGET_PROJECT, id, user, request);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        } catch (Exception e) {
            log.error("star project error: " + e);
            return ResponseEntity.status(HttpCodeEnum.SERVER_ERROR.getCode()).body(HttpCodeEnum.SERVER_ERROR.getMessage());
        }
    }

    @MethodLog
    @GetMapping("/project/{id}")
    public ResponseEntity getStarUsers(@PathVariable Long id,
                                       HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        try {
            ResultMap resultMap = starService.getStarUserListByTarget(Constants.STAR_TARGET_PROJECT, id, request);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        } catch (Exception e) {
            log.error("get star user error: " + e);
            return ResponseEntity.status(HttpCodeEnum.SERVER_ERROR.getCode()).body(HttpCodeEnum.SERVER_ERROR.getMessage());
        }
    }


    @MethodLog
    @GetMapping("/mystar/project")
    public ResponseEntity getMyStarProjects(@CurrentUser User user,
                                            HttpServletRequest request) {
        try {
            ResultMap resultMap = starService.getStarListByUser(Constants.STAR_TARGET_PROJECT, user, request);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        } catch (Exception e) {
            log.error("get mystar project error: " + e);
            return ResponseEntity.status(HttpCodeEnum.SERVER_ERROR.getCode()).body(HttpCodeEnum.SERVER_ERROR.getMessage());
        }
    }

}
