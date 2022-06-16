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

import com.google.common.collect.Lists;
import com.webank.wedatasphere.dss.visualis.auth.ProjectAuth;
import com.webank.wedatasphere.dss.visualis.query.service.VirtualViewQueryService;
import com.webank.wedatasphere.dss.visualis.query.utils.EnvLimitUtils;
import com.webank.wedatasphere.dss.visualis.query.utils.QueryUtils;
import com.webank.wedatasphere.dss.visualis.utils.HiveDBHelper;
import com.webank.wedatasphere.dss.visualis.utils.HttpUtils;
import edp.core.annotation.CurrentUser;
import edp.core.annotation.MethodLog;
import edp.core.model.Paginate;
import edp.core.model.PaginateWithQueryColumns;
import edp.davinci.common.controller.BaseController;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.core.utils.DacChannelUtil;
import edp.davinci.dto.viewDto.*;
import edp.davinci.model.DacChannel;
import edp.davinci.model.User;
import edp.davinci.service.ViewService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = Constants.BASE_API_PATH + "/views", produces = MediaType.APPLICATION_JSON_VALUE)
public class ViewController extends BaseController {

    @Autowired
    private ViewService viewService;

    @Autowired
    private VirtualViewQueryService virtualViewQueryService;

    @Autowired
    private DacChannelUtil dacChannelUtil;

    @Autowired
    private HiveDBHelper hiveDBHelper;

    @Autowired
    private ProjectAuth projectAuth;

