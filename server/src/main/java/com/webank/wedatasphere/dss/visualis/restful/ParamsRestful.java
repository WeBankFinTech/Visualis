package com.webank.wedatasphere.dss.visualis.restful;

import com.webank.wedatasphere.dss.visualis.service.ParamsService;
import edp.core.annotation.MethodLog;
import edp.davinci.common.controller.BaseController;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.linkis.server.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = Constants.RESTFUL_BASE_PATH + "params", produces = MediaType.APPLICATION_JSON_VALUE)
@ComponentScan(basePackages = {"edp", "com.webank.wedatasphere.dss"})
public class ParamsRestful extends BaseController {

    @Autowired
    private ParamsService paramsService;

    @MethodLog
    @RequestMapping(path = "create", method = RequestMethod.POST)
    public ResponseEntity createParams(HttpServletRequest req, @RequestBody Params params) {

        if (CollectionUtils.isEmpty(params.getParamDetails())) {
            ResultMap resultMap = new ResultMap().fail().message("create param error.");
            return ResponseEntity.ok(resultMap);
        }
        try {
            paramsService.insertParams(params);
        } catch (Exception e) {
            log.error("create param fail, because: ", e);
            return ResponseEntity.ok(new ResultMap().fail().message(e.getMessage()));
        }
        return ResponseEntity.ok(new ResultMap().success().payload(params));
    }


    @MethodLog
    @RequestMapping(path = "info", method = RequestMethod.GET)
    public ResponseEntity getGraphInfo(HttpServletRequest req, @RequestParam String projectName) {

        String userName = SecurityFilter.getLoginUsername(req);
        List<Map<String, Object>> graphInfo;

        try {
            graphInfo = paramsService.getGraphInfo(projectName, userName);
        } catch (Exception e) {
            log.error("access info error, because: ", e);
            return ResponseEntity.ok(new ResultMap().fail().message(e.getMessage()));
        }

        if (null == graphInfo) {
            return ResponseEntity.ok(new ResultMap().fail().message("Project name is incorrect."));
        }

        return ResponseEntity.ok(new ResultMap().success().payload(graphInfo));
    }
}
