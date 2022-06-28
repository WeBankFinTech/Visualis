/*
 * <<
 *  Davinci
 *  ==
 *  Copyright (C) 2016 - 2019 EDP
 *  ==
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  >>
 *
 */

package edp.davinci.core.inteceptor;

import org.apache.linkis.server.security.SecurityFilter;
import edp.core.annotation.CurrentUser;
import edp.core.inteceptor.CurrentUserMethodArgumentResolverInterface;
import edp.davinci.dao.UserMapper;
import edp.davinci.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * @CurrentUser 注解 解析器
 */
@Slf4j
public class CurrentUserMethodArgumentResolver implements CurrentUserMethodArgumentResolverInterface {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(User.class)
                && parameter.hasParameterAnnotation(CurrentUser.class);
    }

    /**
     * 动机:
     * 由于之前Visualis依赖于linkis_user表，存在极大的耦合，
     *
     * 解决方式:
     * 新建一张visualis_user表，复用原来的权限逻辑，
     * 如果访问Visualis时，使用该注解，没有该用户，即插入用户，录入用户信息。
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        try {
            String dssUser = (String) ((ServletWebRequest) webRequest).getRequest().getAttribute("dss-user");
            if (StringUtils.isNotBlank(dssUser)) {
                return userMapper.selectByUsername(dssUser);
            }
            String accessUsername = SecurityFilter.getLoginUsername(webRequest.getNativeRequest(HttpServletRequest.class));
            log.info("Get request access user name: {}", accessUsername);
            User visualisUser = (User) userMapper.selectByUsername(accessUsername);
            log.info("Get visualis user from table: {}", visualisUser);
            User user = new User();
            if(null == visualisUser) {
                user.setUsername(accessUsername);
                user.setName(accessUsername);
                user.setPassword(null);
                log.info("Insert into visualis user: {}", user);
                userMapper.insert(user);
                return user;
            }
            return visualisUser;
        } catch (Throwable e) {
            log.error("Failed to get user: ", e);
            throw e;
        }
    }
}