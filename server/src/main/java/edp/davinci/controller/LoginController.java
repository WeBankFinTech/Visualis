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

package edp.davinci.controller;

import edp.core.annotation.MethodLog;
import edp.core.utils.TokenUtils;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.dto.userDto.UserLogin;
import edp.davinci.dto.userDto.UserLoginResult;
import edp.davinci.model.User;
import edp.davinci.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@RestController
@Slf4j
@RequestMapping(value = Constants.BASE_API_PATH + "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private Environment environment;

    /**
     * 登录
     *
     * @param userLogin
     * @param bindingResult
     * @return
     */
    @MethodLog
    @PostMapping
    public ResponseEntity login(@Valid @RequestBody UserLogin userLogin, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            ResultMap resultMap = new ResultMap().fail().message(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        User user = userService.userLogin(userLogin);
        if (!user.getActive()) {
            log.info("this user is not active： {}", userLogin.getUsername());
            ResultMap resultMap = new ResultMap().failWithToken(tokenUtils.generateToken(user)).message("this user is not active");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        UserLoginResult userLoginResult = new UserLoginResult(user);
        String statistic_open = environment.getProperty("statistic.enable");
        if("true".equalsIgnoreCase(statistic_open)){
            userLoginResult.setStatisticOpen(true);
        }

        return ResponseEntity.ok(new ResultMap().success(tokenUtils.generateToken(user)).payload(userLoginResult));
    }
}
