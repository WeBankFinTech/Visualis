package com.webank.wedatasphere.dss.visualis.restful;

import com.webank.wedatasphere.dss.visualis.service.DssViewService;
import com.webank.wedatasphere.dss.visualis.model.DWCResultInfo;
import edp.core.annotation.CurrentUser;
import edp.core.annotation.MethodLog;
import edp.davinci.common.controller.BaseController;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.linkis.server.Message;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = Constants.RESTFUL_BASE_PATH + "view", produces = MediaType.APPLICATION_JSON_VALUE)
@ComponentScan(basePackages = {"edp", "com.webank.wedatasphere.dss"})
public class ViewRestful extends BaseController {


    @Resource(name = "dssViewService")
    private DssViewService dssViewService;

    @MethodLog
    @RequestMapping(path = "enginetypes", method = RequestMethod.GET)
    public Message getAvailableEngineTypes(HttpServletRequest req, Long id) {
        List<String> engineTypes = null;
        try {
            engineTypes = dssViewService.getAvailableEngineTypes(req, id);
        } catch (Exception e) {
            log.error("read project error, because: " , e);
//            return ResponseEntity.ok(new ResultMap().fail().message(e.getMessage()));
        }
//        return ResponseEntity.ok(new ResultMap().success().payload(engineTypes));
        Message message = Message.ok().data("engineTypes", engineTypes);
        return message;
    }

    @MethodLog
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity createView(HttpServletRequest req, @CurrentUser User user, DWCResultInfo dwcResultInfo) {
        ResultMap resultMap = null;
        try {
            resultMap = dssViewService.createView(req, dwcResultInfo);
        } catch (Exception e) {
            log.error("create view error, because: " , e);
            resultMap = new ResultMap().fail().message(e.getMessage());
        }
        return ResponseEntity.ok(resultMap);
    }


    @MethodLog
    @RequestMapping(path = "{id}/getdata", method = RequestMethod.GET)
    public ResponseEntity getViewData(HttpServletRequest req, @PathVariable("id") Long id) {
        ResultMap resultMap = null;
        try {
            resultMap = dssViewService.getViewData(req, id);
        } catch (Exception e) {
            log.error("get view data error, because: " , e);
            resultMap = new ResultMap().fail().message(e.getMessage());
        }
        return ResponseEntity.ok(resultMap);
    }


    @MethodLog
    @RequestMapping(path = "{id}/async/submit", method = RequestMethod.GET)
    public ResponseEntity asyncSubmitSql(HttpServletRequest req, @PathVariable("id") Long id) {
        ResultMap resultMap = null;
        try {
            resultMap = dssViewService.submitQuery(req, id);
        } catch (Exception e) {
            log.error("submit view error, because: " , e);
            resultMap = new ResultMap().fail().message(e.getMessage());
        }
        return ResponseEntity.ok(resultMap);

    }


    @MethodLog
    @RequestMapping(path = "{id}/type/source", method = RequestMethod.GET)
    public ResponseEntity isHiveDataSource(HttpServletRequest req, @PathVariable("id") Long id) {
        ResultMap resultMap = null;
        try {
            resultMap = dssViewService.isHiveDataSource(req, id);
        } catch (Exception e) {
            log.error("get view source error, because: " , e);
            resultMap = new ResultMap().fail().message(e.getMessage());
        }
        return ResponseEntity.ok(resultMap);
    }
}
