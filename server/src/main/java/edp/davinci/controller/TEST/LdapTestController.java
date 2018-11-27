/*
 * <<
 * Davinci
 * ==
 * Copyright (C) 2016 - 2018 EDP
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * >>
 */

package edp.davinci.controller.TEST;

import edp.davinci.model.LdapUser;
import edp.davinci.service.LdapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.NamingException;

@RestController
@RequestMapping("/test")
public class LdapTestController {

    @Autowired
    LdapService ldapService;

    @GetMapping("/user")
    public LdapUser findUser() {
        LdapUser ldapUser = null;
        try {
            ldapUser = ldapService.findByUsername("mengmengshan@creditease.cn", "ShanMengm3410!");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return ldapUser;
    }
}
