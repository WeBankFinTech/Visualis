package com.webank.wedatasphere.dss.visualis.query.service;

import edp.core.model.Paginate;
import edp.davinci.dto.viewDto.DistinctParam;
import edp.davinci.dto.viewDto.ViewExecuteParam;
import edp.davinci.model.User;

import java.util.List;
import java.util.Map;

public interface VirtualViewQueryService {

    Paginate<Map<String, Object>> getData(ViewExecuteParam executeParam, User user, boolean async) throws Exception;

    List<Map<String, Object>> getDistinctValue(DistinctParam param, User user) throws Exception;
}
