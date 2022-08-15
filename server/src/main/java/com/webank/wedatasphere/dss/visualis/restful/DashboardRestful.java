package com.webank.wedatasphere.dss.visualis.restful;

import com.google.common.collect.Iterables;
import com.webank.wedatasphere.dss.visualis.enums.VisualisStateEnum;
import com.webank.wedatasphere.dss.visualis.service.AsynService;
import edp.core.annotation.CurrentUser;
import edp.core.annotation.MethodLog;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.dao.DashboardMapper;
import edp.davinci.dao.DisplayMapper;
import edp.davinci.dao.ProjectMapper;
import edp.davinci.dto.dashboardDto.DashboardWithPortal;
import edp.davinci.model.*;
import edp.davinci.service.screenshot.HtmlContent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping(path = Constants.RESTFUL_BASE_PATH + "dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
@ComponentScan(basePackages = {"edp", "com.webank.wedatasphere.dss"})
public class DashboardRestful {

    @Autowired
    DisplayMapper displayMapper;

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    AsynService asynService;

    @Autowired
    DashboardMapper dashboardMapper;


    @MethodLog
    @RequestMapping(path = "/portal/{id}/submitPreview", method = RequestMethod.GET)
    public ResponseEntity sumbitDisplay(HttpServletRequest req, @PathVariable("id") Long id, @CurrentUser User user) {

        List<Dashboard> dashboards = dashboardMapper.getByPortalId(id);
        String execId = null;
        ResultMap resultMap = new ResultMap();
        try {
            execId = asynService.sumbmitPreviewTask(user, "dashboard", id);
            Map<String, Object> resultDataMap = new HashMap<>();
            resultDataMap.put("execId", execId);
            resultMap.success().payload(resultDataMap);
        } catch (Exception e) {
            log.error("submit display task error.", e);
        }
        return ResponseEntity.ok(resultMap);
    }

    @MethodLog
    @RequestMapping(path = "/portal/{execId}/state", method = RequestMethod.POST)
    public ResponseEntity state(HttpServletRequest req, @PathVariable("execId") String execId, @CurrentUser User user) {
        ResultMap resultMap = new ResultMap();
        String execState = null;
        Map<String, Object> resultDataMap = new HashMap<>();
        try {
            execState = asynService.state(execId, "dashboard");
            resultDataMap.put("status", execState);
            resultMap.success().payload(resultDataMap);
        } catch (Exception e) {
            log.error("get display execution state error. ", e);
        }
        return ResponseEntity.ok(resultMap);
    }

    @MethodLog
    @RequestMapping(path = "/portal/{execId}/getResult", method = RequestMethod.GET)
    public void getResult(HttpServletRequest req, @PathVariable("execId") String execId, @CurrentUser User user, HttpServletResponse response)  throws Exception  {
        ResultMap resultMap = new ResultMap();
        Map<String, Object> resultDataMap = new HashMap<>();
        try {

            // 1. 由于DSS侧异步执行时，failed状态也会去获取结果集，所以此处需要做兼容，
            // 临时解决方案，抛出一个异常断开http请求。
            String execState = asynService.state(execId, "dashboard");
            if(!execState.equals(VisualisStateEnum.SUCCESS.getValue())) {
                throw new Exception("dashboard execute error because state is not success, so throws an exception.");
            }

            PreviewResult previewResult = asynService.getResult(execId, "dashboard");
            InputStream resultStreams = new ByteArrayInputStream(previewResult.getResult());

            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            IOUtils.copy(resultStreams, response.getOutputStream());

            resultDataMap.put("resultBytes", previewResult.getResult());

        } catch (Exception e) {
            log.error("get display execute result error.");
            throw e;
        }
    }
}
