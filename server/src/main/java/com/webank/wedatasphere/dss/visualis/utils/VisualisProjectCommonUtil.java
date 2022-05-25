package com.webank.wedatasphere.dss.visualis.utils;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VisualisProjectCommonUtil {

    public static String updateName(String name, String versionSuffix) {
        if (StringUtils.isBlank(versionSuffix)) {
            return name;
        }

        //节点截取前面一段字符，如widget_5979_v1_v000007截取widget_5979
        Pattern pattern1 = Pattern.compile("([a-zA-Z]+_\\d+).*");
        Matcher matcher = pattern1.matcher(name);
        if (matcher.find()) {
            return matcher.group(1) + "_" + versionSuffix;
        } else {
            //数据源名字匹配
            Pattern pattern2 = Pattern.compile("(\\S+)_v\\S+");
            Matcher matcher2 = pattern2.matcher(name);
            if (matcher2.find()) {
                return matcher2.group(1) + "_" + versionSuffix;
            }
        }
        return name + "_" + versionSuffix;
    }


}
