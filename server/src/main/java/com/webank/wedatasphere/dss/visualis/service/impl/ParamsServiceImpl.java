package com.webank.wedatasphere.dss.visualis.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.wedatasphere.dss.visualis.service.ParamsService;
import com.webank.wedatasphere.dss.visualis.content.CommonContant;
import com.webank.wedatasphere.dss.visualis.content.DashboardContant;
import com.webank.wedatasphere.dss.visualis.content.ViewContant;
import com.webank.wedatasphere.dss.visualis.content.WidgetContant;
import edp.core.common.job.ScheduleService;
import edp.davinci.dao.*;
import edp.davinci.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ParamsServiceImpl implements ParamsService {

    private static Logger logger = LoggerFactory.getLogger(ParamsServiceImpl.class);

    @Autowired
    private ParamsMapper paramsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private DashboardPortalMapper dashboardPortalMapper;

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private MemDashboardWidgetMapper memDashboardWidgetMapper;

    @Autowired
    private WidgetMapper widgetMapper;

    @Override
    public void insertParams(Params params) throws Exception {

        params.setUuid(UUID.randomUUID().toString());
        params.setParams(JSONObject.toJSONString(params.getParamDetails()));

        logger.info("Params is creating,  Params is {}", params);
        paramsMapper.insert(params);
    }

    @Override
    public List<Map<String, Object>> getGraphInfo(String projectName, String userName) {
        User user = userMapper.selectByUsername(userName);

        Project project = Iterables.getFirst(projectMapper.getProjectByNameWithUserId(projectName, user.getId()), null);
        if (project == null) {
            logger.error("Project does not exist");
            return null;
        }

        List<Map<String, Object>> dashboardsInfo = Lists.newArrayList();
        List<DashboardPortal> dashboardPortals = dashboardPortalMapper.getByProject(project.getId());
        for (DashboardPortal portal : dashboardPortals) {
            List<Dashboard> dashboardList = dashboardMapper.getByPortalId(portal.getId());
            for (Dashboard dashboard : dashboardList) {
                Map<String, Object> dashboardInfo = Maps.newHashMap();
                dashboardInfo.put(DashboardContant.DASHBOARD_ID, dashboard.getId());
                dashboardInfo.put(DashboardContant.NAME, dashboard.getName());
                dashboardInfo.put(CommonContant.URL, scheduleService.getContentUrl(user.getId(), DashboardContant.DASHBOARD, dashboard.getId()));

                List<Map<String, Object>> widgetsInfo = Lists.newArrayList();
                List<MemDashboardWidget> memDashboardWidgets = memDashboardWidgetMapper.getByDashboardId(dashboard.getId());
                for (MemDashboardWidget memDashboardWidget : memDashboardWidgets) {
                    Map<String, Object> widgetInfo = Maps.newHashMap();
                    Widget widget = widgetMapper.getById(memDashboardWidget.getWidgetId());
                    widgetInfo.put(WidgetContant.WIDGET_ID, widget.getId());
                    widgetInfo.put(WidgetContant.NAME, widget.getName());
                    widgetInfo.put(ViewContant.VIEW_ID, widget.getViewId());
                    widgetInfo.put(CommonContant.URL, scheduleService.getContentUrl(user.getId(), WidgetContant.WIDGET, widget.getId()));
                    widgetsInfo.add(widgetInfo);
                }
                dashboardInfo.put(WidgetContant.WIDGETS, widgetsInfo);
                dashboardsInfo.add(dashboardInfo);
            }
        }
        return dashboardsInfo;
    }
}
