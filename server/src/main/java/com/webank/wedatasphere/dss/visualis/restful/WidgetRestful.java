package com.webank.wedatasphere.dss.visualis.restful;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.webank.wedatasphere.dss.visualis.query.service.VirtualViewQueryService;
import com.webank.wedatasphere.dss.visualis.service.DssWidgetService;
import edp.core.annotation.CurrentUser;
import edp.core.annotation.MethodLog;
import edp.core.utils.CollectionUtils;
import edp.davinci.common.controller.BaseController;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.dao.*;
import edp.davinci.dto.dashboardDto.DashboardWithPortal;
import edp.davinci.dto.displayDto.DisplayWithProject;
import edp.davinci.model.Dashboard;
import edp.davinci.model.Project;
import edp.davinci.model.User;
import edp.davinci.model.Widget;
import edp.davinci.service.ViewService;
import edp.davinci.service.WidgetService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.adapt.LinkisUtils;
import org.apache.linkis.cs.common.utils.CSCommonUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Slf4j
@RestController
@RequestMapping(path = Constants.RESTFUL_BASE_PATH + "widget", produces = MediaType.APPLICATION_JSON_VALUE)
@ComponentScan(basePackages = {"edp", "com.webank.wedatasphere.dss"})
public class WidgetRestful extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(WidgetRestful.class);

    private String migratedOldTime = "2000-05-26 18:34:01";

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    DssWidgetService dssWidgetService;

    @Autowired
    private ViewMapper viewMapper;

    @Autowired
    private WidgetMapper widgetMapper;

    @Autowired
    private DisplayMapper displayMapper;

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private ViewService viewService;

    @Autowired
    private VirtualViewQueryService virtualViewQueryService;


    @Autowired
    private WidgetService widgetService;

    @Autowired
    private UserMapper userMapper;


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
    public ResponseEntity setcontext(HttpServletRequest req, @CurrentUser User user, @RequestBody Map<String, Object> params) {

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


//    @MethodLog
//    @RequestMapping(path = "{type}/{id}/metadata", method = RequestMethod.GET)
//    public ResponseEntity compareWithSnapshot(HttpServletRequest req, @PathVariable("type") String type, @PathVariable("id") Long id) {
//        String userName = SecurityFilter.getLoginUsername(req);
//        ResultMap resultMap = null;
//        try {
//            resultMap = dssWidgetService.compareWithSnapshot(userName, type, id);
//        } catch (Exception e) {
//            log.error("get widget metadata error, because: " , e);
//            resultMap = new ResultMap().fail().message(e.getMessage());
//        }
//
//        return ResponseEntity.ok(resultMap);
//    }

    @MethodLog
    @RequestMapping(path = "{type}/{id}/metadata", method = RequestMethod.GET)
    public Message compareWithSnapshot(HttpServletRequest req, @PathVariable("type") String type, @PathVariable("id") Long id) {

        String userName = SecurityFilter.getLoginUsername(req);
        User user = userMapper.selectByUsername(userName);

        Project project = null;
        Set<Widget> widgets = Sets.newHashSet();
        if ("dashboard".equals(type)) {
            DashboardWithPortal dashboardWithPortal = dashboardMapper.getDashboardWithPortalAndProject(id);
            if (dashboardWithPortal == null) {
                Message message = Message.error("Dashboard does not exist.");
                return message;
            }
            project = dashboardWithPortal.getProject();
            widgets.addAll(widgetMapper.getByDashboard(id));
        } else if ("portal".equals(type)) {
            List<Dashboard> dashboards = dashboardMapper.getByPortalId(id);
            if (CollectionUtils.isEmpty(dashboards)) {
                Message message = Message.error("Display does not exist.");
                return message;
            }
            for (Dashboard dashboard : dashboards) {
                DashboardWithPortal dashboardWithPortal = dashboardMapper.getDashboardWithPortalAndProject(dashboard.getId());
                project = dashboardWithPortal.getProject();
                widgets.addAll(widgetMapper.getByDashboard(dashboard.getId()));
            }
        } else {
            DisplayWithProject displayWithProject = displayMapper.getDisplayWithProjectById(id);
            if (displayWithProject == null) {
                Message message = Message.error("Display does not exist.");
                return message;
            }
            project = displayWithProject.getProject();
            widgets.addAll(widgetMapper.getByDisplayId(id));
        }
        List<Map<String, String>> widgetsMetaData = Lists.newArrayList();
        for (Widget widget : widgets) {
            Map<String, String> widgetMeta = Maps.newHashMap();
            widgetMeta.put("name", widget.getName());
            if (widget.getUpdateTime() == null) {
                widgetMeta.put("updated", migratedOldTime);
            } else {
                widgetMeta.put("updated", simpleDateFormat.format(widget.getUpdateTime()));
            }
            widgetMeta.put("columns", StringUtils.join(getWidgetUsedColumns(widget.getConfig()), ";"));
            widgetsMetaData.add(widgetMeta);
        }

        Message message = Message.ok();
        message.data("projectName", project.getName());
        message.data("widgetsMetaData", widgetsMetaData);
        return message;
    }

    private Set<String> getWidgetUsedColumns(String config) {
        Set<String> columns = Sets.newHashSet();
        JsonObject configJson = LinkisUtils.gson().fromJson(config, JsonElement.class).getAsJsonObject();
        configJson.getAsJsonArray("rows").forEach(e -> columns.add(getRealColumn(e.getAsJsonObject().get("name").getAsString())));
        configJson.getAsJsonArray("cols").forEach(e -> columns.add(getRealColumn(e.getAsJsonObject().get("name").getAsString())));
        configJson.getAsJsonArray("metrics").forEach(e -> columns.add(getRealColumn(e.getAsJsonObject().get("name").getAsString())));
        return columns;
    }

    private String getRealColumn(String wrappedColumn) {
        return wrappedColumn.split("@")[0];
    }

}
