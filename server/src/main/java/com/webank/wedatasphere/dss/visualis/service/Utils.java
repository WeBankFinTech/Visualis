package com.webank.wedatasphere.dss.visualis.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.webank.wedatasphere.dss.visualis.utils.StringConstant;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    public static Map<String, Set<Long>> getModuleIdsMap(Map<String, String> params) {

        Map<String, Set<Long>> map = Maps.newHashMap();
        String widgetIdsStr = params.get(StringConstant.WIDGET_IDS);
        String displayIdsStr = params.get(StringConstant.DISPLAY_IDS);
        String dashboardPortalIdsStr = params.get(StringConstant.DASHBOARD_PORTAL_IDS);
        String viewIdsStr = params.get(StringConstant.VIEW_IDS);

        Set<Long> widgetIds = Sets.newHashSet();
        Set<Long> displayIds = Sets.newHashSet();
        Set<Long> dashboardPortalIds = Sets.newHashSet();
        Set<Long> viewIds = Sets.newHashSet();

        if (StringUtils.isNotEmpty(widgetIdsStr)) {
            widgetIds = Arrays.stream(StringUtils.split(widgetIdsStr, StringConstant.COMMA))
                    .map(Long::parseLong).collect(Collectors.toSet());
        }
        if (StringUtils.isNotEmpty(displayIdsStr)) {
            displayIds = Arrays.stream(StringUtils.split(displayIdsStr, StringConstant.COMMA))
                    .map(Long::parseLong).collect(Collectors.toSet());
        }
        if (StringUtils.isNotEmpty(dashboardPortalIdsStr)) {
            dashboardPortalIds = Arrays.stream(StringUtils.split(dashboardPortalIdsStr, StringConstant.COMMA))
                    .map(Long::parseLong).collect(Collectors.toSet());
        }
        if (StringUtils.isNotEmpty(viewIdsStr)) {
            viewIds = Arrays.stream(StringUtils.split(viewIdsStr, StringConstant.COMMA))
                    .map(Long::parseLong).collect(Collectors.toSet());
        }

        map.put(StringConstant.WIDGET_IDS, widgetIds);
        map.put(StringConstant.DISPLAY_IDS, displayIds);
        map.put(StringConstant.DASHBOARD_PORTAL_IDS, dashboardPortalIds);
        map.put(StringConstant.VIEW_IDS, viewIds);

        return map;
    }

    public static String updateName(String name, String versionSuffix) {
        if (StringUtils.isBlank(versionSuffix)) {
            return name;
        }
        String shortName = getShortName(name);
        return shortName + "_" + versionSuffix;
    }

    private static String getShortName(String longName) {
        String shortName;
        String version = getSuffixVersion(longName);
        if (null == version) {
            shortName = longName;
        } else {
            shortName = longName.substring(0, longName.length() - version.length() - 1);
        }
        return shortName;
    }

    private static String getSuffixVersion(String longName) {
        String version;
        Pattern suffixVersionPattern = Pattern.compile("[v]\\d_[v][0-9]{6}");
        Matcher matcherVersionPattern = suffixVersionPattern.matcher(longName);
        if (matcherVersionPattern.find()) {
            version = matcherVersionPattern.group();
        } else {
            version = null;
        }
        return version;
    }

}
