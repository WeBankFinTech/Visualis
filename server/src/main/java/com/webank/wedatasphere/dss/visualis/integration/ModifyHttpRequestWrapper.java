package com.webank.wedatasphere.dss.visualis.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

/**
 * 用来将DSS请求提供的cookie信息复制到visualis侧的cookie中
 */
public class ModifyHttpRequestWrapper extends HttpServletRequestWrapper {

    Logger logger = LoggerFactory.getLogger(ModifyHttpRequestWrapper.class);

    private Map<String, String> mapCookies;

    ModifyHttpRequestWrapper(HttpServletRequest request) {
        super(request);
        logger.info("Wrapper the request sent by DSS appconn.");
        this.mapCookies = new HashMap<>();
    }

    void putCookie(String name, String value) {
        logger.info("Wrapper the request sent by DSS appconn, And put cookie.");
        this.mapCookies.put(name, value);
    }
    public Cookie[] getCookies() {
        logger.info("Wrapper the request sent by DSS appconn, And get cookie.");
        HttpServletRequest request = (HttpServletRequest) getRequest();
        Cookie[] cookies = request.getCookies();
        if (mapCookies == null || mapCookies.isEmpty()) {
            return cookies;
        }
        if (cookies == null || cookies.length == 0) {
            List<Cookie> cookieList = new LinkedList<>();
            for (Map.Entry<String, String> entry : mapCookies.entrySet()) {
                String key = entry.getKey();
                if (key != null && !"".equals(key)) {
                    cookieList.add(new Cookie(key, entry.getValue()));
                }
            }
            if (cookieList.isEmpty()) {
                return cookies;
            }
            return cookieList.toArray(new Cookie[cookieList.size()]);
        } else {
            List<Cookie> cookieList = new ArrayList<>(Arrays.asList(cookies));
            for (Map.Entry<String, String> entry : mapCookies.entrySet()) {
                String key = entry.getKey();
                if (key != null && !"".equals(key)) {
                    for (int i = 0; i < cookieList.size(); i++) {
                        if(cookieList.get(i).getName().equals(key)){
                            cookieList.remove(i);
                        }
                    }
                    cookieList.add(new Cookie(key, entry.getValue()));
                }
            }
            return cookieList.toArray(new Cookie[cookieList.size()]);
        }
    }
}
