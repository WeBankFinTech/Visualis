package com.webank.wedatasphere.dss.visualis.restful;


import com.google.common.collect.Iterables;
import com.webank.wedatasphere.dss.visualis.enums.VisualisStateEnum;
import com.webank.wedatasphere.dss.visualis.service.AsynService;
import edp.core.annotation.CurrentUser;
import edp.core.annotation.MethodLog;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.dao.DisplayMapper;
import edp.davinci.dao.ProjectMapper;
import edp.davinci.model.Display;
import edp.davinci.model.PreviewResult;
import edp.davinci.model.Project;
import edp.davinci.model.User;
import edp.davinci.service.screenshot.HtmlContent;
import edp.davinci.service.screenshot.ImageContent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.linkis.server.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = Constants.RESTFUL_BASE_PATH + "displays", produces = MediaType.APPLICATION_JSON_VALUE)
@ComponentScan(basePackages = {"edp", "com.webank.wedatasphere.dss"})
public class DisplayRestful {

    @Autowired
    DisplayMapper displayMapper;

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    AsynService asynService;

    @MethodLog
    @RequestMapping(path = "{id}/submitPreview", method = RequestMethod.GET)
    public ResponseEntity sumbitDisplay(HttpServletRequest req, @PathVariable("id") Long id, @CurrentUser User user) {

        Display display = displayMapper.getById(id);
        Project project = projectMapper.getById(display.getProjectId());
        String execId = null;
        ResultMap resultMap = new ResultMap();
        try {
            execId = asynService.sumbmitPreviewTask(user, "display", id);
            Map<String, Object> resultDataMap = new HashMap<>();
            resultDataMap.put("execId", execId);
            resultMap.success().payload(resultDataMap);
        } catch (Exception e) {
            log.error("submit display task error.", e);
        }
        return ResponseEntity.ok(resultMap);
    }

    @MethodLog
    @RequestMapping(path = "{execId}/state", method = RequestMethod.POST)
    public ResponseEntity state(HttpServletRequest req, @PathVariable("execId") String execId, @CurrentUser User user) {
        ResultMap resultMap = new ResultMap();
        String execState = null;
        Map<String, Object> resultDataMap = new HashMap<>();
        try {
            execState = asynService.state(execId, "display");
            resultDataMap.put("status", execState);
            resultMap.success().payload(resultDataMap);
        } catch (Exception e) {
            log.error("get display execution state error. ", e);
        }
        return ResponseEntity.ok(resultMap);
    }

    @MethodLog
    @RequestMapping(path = "{execId}/getResult", method = RequestMethod.GET)
    public void getResult(HttpServletRequest req, @PathVariable("execId") String execId, @CurrentUser User user, HttpServletResponse response) throws Exception {
        ResultMap resultMap = new ResultMap();
        Map<String, Object> resultDataMap = new HashMap<>();
        try {
            // 1. 由于DSS侧异步执行时，failed状态也会去获取结果集，所以此处需要做兼容，
            // 临时解决方案，抛出一个异常断开http请求。
            String execState = asynService.state(execId, "display");
            if(!execState.equals(VisualisStateEnum.SUCCESS.getValue())) {
                throw new Exception("display execute error because state is not success, so throws an exception.");
            }
            PreviewResult previewResult = asynService.getResult(execId, "display");
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
