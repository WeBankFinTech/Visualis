package com.webank.wedatasphere.dss.visualis.service.impl;

import com.webank.wedatasphere.dss.visualis.configuration.CommonConfig;
import com.webank.wedatasphere.dss.visualis.service.IBaseInfoService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import edp.davinci.dao.ProjectMapper;
import edp.davinci.dao.UserMapper;
import edp.davinci.model.Project;
import edp.davinci.model.User;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Service
public class BaseInfoServiceImpl implements IBaseInfoService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public Message getDefault(HttpServletRequest req) {
        Project project;
        String userName = SecurityFilter.getLoginUsername(req);
        User user = userMapper.selectByUsername(userName);
        List<Project> defaultProjects = projectMapper.getProjectByNameWithUserId(CommonConfig.DEFAULT_PROJECT_NAME().getValue(), user.getId());
        if (CollectionUtils.isEmpty(defaultProjects)) {
            project = new Project();
            project.setName(CommonConfig.DEFAULT_PROJECT_NAME().getValue());
            project.setCreateTime(new Date());
            project.setCreateUserId(user.getId());
            project.setDescription("");
            project.setInitialOrgId(null);
            project.setIsTransfer(false);
            project.setPic(null);
            project.setStarNum(0);
            project.setVisibility(true);
            project.setOrgId(null);
            project.setUserId(user.getId());
            projectMapper.insert(project);
        } else {
            project = defaultProjects.get(0);
        }

        Message message = Message.ok().data("project", project);
        return message;
    }
}