    // 工作流创建widget时调用Step 3
    /**
     * 获取view
     *
     * @param projectId
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping
    public ResponseEntity getViews(@RequestParam Long projectId,
                                   @RequestParam(required = false) String contextId,
                                   @RequestParam(required = false) String nodeName,
                                   @CurrentUser User user,
                                   HttpServletRequest request) throws Exception {

        if (invalidId(projectId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        List<ViewBaseInfo> views = viewService.getViews(projectId, user);
        if (StringUtils.isNotBlank(contextId) && StringUtils.isNotBlank(nodeName)) {
            List<Object> virtualviews = Lists.newArrayList();
            virtualviews.addAll(QueryUtils.getFromContext(contextId, nodeName));
            virtualviews.addAll(views);
            return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(virtualviews));
        }
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(views));
    }


    /**
     * get view info
     *
     * @param id
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @GetMapping("/{id}")
    public ResponseEntity getView(@PathVariable Long id,
                                  @CurrentUser User user,
                                  HttpServletRequest request) {

        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid view id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        ViewWithSourceBaseInfo view = viewService.getView(id, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(view));
    }


    /**
     * 新建view
     *
     * @param view
     * @param bindingResult
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createView(@Valid @RequestBody ViewCreate view,
                                     BindingResult bindingResult,
                                     @CurrentUser User user,
                                     HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (EnvLimitUtils.notPermitted()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(EnvLimitUtils.ERROR_MESSAGE);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if(!projectAuth.isPorjectOwner(view.getProjectId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ViewWithSourceBaseInfo viewWithSourceBaseInfo = viewService.createView(view, user, HttpUtils.getUserTicketId(request));

        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(viewWithSourceBaseInfo));
    }


    /**
     * 修改View
     *
     * @param id
     * @param viewUpdate
     * @param bindingResult
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateView(@PathVariable Long id,
                                     @Valid @RequestBody ViewUpdate viewUpdate,
                                     BindingResult bindingResult,
                                     @CurrentUser User user,
                                     HttpServletRequest request) {


        if (invalidId(id) || !id.equals(viewUpdate.getId())) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid view id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (EnvLimitUtils.notPermitted()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(EnvLimitUtils.ERROR_MESSAGE);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        viewService.updateView(viewUpdate, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 删除View
     *
     * @param id
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @DeleteMapping("/{id}")
    public ResponseEntity deleteView(@PathVariable Long id,
                                     @CurrentUser User user,
                                     HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid view id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (EnvLimitUtils.notPermitted()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(EnvLimitUtils.ERROR_MESSAGE);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        viewService.deleteView(id, user);

        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request));
    }


    /**
     * 执行sql
     *
     * @param executeSql
     * @param bindingResult
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(value = "/executesql", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity executeSql(@Valid @RequestBody ViewExecuteSql executeSql,
                                     BindingResult bindingResult,
                                     @CurrentUser User user,
                                     HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (EnvLimitUtils.notPermitted()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(EnvLimitUtils.ERROR_MESSAGE);
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        PaginateWithQueryColumns paginateWithQueryColumns = viewService.executeSql(executeSql, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(paginateWithQueryColumns));
    }


    /**
     * 获取当前view对应的源数据
     *
     * @param id
     * @param executeParam
     * @param user
     * @param request
     * @return
     */
    @MethodLog
    @PostMapping(value = "/{id}/getdata", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getData(@PathVariable Long id,
                                  @RequestBody(required = false) ViewExecuteParam executeParam,
                                  @CurrentUser User user,
                                  HttpServletRequest request) throws Exception {
        if (invalidId(id) && executeParam.getView() == null) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid view id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        Paginate<Map<String, Object>> paginate;
        if (executeParam.getView() == null) {
            paginate = viewService.getData(id, executeParam, user, true);
        } else {
            paginate = virtualViewQueryService.getData(executeParam, user, true);
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(paginate));
    }

    @MethodLog
    @PostMapping(value = "/{id}/getprogress", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getProgress(@PathVariable String id,
                                      @RequestBody(required = false) ViewExecuteParam executeParam,
                                      @CurrentUser User user,
                                      HttpServletRequest request) throws Exception {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid exec id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        Paginate<Map<String, Object>> paginate = viewService.getAsyncProgress(id, user);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(paginate));
    }

    @MethodLog
    @PostMapping(value = "/{id}/kill", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity kill(@PathVariable String id,
                               @RequestBody(required = false) ViewExecuteParam executeParam,
                               @CurrentUser User user,
                               HttpServletRequest request) throws Exception {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid exec id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        Paginate<Map<String, Object>> paginate = viewService.killAsyncJob(id, user);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(paginate));
    }

    @MethodLog
    @PostMapping(value = "/{id}/getresult", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getResult(@PathVariable String id,
                                    @RequestBody(required = false) ViewExecuteParam executeParam,
                                    @CurrentUser User user,
                                    HttpServletRequest request) throws Exception {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid exec id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        Paginate<Map<String, Object>> paginate = viewService.getAsyncResult(id, user);
        if (executeParam == null || executeParam.getPageNo() == -1) {
            paginate.setPageNo(1);
            paginate.setPageSize(new Long(paginate.getTotalCount()).intValue());
        } else {
            paginate.setPageNo(executeParam.getPageNo());
            paginate.setPageSize(executeParam.getPageSize());
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(paginate));
    }

    @MethodLog
    @PostMapping(value = "/getdistinctvalue", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getDistinctValueNoView(@Valid @RequestBody DistinctParam param,
                                                 BindingResult bindingResult,
                                                 @CurrentUser User user,
                                                 HttpServletRequest request) throws Exception {
        return getDistinctValue(null, param, bindingResult, user, request);
    }

    @MethodLog
    @PostMapping(value = "/{id}/getdistinctvalue", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getDistinctValue(@PathVariable Long id,
                                           @Valid @RequestBody DistinctParam param,
                                           BindingResult bindingResult,
                                           @CurrentUser User user,
                                           HttpServletRequest request) throws Exception {
        if (invalidId(id) && param.getView() == null) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid view id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        List<Map<String, Object>> distinctValue = Lists.newArrayList();
        if (param.getView() == null) {
            distinctValue = viewService.getDistinctValue(id, param, user);
        } else {
            distinctValue = virtualViewQueryService.getDistinctValue(param, user);
        }
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(distinctValue));
    }

    @MethodLog
    @GetMapping("/dac/channels")
    public ResponseEntity getDacChannels(@CurrentUser User user, HttpServletRequest request) {
        Map<String, DacChannel> dacMap = DacChannelUtil.dacMap;
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(dacMap.keySet()));
    }

    @MethodLog
    @GetMapping("/dac/{dacName}/tenants")
    public ResponseEntity getDacTannets(@PathVariable String dacName, @CurrentUser User user, HttpServletRequest request) {

        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(dacChannelUtil.getTenants(dacName)));
    }

    @MethodLog
    @GetMapping("/dac/{dacName}/tenants/{tenantId}/bizs")
    public ResponseEntity getDacBizs(@PathVariable String dacName,
                                     @PathVariable String tenantId,
                                     @CurrentUser User user,
                                     HttpServletRequest request) {
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(dacChannelUtil.getBizs(dacName, tenantId)));
    }
}
