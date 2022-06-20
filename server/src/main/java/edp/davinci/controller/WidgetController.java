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
import edp.davinci.dto.viewDto.ViewExecuteParam;
import edp.davinci.dto.widgetDto.WidgetCreate;
import edp.davinci.dto.widgetDto.WidgetUpdate;
import edp.davinci.model.User;
import edp.davinci.model.Widget;
import edp.davinci.service.WidgetService;
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
@RequestMapping(value = Constants.BASE_API_PATH + "/widgets", produces = MediaType.APPLICATION_JSON_VALUE)
public class WidgetController extends BaseController {

    @Autowired
    private WidgetService widgetService;

    @Autowired
    private ProjectAuth projectAuth;

    @MethodLog
    @GetMapping
    public ResponseEntity getWidgets(@RequestParam Long projectId,
                                     @CurrentUser User user,
                                     HttpServletRequest request) {
        if (invalidId(projectId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        List<Widget> widgets = widgetService.getWidgets(projectId, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(widgets));
    }


    @MethodLog
    @GetMapping("/{id}")
    public ResponseEntity getWidgetInfo(@PathVariable Long id,
                                        @CurrentUser User user,
                                        HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }
        Widget widget = widgetService.getWidget(id, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(widget));
    }

    @MethodLog
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createWidgets(@Valid @RequestBody WidgetCreate widget,
                                        BindingResult bindingResult,
                                        @CurrentUser User user,
                                        HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }


        if(!projectAuth.isPorjectOwner(widget.getProjectId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Widget newWidget = widgetService.createWidget(widget, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(newWidget));
    }

    @MethodLog
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateWidget(@PathVariable Long id,
                                       @Valid @RequestBody WidgetUpdate widget,
                                       BindingResult bindingResult,
                                       @CurrentUser User user,
                                       HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (invalidId(id) || !id.equals(widget.getId())) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        widgetService.updateWidget(widget, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    @MethodLog
    @PostMapping("/{id}")
    public ResponseEntity deleteWidget(@PathVariable Long id,
                                       @CurrentUser User user,
                                       HttpServletRequest request) {

        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        widgetService.deleteWidget(id, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    @MethodLog
    @PostMapping("/{id}/{type}")
    public ResponseEntity downloadWidget(@PathVariable("id") Long id,
                                         @PathVariable("type") String type,
                                         @Valid @RequestBody ViewExecuteParam executeParam,
                                         BindingResult bindingResult,
                                         @CurrentUser User user,
                                         HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        String filePath = widgetService.generationFile(id, executeParam, user, type);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(filePath));
    }


    @MethodLog
    @GetMapping("/{id}/share")
    public ResponseEntity shareWidget(@PathVariable Long id,
                                      @RequestParam(required = false) String username,
                                      @CurrentUser User user,
                                      HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        String shareToken = widgetService.shareWidget(id, user, username);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(shareToken));
    }

}
