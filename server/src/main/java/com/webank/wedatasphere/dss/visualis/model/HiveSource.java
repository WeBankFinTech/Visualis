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
package com.webank.wedatasphere.dss.visualis.model;


import com.alibaba.fastjson.JSONObject;
import edp.davinci.model.Source;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * created by cooperyang on 2019/1/24
 * Description:
 */
@Slf4j
@Data
public class HiveSource extends Source {

    private Long id;
    private String name;

    private String description;

    private String type;

    private Long projectId;

    private String config;

    @Override
    public String getJdbcUrl() {
        String url = null;
        if (null == config) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(this.config);
            url = jsonObject.getString("url");
        } catch (Exception e) {
            log.error("get jdbc url from source config, {}", e.getMessage());
        }
        return url;
    }

    @Override
    public String getUsername() {
        String username = null;
        if (null == config) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(this.config);
            username = jsonObject.getString("username");
        } catch (Exception e) {
            log.error("get jdbc user from source config, {}", e.getMessage());
        }
        return username;
    }

    @Override
    public String getPassword() {
        String password = null;
        if (null == config) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(this.config);
            password = jsonObject.getString("password");
        } catch (Exception e) {
            log.error("get jdbc password from source config, {}", e.getMessage());
        }
        return password;
    }

    @Override
    public String getDatabase() {
        return "";
    }

    @Override
    public String getDbVersion() {
        return "";
    }

    @Override
    public boolean isExt() {
        return false;
    }
}
