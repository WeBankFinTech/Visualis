package com.webank.wedatasphere.dss.visualis.service;


import edp.davinci.model.Params;

import java.util.List;
import java.util.Map;

public interface ParamsService {

    void insertParams(Params params) throws Exception;

    List<Map<String, Object>> getGraphInfo(String projectName, String userName) throws Exception;
}
