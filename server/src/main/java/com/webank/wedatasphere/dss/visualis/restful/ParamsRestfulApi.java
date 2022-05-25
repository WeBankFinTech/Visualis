package com.webank.wedatasphere.dss.visualis.restful;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import edp.core.annotation.MethodLog;
import edp.core.common.job.ScheduleService;
import edp.davinci.core.common.Constants;
import edp.davinci.dao.*;
import edp.davinci.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = Constants.RESTFUL_BASE_PATH + "params", produces = MediaType.APPLICATION_JSON_VALUE)
@ComponentScan(basePackages = {"edp", "com.webank.wedatasphere.dss"})
public class ParamsRestfulApi {

    @Autowired
    private ParamsMapper paramsMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DashboardPortalMapper dashboardPortalMapper;

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private MemDashboardWidgetMapper memDashboardWidgetMapper;

    @Autowired
    private WidgetMapper widgetMapper;

    @Autowired
    private ScheduleService scheduleService;

    @MethodLog
    @RequestMapping(path = "create", method = RequestMethod.POST)
    public Message createParams(HttpServletRequest req, @RequestBody Params params) {
        Message message = null;

        if (CollectionUtils.isEmpty(params.getParamDetails())) {
            message = Message.error("Params body cannot be empty");
            return message;
        }

        params.setUuid(UUID.randomUUID().toString());
        params.setParams(JSONObject.toJSONString(params.getParamDetails()));

        paramsMapper.insert(params);
        message = Message.ok().data("params", params);
        return message;
    }

    @MethodLog
    @RequestMapping(path = "info", method = RequestMethod.GET)
    public Message getGraphInfo(HttpServletRequest req, @RequestParam String projectName) {
        Message message;

        String userName = SecurityFilter.getLoginUsername(req);
        User user = userMapper.selectByUsername(userName);

        Project project = Iterables.getFirst(projectMapper.getProjectByNameWithUserId(projectName, user.getId()), null);
        if (project == null) {
            message = Message.error("Project does not exist");
            return message;
        }

        List<Map<String, Object>> dashboardsInfo = Lists.newArrayList();
        List<DashboardPortal> dashboardPortals = dashboardPortalMapper.getByProject(project.getId());
        for (DashboardPortal portal : dashboardPortals) {
            List<Dashboard> dashboardList = dashboardMapper.getByPortalId(portal.getId());
            for (Dashboard dashboard : dashboardList) {
                Map<String, Object> dashboardInfo = Maps.newHashMap();
                dashboardInfo.put("dashboardId", dashboard.getId());
                dashboardInfo.put("name", dashboard.getName());
                dashboardInfo.put("url", scheduleService.getContentUrl(user.getId(), "dashboard", dashboard.getId()));

                List<Map<String, Object>> widgetsInfo = Lists.newArrayList();
                List<MemDashboardWidget> memDashboardWidgets = memDashboardWidgetMapper.getByDashboardId(dashboard.getId());
                for (MemDashboardWidget memDashboardWidget : memDashboardWidgets) {
                    Map<String, Object> widgetInfo = Maps.newHashMap();
                    Widget widget = widgetMapper.getById(memDashboardWidget.getWidgetId());
                    widgetInfo.put("widgetId", widget.getId());
                    widgetInfo.put("name", widget.getName());
                    widgetInfo.put("viewId", widget.getViewId());
                    widgetInfo.put("url", scheduleService.getContentUrl(user.getId(), "widget", widget.getId()));
                    widgetsInfo.add(widgetInfo);
                }
                dashboardInfo.put("widgets", widgetsInfo);
                dashboardsInfo.add(dashboardInfo);
            }
        }


        message = Message.ok().data("dashboards", dashboardsInfo);
        return message;
    }
}
