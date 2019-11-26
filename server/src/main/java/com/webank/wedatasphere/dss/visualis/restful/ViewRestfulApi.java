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

import com.webank.wedatasphere.dss.visualis.utils.HttpUtils;
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import com.webank.wedatasphere.dss.visualis.entrance.spark.SqlCodeParse;
import com.webank.wedatasphere.dss.visualis.exception.VGErrorException;
import com.webank.wedatasphere.dss.visualis.model.DWCResultInfo;
import com.webank.wedatasphere.dss.visualis.res.ResultHelper;
import com.webank.wedatasphere.dss.visualis.utils.VisualisUtils;
import edp.davinci.core.common.Constants;
import edp.davinci.dao.ProjectMapper;
import edp.davinci.dao.UserMapper;
import edp.davinci.dao.ViewMapper;
import edp.davinci.model.Project;
import edp.davinci.model.Source;
import edp.davinci.model.User;
import edp.davinci.model.View;
import edp.davinci.service.ProjectService;
import edp.davinci.service.SourceService;
import edp.davinci.service.ViewService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by johnnwang on 2019/1/21.
 */
@Slf4j
@Path(Constants.RESTFUL_BASE_PATH + "view")
@Component
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ComponentScan(basePackages = {"edp","com.webank.wedatasphere.dss"})
public class ViewRestfulApi {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ViewMapper viewMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private ViewService viewService;

    @POST
    public Response createView(@Context HttpServletRequest req, DWCResultInfo dwcResultInfo) {
        Message message = null;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            User user = userMapper.selectByUsername(userName);
            Project project = projectMapper.getProejctsByUser(user.getId()).get(0);


            if (project == null) {
                message = Message.error("用户没有默认的项目，请联系管理员");
                return Message.messageToResponse(message);

            }
            if (dwcResultInfo == null) {
                message = Message.error("结果为空，无法做可视化分析");
                return Message.messageToResponse(message);
            }
            if(StringUtils.isEmpty(dwcResultInfo.getExecutionCode())){
                message = Message.error("脚本为空，无法做可视化分析");
                return Message.messageToResponse(message);
            }
            String[] sqlList  = SqlCodeParse.parse(dwcResultInfo.getExecutionCode());
            int index = dwcResultInfo.getResultNumber();
            String code="";
            if(index < sqlList.length){
               code =  sqlList[index];
            }
            View view = new View();
            view.setProjectId(project.getId());
            view.setName(VisualisUtils.createTmpViewName(user.getName()));

            List<Source> sources= sourceService.getSources(project.getId(), user, HttpUtils.getUserTicketId(req));
            for(Source source : sources){
                if(VisualisUtils.isHiveDataSource(source)){
                    view.setSourceId(source.getId());
                }
            }

            view.setSql(code);
            view.setModel(ResultHelper.toModelItem(dwcResultInfo.getResultPath()));
            view.setConfig("{\"" + VisualisUtils.DWC_RESULT_INFO().getValue() + "\":" + BDPJettyServerHelper.gson().toJson(dwcResultInfo) + "}");
            try {
                view = createView(view, user);
                message = Message.ok();
                message.data("id", view.getId());
                message.data("projectId",view.getProjectId());
            } catch (VGErrorException e) {
                log.error("可视化分析失败：", e);
                message = Message.error("可视化分析失败：" + e.getMessage());
            }
            return Message.messageToResponse(message);
        } catch (Throwable e) {
            log.error("可视化分析失败：", e);
            message = Message.error("可视化分析失败：" + e.getMessage());
            return Message.messageToResponse(message);
        }
    }



    @Transactional
    public View createView(View view, User user) throws VGErrorException {
        int id = viewMapper.insert(view);
        if (id < 0) {
            throw new VGErrorException(70002, "将view 插入数据库失败");
        }
        return view;
    }
}
