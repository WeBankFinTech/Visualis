package com.webank.wedatasphere.dss.visualis.integration;

import com.webank.wedatasphere.dss.standard.app.structure.project.plugin.filter.AbstractProjectAuthInterceptor;
import com.webank.wedatasphere.dss.standard.app.structure.project.plugin.filter.ProjectRequestType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class VisualisProjectAuthInterceptor extends AbstractProjectAuthInterceptor {

    @Override
    public boolean isProjectRequest(HttpServletRequest request) {
        return true;
    }

    @Override
    protected Object getForbiddenMsg(String s) {
        return s;
    }

    @Override
    public String getProjectId(HttpServletRequest httpServletRequest) {
        return null;
    }

    @Override
    public String getProjectName(HttpServletRequest httpServletRequest) {
        return null;
    }

    @Override
    public ProjectRequestType getProjectRequestType(HttpServletRequest httpServletRequest) {
        if("GET".equalsIgnoreCase(httpServletRequest.getMethod())){
            return ProjectRequestType.Access;
        } else if ("PUT".equalsIgnoreCase(httpServletRequest.getMethod())) {
            return ProjectRequestType.Edit;
        } else if ("DELETE".equalsIgnoreCase(httpServletRequest.getMethod())){
            return ProjectRequestType.Delete;
        } else {
            return ProjectRequestType.Execute;
        }
    }
}
