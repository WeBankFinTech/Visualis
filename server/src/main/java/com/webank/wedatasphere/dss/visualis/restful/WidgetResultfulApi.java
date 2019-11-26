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

import com.webank.wedatasphere.linkis.server.Message;
import edp.davinci.core.common.Constants;
import edp.davinci.dao.ProjectMapper;
import edp.davinci.dao.ViewMapper;
import edp.davinci.dao.WidgetMapper;
import edp.davinci.model.Project;
import edp.davinci.model.View;
import edp.davinci.model.Widget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Created by johnnwang on 2019/1/24.
 */
@Slf4j
@Path(Constants.RESTFUL_BASE_PATH + "widgets")
@Component
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WidgetResultfulApi {

    @Autowired
    private ViewMapper viewMapper;

    @Autowired
    private WidgetMapper widgetMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @POST
    public Response updateProjectId(@Context HttpServletRequest req, Map<String, Object> json) {
        Message message = null;
        try {
            Long widgetId = ((Integer) json.getOrDefault("widgetId", -1)).longValue();
            Long projectId = ((Integer) json.getOrDefault("projectId", -1)).longValue();
            Long viewId = ((Integer) json.getOrDefault("viewId", -1)).longValue();

            Project project = projectMapper.getById(projectId.longValue());
            if (project == null) {
                message = Message.error("项目不存在");
                return Message.messageToResponse(message);
            }
            Widget widget = widgetMapper.getById(widgetId);
            if (widget == null) {
                message = Message.error("保存图表到数据开发失败，对应的图表不存在");
                return Message.messageToResponse(message);
            }
            widget.setProjectId(projectId);
            widgetMapper.update(widget);
            View view = viewMapper.getById(viewId);
            if (view != null) {
                view.setName(widget.getName());
                view.setProjectId(projectId);
                viewMapper.update(view);
            }
            message = Message.ok();
            message.data("widgetId", widgetId);
            message.data("projectId", projectId);
            message.data("viewId", viewId);
            return Message.messageToResponse(message);
        } catch (Exception e) {
            log.error("保存图表失败：", e);
            message = Message.error("保存图表失败：" + e.getMessage());
            return Message.messageToResponse(message);
        }
    }

}
