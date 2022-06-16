package com.webank.wedatasphere.dss.visualis.query.utils;

import com.webank.wedatasphere.dss.visualis.configuration.CommonConfig;

public class EnvLimitUtils {

    public static final String BDP_PROD = "BDP_PROD";
    public static final String BDAP_PROD = "BDAP_PROD";
    public static final String ERROR_MESSAGE = "BDP生产环境不允许进行此操作！";

    public static boolean notPermitted() {
        if (BDP_PROD.equals(CommonConfig.DEPLOY_ENV().getValue())) {
            return true;
        }
        return false;
    }

    public static boolean isProdEnv() {
        if (BDAP_PROD.equals(CommonConfig.DEPLOY_ENV().getValue())) {
            return true;
        }
        return false;
    }
}
