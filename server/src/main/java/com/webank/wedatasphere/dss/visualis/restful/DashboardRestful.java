package com.webank.wedatasphere.dss.visualis.restful;

import com.webank.wedatasphere.dss.visualis.enums.VisualisStateEnum;
import com.webank.wedatasphere.dss.visualis.service.AsynService;
import edp.core.annotation.CurrentUser;
import edp.core.annotation.MethodLog;
import edp.core.enums.HttpCodeEnum;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.dao.DashboardMapper;
import edp.davinci.dao.DisplayMapper;
import edp.davinci.dao.ProjectMapper;
import edp.davinci.model.*;
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
    public void getResult(HttpServletRequest req, @PathVariable("execId") String execId, @CurrentUser User user, HttpServletResponse response) throws Exception {
        InputStream resultStreams = null;
        try {
            String execState = asynService.state(execId, "dashboard");
            if (!execState.equals(VisualisStateEnum.SUCCESS.getValue())) {
                log.error("dashboard execute error because state is not success.");
                asynService.setPreviewResultArchived(execId);
                response.setStatus(HttpCodeEnum.SERVER_ERROR.getCode());
                response.getWriter().write("dashboard execute error because state is not success.");
            } else {
                PreviewResult previewResult = asynService.getResult(execId, "dashboard");
                resultStreams = new ByteArrayInputStream(previewResult.getResult());
                response.setContentType(MediaType.IMAGE_PNG_VALUE);
                IOUtils.copy(resultStreams, response.getOutputStream());
            }
        } catch (Exception e) {
            log.error("get dashboard execute result error.");
        } finally {
            if (resultStreams != null) {
                resultStreams.close();
            }
        }
    }
}
