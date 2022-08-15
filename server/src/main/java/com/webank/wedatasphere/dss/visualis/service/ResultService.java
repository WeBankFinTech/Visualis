package com.webank.wedatasphere.dss.visualis.service;

import java.io.File;

public interface ResultService {

    // 1. 存储结果集
    void setResult(String keyValue, File object);

    // 2. 获取结果集
    Object getResult(String key);

    // 3. 判断结果集是否存在
    boolean exist(String key);

}
