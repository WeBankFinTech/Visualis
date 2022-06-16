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

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Iterables;
import com.webank.wedatasphere.dss.visualis.auth.ProjectAuth;
import edp.core.annotation.CurrentUser;
import edp.core.annotation.MethodLog;
import edp.core.common.job.ScheduleService;
import edp.davinci.common.controller.BaseController;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.dao.DisplayMapper;
import edp.davinci.dao.ProjectMapper;
import edp.davinci.dto.displayDto.*;
import edp.davinci.model.*;
import edp.davinci.service.DisplayService;
import edp.davinci.service.screenshot.ImageContent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = Constants.BASE_API_PATH + "/displays", produces = MediaType.APPLICATION_JSON_VALUE)
public class DisplayController extends BaseController {

    @Autowired
    private DisplayService displayService;

    @Autowired
    ScheduleService scheduleService;

    //TODO not this layer, should be removed
    @Autowired
    DisplayMapper displayMapper;

    @Autowired
    ProjectMapper projectMapper;

    @Value("${file.userfiles-path}")
    private String fileBasePath;

    @Autowired
    private ProjectAuth projectAuth;

    /**
     * 新建display
     *
     * @param displayInfo
     * @param bindingResult
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createDisplay(@Valid @RequestBody DisplayInfo displayInfo,
                                        BindingResult bindingResult,
                                        @CurrentUser User user,
                                        HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }


        if(!projectAuth.isPorjectOwner(displayInfo.getProjectId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Display display;
        if(displayInfo.getIsCopy()){
            display = displayService.copyDisplay(displayInfo, user);
        } else {
            display = displayService.createDisplay(displayInfo, user);
        }
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(display));
    }

    /**
     * 更新display 信息
     *
     * @param display
     * @param bindingResult
     * @param user
     * @param id
     * @param request
     * @return
     */
    @MethodLog
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateDisplay(@Valid @RequestBody DisplayUpdate display,
                                        BindingResult bindingResult,
                                        @CurrentUser User user,
                                        @PathVariable Long id, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (invalidId(id) || !id.equals(display.getId())) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        displayService.updateDisplay(display, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }

    /**
     * 删除display
     *
     * @param id
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @DeleteMapping("/{id}")
    public ResponseEntity deleteDisplay(@PathVariable Long id,
                                        @CurrentUser User user,
                                        HttpServletRequest request) {

        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid display id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        displayService.deleteDisplay(id, user);

        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 新建displaySlide
     *
     * @param displaySlideCreate
     * @param bindingResult
     * @param displayId
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(value = "/{id}/slides", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createDisplaySlide(@Valid @RequestBody DisplaySlideCreate displaySlideCreate,
                                             BindingResult bindingResult,
                                             @PathVariable("id") Long displayId,
                                             @CurrentUser User user,
                                             HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (invalidId(displayId) || !displayId.equals(displaySlideCreate.getDisplayId())) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid display id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        DisplaySlide displaySlide = displayService.createDisplaySlide(displaySlideCreate, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(displaySlide));
    }

    /**
     * 更新displayslides信息
     *
     * @param displaySlides
     * @param bindingResult
     * @param user
     * @param displayId
     * @param request
     * @return
     */
    @MethodLog
    @PutMapping(value = "/{id}/slides", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateDisplaySlide(@Valid @RequestBody DisplaySlide[] displaySlides,
                                             BindingResult bindingResult,
                                             @CurrentUser User user,
                                             @PathVariable("id") Long displayId, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (invalidId(displayId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid display id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (null == displaySlides || displaySlides.length < 1) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("display slide info cannot be EMPTY");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        displayService.updateDisplaySildes(displayId, displaySlides, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 删除DisplaySlide
     *
     * @param slideId
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @DeleteMapping("/slides/{slideId}")
    public ResponseEntity deleteDisplaySlide(@PathVariable("slideId") Long slideId,
                                             @CurrentUser User user,
                                             HttpServletRequest request) {

        if (invalidId(slideId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid display slide id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        displayService.deleteDisplaySlide(slideId, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 在displaySlide下新建widget关联
     *
     * @param slideWidgetCreates
     * @param displayId
     * @param slideId
     * @param bindingResult
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(value = "/{displayId}/slides/{slideId}/widgets", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addMemDisplaySlideWidgets(@PathVariable("displayId") Long displayId,
                                                    @PathVariable("slideId") Long slideId,
                                                    @Valid @RequestBody MemDisplaySlideWidgetCreate[] slideWidgetCreates,
                                                    BindingResult bindingResult,
                                                    @CurrentUser User user,
                                                    HttpServletRequest request) {

        if (invalidId(displayId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid display id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (invalidId(slideId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid display slide id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (null == slideWidgetCreates || slideWidgetCreates.length < 1) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("display slide widget info cannot be EMPTY");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        for (MemDisplaySlideWidgetCreate slideWidgetCreate : slideWidgetCreates) {
            if (!slideWidgetCreate.getDisplaySlideId().equals(slideId)) {
                ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid display slide id");
                return ResponseEntity.status(resultMap.getCode()).body(resultMap);
            }
            if (slideWidgetCreate.getType() == 1 && invalidId(slideWidgetCreate.getWidgetId())) {
                ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid widget id");
                return ResponseEntity.status(resultMap.getCode()).body(resultMap);
            }
        }

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        List<MemDisplaySlideWidget> memDisplaySlideWidgets = displayService.addMemDisplaySlideWidgets(displayId, slideId, slideWidgetCreates, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(memDisplaySlideWidgets));
    }

    /**
     * 批量修改widget关联
     *
     * @param memDisplaySlideWidgets
     * @param displayId
     * @param slideId
     * @param bindingResult
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PutMapping(value = "/{displayId}/slides/{slideId}/widgets", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateMemDisplaySlideWidgets(@PathVariable("displayId") Long displayId,
                                                       @PathVariable("slideId") Long slideId,
                                                       @Valid @RequestBody MemDisplaySlideWidgetDto[] memDisplaySlideWidgets,
                                                       BindingResult bindingResult,
                                                       @CurrentUser User user,
                                                       HttpServletRequest request) {

        if (invalidId(displayId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid display id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (invalidId(slideId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid display slide id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (null == memDisplaySlideWidgets || memDisplaySlideWidgets.length < 1) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("display slide widget info cannot be EMPTY");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        for (MemDisplaySlideWidget slideWidgetCreate : memDisplaySlideWidgets) {
            if (!slideWidgetCreate.getDisplaySlideId().equals(slideId)) {
                ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid display slide id");
                return ResponseEntity.status(resultMap.getCode()).body(resultMap);
            }
            if (1 == slideWidgetCreate.getType() && invalidId(slideWidgetCreate.getWidgetId())) {
                ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid widget id");
                return ResponseEntity.status(resultMap.getCode()).body(resultMap);
            }
        }

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        displayService.updateMemDisplaySlideWidgets(displayId, slideId, memDisplaySlideWidgets, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 修改displaySlide下的widget关联信息
     *
     * @param memDisplaySlideWidget
     * @param bindingResult
     * @param relationId
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PutMapping(value = "/slides/widgets/{relationId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateMemDisplaySlideWidget(@PathVariable("relationId") Long relationId,
                                                      @Valid @RequestBody MemDisplaySlideWidget memDisplaySlideWidget,
                                                      BindingResult bindingResult,
                                                      @CurrentUser User user,
                                                      HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (invalidId(relationId) || !memDisplaySlideWidget.getId().equals(relationId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid relation id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        displayService.updateMemDisplaySlideWidget(memDisplaySlideWidget, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 删除displaySlide下的widget关联信息
     *
     * @param relationId
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @DeleteMapping("/slides/widgets/{relationId}")
    public ResponseEntity deleteMemDisplaySlideWidget(@PathVariable("relationId") Long relationId,
                                                      @CurrentUser User user,
                                                      HttpServletRequest request) {

        if (invalidId(relationId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid relation id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        displayService.deleteMemDisplaySlideWidget(relationId, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }

    /**
     * 获取display列表
     *
     * @param projectId
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping
    public ResponseEntity getDisplays(@RequestParam Long projectId,
                                      @CurrentUser User user,
                                      HttpServletRequest request) {

        if (invalidId(projectId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }
        List<Display> displayList = displayService.getDisplayListByProject(projectId, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(displayList));
    }


    /**
     * 获取display slide列表
     *
     * @param id
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/{id}/slides")
    public ResponseEntity getDisplaySlide(@PathVariable Long id,
                                          @CurrentUser User user,
                                          HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid Display id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }
        DisplayWithSlides displayWithSlides = displayService.getDisplaySlideList(id, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(displayWithSlides));
    }


    /**
     * 获取displaySlide下widgets关联信息列表
     *
     * @param displayId
     * @param slideId
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/{displayId}/slides/{slideId}")
    public ResponseEntity getDisplaySlideWidgets(@PathVariable("displayId") Long displayId,
                                                 @PathVariable("slideId") Long slideId,
                                                 @CurrentUser User user,
                                                 HttpServletRequest request) {

        if (invalidId(displayId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid Display id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (invalidId(slideId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid Display Slide id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        SlideWithMem displaySlideMem = displayService.getDisplaySlideMem(displayId, slideId, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(displaySlideMem));
    }


    /**
     * 删除displaySlide下widgets关联信息列表
     *
     * @param displayId
     * @param slideId
     * @param user
     * @param request
     * @return
     */
    // RequestBody: {"slides":["83"],"labels":{"route":"dev"}}
    @MethodLog
    @DeleteMapping("/{displayId}/slides/{slideId}/widgets")
    public ResponseEntity deleteDisplaySlideWeight(@PathVariable("displayId") Long displayId,
                                                   @PathVariable("slideId") Long slideId,
                                                   @RequestBody Map<String, Object> param,
                                                   @CurrentUser User user,
                                                   HttpServletRequest request) {

        Long[] ids = ((JSONArray) param.get("slides")).toJavaList(Long.class).toArray(new Long[]{});

        if (invalidId(displayId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid Display id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (invalidId(slideId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid Display Slide id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (null == ids || ids.length < 1) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("nothing be deleted");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        displayService.deleteDisplaySlideWidgetList(displayId, slideId, ids, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 上传封面图
     *
     * @param file
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(value = "/upload/coverImage")
    public ResponseEntity uploadAvatar(@RequestParam("coverImage") MultipartFile file,
                                       HttpServletRequest request) {


        if (file.isEmpty() || StringUtils.isEmpty(file.getOriginalFilename())) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("file can not be EMPTY");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        String avatar = displayService.uploadAvatar(file);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(avatar));
    }


    /**
     * 上传slide背景图
     *
     * @param slideId
     * @param file
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(value = "/slide/{slideId}/upload/bgImage")
    public ResponseEntity uploadSlideBGImage(@PathVariable Long slideId,
                                             @RequestParam("backgroundImage") MultipartFile file,
                                             @CurrentUser User user,
                                             HttpServletRequest request) {

        if (invalidId(slideId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid slide id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (file.isEmpty() || StringUtils.isEmpty(file.getOriginalFilename())) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("file can not be EMPTY");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        String slideBGImage = displayService.uploadSlideBGImage(slideId, file, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(slideBGImage));
    }

    /**
     * 上传slide背景图
     *
     * @param relationId
     * @param file
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(value = "/slide/widget/{relationId}/bgImage")
    public ResponseEntity uploadSlideSubWidgetBGImage(@PathVariable Long relationId,
                                                      @RequestParam("backgroundImage") MultipartFile file,
                                                      @CurrentUser User user,
                                                      HttpServletRequest request) {

        if (invalidId(relationId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid relation id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (file.isEmpty() || StringUtils.isEmpty(file.getOriginalFilename())) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("file can not be EMPTY");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        String bgImage = displayService.uploadSlideSubWidgetBGImage(relationId, file, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(bgImage));
    }

    /**
     * 共享display
     *
     * @param id
     * @param username
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/{id}/share")
    public ResponseEntity shareDisplay(@PathVariable Long id,
                                       @RequestParam(required = false) String username,
                                       @CurrentUser User user,
                                       HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        String shareToken = displayService.shareDisplay(id, user, username);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(shareToken));
    }


    /**
     * 获取Display 排除访问的团队列表
     *
     * @param id
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/{id}/exclude/roles")
    public ResponseEntity getDisplayExcludeRoles(@PathVariable Long id,
                                                 HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        List<Long> excludeRoles = displayService.getDisplayExcludeRoles(id);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(excludeRoles));
    }


    /**
     * 获取Display 排除访问的团队列表
     *
     * @param id
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/slide/{id}/exclude/roles")
    public ResponseEntity getSlideExcludeRoles(@PathVariable Long id,
                                               HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        List<Long> excludeRoles = displayService.getSlideExecludeRoles(id);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(excludeRoles));
    }

    @MethodLog
    @GetMapping(value = "/{id}/preview", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public void previewDisplay(@PathVariable Long id,
                                        @RequestParam(required = false) String username,
                                        @CurrentUser User user,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws IOException {
        Display display = displayMapper.getById(id);
        Project project = projectMapper.getById(display.getProjectId());

        FileInputStream inputStream = null;
        try {
            List<ImageContent> imageFiles = scheduleService.getPreviewImage(user.getId(), "display", id);
            File imageFile = Iterables.getFirst(imageFiles, null).getImageFile();
            if(null != imageFile) {
                inputStream = new FileInputStream(imageFile);
                response.setContentType(MediaType.IMAGE_PNG_VALUE);
                IOUtils.copy(inputStream, response.getOutputStream());
            } else {
                log.error("Execute display failed, because image file is null.");
                response.sendError(504, "Execute display failed, because image file is null.");
            }
        } catch (Exception e) {
            log.error("display preview error: ", e);
        } finally {
            if(null != inputStream) {
                inputStream.close();
            }
        }
    }
}
