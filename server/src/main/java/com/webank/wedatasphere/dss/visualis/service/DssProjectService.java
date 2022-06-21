package com.webank.wedatasphere.dss.visualis.service;

import edp.davinci.core.common.ResultMap;

import java.util.Map;

public interface DssProjectService {

    /**
     * get default project.
     */
    ResultMap getDefaultProject(String userName) throws Exception;

    /**
     * 工程导出
     */
    ResultMap exportProject(Map<String, String> params, String userName) throws Exception;

    /**
     * 工程导入
     */
    ResultMap importProject(Map<String, String> params, String userName) throws Exception;

    /**
     * 工程复制
     */
    ResultMap copyProject(Map<String, String> params, String userName) throws Exception;

    /**
     * 读取工程
     */
    ResultMap readProject(String fileName, Long projectId, String userName) throws Exception;
}
