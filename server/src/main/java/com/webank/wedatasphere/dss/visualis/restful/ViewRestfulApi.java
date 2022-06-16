package com.webank.wedatasphere.dss.visualis.restful;

import com.webank.wedatasphere.dss.visualis.service.IViewService;
import com.webank.wedatasphere.dss.visualis.model.DWCResultInfo;
import edp.core.annotation.MethodLog;
import edp.davinci.core.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.linkis.server.Message;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;



@Slf4j
@RestController
@RequestMapping(path = Constants.RESTFUL_BASE_PATH + "view", produces = MediaType.APPLICATION_JSON_VALUE)
@ComponentScan(basePackages = {"edp", "com.webank.wedatasphere.dss"})
public class ViewRestfulApi {

    @Resource(name = "dssViewService")
    private IViewService dssViewService;

    @MethodLog
    @RequestMapping(path = "enginetypes", method = RequestMethod.GET)
    public Message getAvailableEngineTypes(HttpServletRequest req, Long id) {
        return dssViewService.getAvailableEngineTypes(req, id);
    }

    @MethodLog
    @RequestMapping(method = RequestMethod.POST)
    public Message createView(HttpServletRequest req, DWCResultInfo dwcResultInfo) {
        return dssViewService.createView(req, dwcResultInfo);
    }

    /**
     * Dss 执行 visualis view节点接口
     *
     * @param req request
     * @param id  viewId
     * @return 返回执行结果集
     * @throws Exception exception
     */
    @MethodLog
    @RequestMapping(path = "{id}/getdata", method = RequestMethod.GET)
    public Message getViewData(HttpServletRequest req, @PathVariable("id") Long id) throws Exception {
        return dssViewService.getViewData(req, id);
    }

    @MethodLog
    @RequestMapping(path = "{id}/async/submit", method = RequestMethod.GET)
    public Message asyncSubmitSql(HttpServletRequest req, @PathVariable("id") Long id) throws Exception {
        return dssViewService.submitQuery(req, id);
    }


    @MethodLog
    @RequestMapping(path = "{id}/type/source", method = RequestMethod.GET)
    public Message isHiveDataSource(HttpServletRequest req, @PathVariable("id") Long id) throws Exception {
        return dssViewService.isHiveDataSource(req, id);
    }


}
