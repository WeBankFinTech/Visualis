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

import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import edp.core.annotation.CurrentUser;
import edp.core.consts.Consts;
import edp.core.inteceptor.CurrentUserMethodArgumentResolverInterface;
import edp.davinci.dao.UserMapper;
import edp.davinci.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
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

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        try
        {
            return (User)userMapper.selectByUsername(SecurityFilter.getLoginUsername(httpServletRequest));
        }catch (Throwable e){
            log.error("Failed to get user:",e);
            // 普通分享页特殊处理，可以不需要登录（不影响授权分享页，授权分享依旧需要登录）
            if (httpServletRequest != null && httpServletRequest.getRequestURI().contains("/share/")) {
                log.warn("Fallback to share page User handler for {}", httpServletRequest.getRequestURI());
                return  (User) webRequest.getAttribute(Consts.CURRENT_USER, RequestAttributes.SCOPE_REQUEST);
            }else {
                throw e;
            }
        }
    }
}