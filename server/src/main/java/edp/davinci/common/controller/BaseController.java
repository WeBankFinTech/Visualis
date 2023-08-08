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

package edp.davinci.common.controller;

import edp.core.utils.TokenUtils;
import edp.davinci.dao.DashboardPortalMapper;
import edp.davinci.dao.DisplayMapper;
import edp.davinci.dao.ViewMapper;
import edp.davinci.dao.WidgetMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BaseController {

    //
    protected Map<String, String> deleteIdNotFound;
    {
        deleteIdNotFound = new HashMap<>();
        deleteIdNotFound.put("errorMsg", "idvalidId");
    }

    @Autowired
    public TokenUtils tokenUtils;

    @Autowired
    ViewMapper viewMapper;

    @Autowired
    WidgetMapper widgetMapper;

    @Autowired
    DisplayMapper displayMapper;

    @Autowired
    DashboardPortalMapper dashboardPortalMapper;

    public boolean invalidId(String value) {
        return StringUtils.isEmpty(value);
    }

    public boolean invalidId(Long value) {
        return (null == value || value < 1L);
    }

    public boolean invalidId(Long value, String component) {
        if (StringUtils.isEmpty(component)) {
            return invalidId(value);
        }

        boolean result = false;
        switch (component) {
            case "view":
                result = viewMapper.getById(value) == null;
                break;
            case "widget":
                result = widgetMapper.getById(value) == null;
                break;
            case "display":
                result = displayMapper.getById(value) == null;
                break;
            case "dashboard":
                result = dashboardPortalMapper.getById(value) == null;
                break;
            default:
                result = false;
        }
        return result;
    }
}
