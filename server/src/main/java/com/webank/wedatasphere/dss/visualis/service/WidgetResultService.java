package com.webank.wedatasphere.dss.visualis.service;

import com.webank.wedatasphere.dss.visualis.exception.VGErrorException;
import edp.core.model.PaginateWithQueryColumns;

public interface WidgetResultService {

    /**
     * When the DSS workflow executes, the ContextID is updated,
     * return ture update success, otherwise update false.
     * */
     boolean updateContextId(Long widgetId, String contextId) throws VGErrorException;


     /**
      *
      * */
    PaginateWithQueryColumns getWidgetData();
}
