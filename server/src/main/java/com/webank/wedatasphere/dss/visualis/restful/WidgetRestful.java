package com.webank.wedatasphere.dss.visualis.restful;

import com.webank.wedatasphere.dss.visualis.service.DssWidgetService;
import edp.core.annotation.MethodLog;
import edp.davinci.common.controller.BaseController;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.linkis.cs.common.utils.CSCommonUtils;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping(path = Constants.RESTFUL_BASE_PATH + "widget", produces = MediaType.APPLICATION_JSON_VALUE)
@ComponentScan(basePackages = {"edp", "com.webank.wedatasphere.dss"})
public class WidgetRestful extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(WidgetRestful.class);

    @Autowired
    DssWidgetService dssWidgetService;

    /**
     * DSS工作流拖拽创建一个Widget的步骤：
     * 1. 创建widget /api/rest_j/v1/visualis/widget/smartcreate
     * 2. 设置该widget的CSID /api/rest_j/v1/visualis/widget/setcontext
     * */

    @MethodLog
    @RequestMapping(path = "rename", method = RequestMethod.POST)
    public ResponseEntity rename(HttpServletRequest req, @RequestBody Map<String, Object> params) {

        String userName = SecurityFilter.getLoginUsername(req);
        ResultMap resultMap = null;

        try {
            resultMap = dssWidgetService.rename(params);
        } catch (Exception e) {
            log.error("rename widget error, because: " , e);
            resultMap = new ResultMap().fail().message(e.getMessage());
        }
        return ResponseEntity.ok(resultMap);
    }

    @MethodLog
    @RequestMapping(path = "smartcreate", method = RequestMethod.POST)
    public ResponseEntity smartCreateFromSql(HttpServletRequest req, @RequestBody Map<String, Object> params) {

        ResultMap resultMap = null;
        String userName = SecurityFilter.getLoginUsername(req);

        try {
            resultMap = dssWidgetService.smartCreateFromSql(userName, params);
        } catch (Exception e) {
            log.error("rename widget error, because: " , e);
            resultMap = new ResultMap().fail().message(e.getMessage());
        }
        return ResponseEntity.ok(resultMap);
    }

    @MethodLog
    @RequestMapping(path = "setcontext", method = RequestMethod.POST)
    public ResponseEntity setcontext(HttpServletRequest req, @RequestBody Map<String, Object> params) {

        Long widgetId = ((Integer) params.getOrDefault("id", -1)).longValue();
        String contextId = ((String) params.getOrDefault(CSCommonUtils.CONTEXT_ID_STR, ""));

        ResultMap resultMap = null;

        try {
            resultMap = dssWidgetService.updateContextId(widgetId, contextId);
        } catch (Exception e) {
            log.error("set widget context error, because: " , e);
            resultMap = new ResultMap().fail().message(e.getMessage());
        }
        return ResponseEntity.ok(resultMap);
    }

    @MethodLog
    @RequestMapping(path = "{id}/getdata", method = RequestMethod.GET)
    public ResponseEntity getWidgetData(HttpServletRequest req, @PathVariable("id") Long id) {
        String userName = SecurityFilter.getLoginUsername(req);
        ResultMap resultMap = null;
        try {
            resultMap = dssWidgetService.getWidgetData(userName, id);
        } catch (Exception e) {
            log.error("get widget data error, because: " , e);
            resultMap = new ResultMap().fail().message(e.getMessage());
        }
        return ResponseEntity.ok(resultMap);
    }


    @MethodLog
    @RequestMapping(path = "{type}/{id}/metadata", method = RequestMethod.GET)
    public ResponseEntity compareWithSnapshot(HttpServletRequest req, @PathVariable("type") String type, @PathVariable("id") Long id) {
        String userName = SecurityFilter.getLoginUsername(req);
        ResultMap resultMap = null;
        try {
            resultMap = dssWidgetService.compareWithSnapshot(userName, type, id);
        } catch (Exception e) {
            log.error("get widget metadata error, because: " , e);
            resultMap = new ResultMap().fail().message(e.getMessage());
        }

        return ResponseEntity.ok(resultMap);
    }
}
