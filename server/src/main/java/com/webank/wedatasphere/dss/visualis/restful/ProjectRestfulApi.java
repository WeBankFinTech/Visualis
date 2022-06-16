package com.webank.wedatasphere.dss.visualis.restful;

import com.webank.wedatasphere.dss.visualis.service.*;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import edp.core.annotation.MethodLog;
import edp.davinci.core.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@RestController
@RequestMapping(path = Constants.RESTFUL_BASE_PATH + "project", produces = MediaType.APPLICATION_JSON_VALUE)
@ComponentScan(basePackages = {"edp", "com.webank.wedatasphere.dss"})
public class ProjectRestfulApi {


    @Autowired
    private IProjectImportService projectImportService;


    @Autowired
    private IProjectExportService projectExportService;

    @Autowired
    private IProjectCopyService projectCopyService;

    @Autowired
    private IDisplayCopyService displayCopyService;

    @Autowired
    private IDashboardCopyService dashboardCopyService;

    @Autowired
    private IBaseInfoService baseInfoService;

    @Autowired
    private IProjectReaderService projectReaderService;

    @MethodLog
    @RequestMapping(path = "default", method = RequestMethod.GET)
    public Message getDefault(HttpServletRequest req) {
        return baseInfoService.getDefault(req);
    }

    @MethodLog
    @RequestMapping(path = "export", method = RequestMethod.POST)
    public Message exportProject(HttpServletRequest req, @RequestBody Map<String, String> params) throws Exception {

        String userName = SecurityFilter.getLoginUsername(req);


        return projectExportService.exportProject(params, userName);
    }

    @MethodLog
    @RequestMapping(path = "import", method = RequestMethod.POST)
    public Message importProject(HttpServletRequest req, @RequestBody Map<String, String> params) throws Exception {
        return projectImportService.importProject(req, params);
    }

    @MethodLog
    @RequestMapping(path = "read", method = RequestMethod.GET)
    public Message read(HttpServletRequest req, @RequestParam(value = "fileName", required = false) String fileName,
                        @RequestParam(value = "projectId", required = false) Long projectId) throws Exception {
        return projectReaderService.read(req, fileName, projectId);
    }

    @MethodLog
    @RequestMapping(path = "copy", method = RequestMethod.POST)
    public Message copy(HttpServletRequest req, @RequestBody Map<String, String> params) throws Exception {
        return projectCopyService.copy(req, params);
    }
}
