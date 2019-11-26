/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.webank.wedatasphere.dss.visualis.restful;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import edp.core.common.job.ScheduleService;
import edp.davinci.core.common.Constants;
import edp.davinci.dao.*;
import edp.davinci.model.*;
import edp.davinci.service.ShareService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
/**
 * Created by shanhuang on 2019/1/23.
 */
@Slf4j
@Path(Constants.RESTFUL_BASE_PATH + "params")
@Component
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ComponentScan(basePackages = {"edp","com.webank.wedatasphere.dss"})
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


    @POST
    @Path("create")
    public Response createParams(@Context HttpServletRequest req, Params params) {
        Message message = null;

        if(CollectionUtils.isEmpty(params.getParamDetails())){
            message = Message.error("Params body cannot be empty");
            return Message.messageToResponse(message);
        }

        params.setUuid(UUID.randomUUID().toString());
        params.setParams(JSONObject.toJSONString(params.getParamDetails()));

        paramsMapper.insert(params);
        message = Message.ok().data("params", params);
        return Message.messageToResponse(message);
    }

    @GET
    @Path("info")
    public Response getGraphInfo(@Context HttpServletRequest req, @QueryParam("projectName") String projectName) {
        Message message = null;

        String userName = SecurityFilter.getLoginUsername(req);
        User user = userMapper.selectByUsername(userName);

        Project project = Iterables.getFirst(projectMapper.getProjectByNameWithUserId(projectName, user.getId()), null);
        if(project == null) {
            message = Message.error("Project does not exist");
            return Message.messageToResponse(message);
        }

        List<Map<String, Object>> dashboardsInfo = Lists.newArrayList();
        List<DashboardPortal> dashboardPortals = dashboardPortalMapper.getByProject(project.getId());
        for(DashboardPortal portal : dashboardPortals){
            List<Dashboard> dashboardList = dashboardMapper.getByPortalId(portal.getId());
            for(Dashboard dashboard : dashboardList){
                Map<String, Object> dashboardInfo = Maps.newHashMap();
                dashboardInfo.put("dashboardId", dashboard.getId());
                dashboardInfo.put("name", dashboard.getName());
                dashboardInfo.put("url", scheduleService.getContentUrl(user.getId(), "dashboard", dashboard.getId()));

                List<Map<String, Object>> widgetsInfo = Lists.newArrayList();
                List<MemDashboardWidget> memDashboardWidgets = memDashboardWidgetMapper.getByDashboardId(dashboard.getId());
                for(MemDashboardWidget memDashboardWidget : memDashboardWidgets){
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
        return Message.messageToResponse(message);
    }


}
