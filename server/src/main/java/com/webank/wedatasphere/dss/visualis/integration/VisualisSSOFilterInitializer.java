package com.webank.wedatasphere.dss.visualis.integration;

import com.webank.wedatasphere.dss.standard.app.sso.origin.filter.spring.SpringOriginSSOPluginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * 将DSS提供的SSO Filter加入Visualis的HTTP请求处理的链路中
 * */
public class VisualisSSOFilterInitializer implements WebApplicationInitializer {
    Logger logger = LoggerFactory.getLogger(VisualisSSOFilterInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        logger.info("Add DSS filter to the request processing of visualis.");
        FilterRegistration.Dynamic myFilter = servletContext.addFilter("dssSSOFilter", SpringOriginSSOPluginFilter.class);
        myFilter.addMappingForUrlPatterns(null, false, "/*");
    }

}
