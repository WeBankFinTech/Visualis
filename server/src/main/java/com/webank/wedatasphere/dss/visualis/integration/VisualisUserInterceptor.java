package com.webank.wedatasphere.dss.visualis.integration;

import com.webank.wedatasphere.dss.standard.app.sso.builder.DssMsgBuilderOperation;
import com.webank.wedatasphere.dss.standard.app.sso.plugin.filter.HttpRequestUserInterceptor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用来操作HTTP session中的用户信息
 * */
@Component
public class VisualisUserInterceptor implements HttpRequestUserInterceptor {

    public void addUserToSession(String username, HttpServletRequest httpServletRequest) {
        httpServletRequest.setAttribute("dss-user", username);
    }

    public HttpServletRequest wrapRequest(DssMsgBuilderOperation.DSSMsg dssMsg, HttpServletRequest httpServletRequest) {
        ModifyHttpRequestWrapper requestWrapper = new ModifyHttpRequestWrapper(httpServletRequest);
        for (Map.Entry<String, String> cookies : dssMsg.getCookies().entrySet()) {
            requestWrapper.putCookie(cookies.getKey(), cookies.getValue());
        }
        return requestWrapper;
    }

    @Override
    public boolean isUserExistInSession(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getAttribute("dss-user") != null;
    }

    @Override
    public String getUser(HttpServletRequest httpServletRequest) {
        return (String) httpServletRequest.getAttribute("dss-user");
    }


    @Override
    public HttpServletRequest addUserToRequest(String user, HttpServletRequest httpServletRequest){
        return httpServletRequest;
    }
}
