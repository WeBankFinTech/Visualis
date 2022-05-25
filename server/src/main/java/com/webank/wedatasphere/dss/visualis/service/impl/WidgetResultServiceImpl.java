package com.webank.wedatasphere.dss.visualis.service.impl;

import com.google.common.collect.Iterables;
import com.webank.wedatasphere.dss.visualis.exception.VGErrorException;
import com.webank.wedatasphere.dss.visualis.query.model.VirtualView;
import com.webank.wedatasphere.dss.visualis.query.utils.QueryUtils;
import com.webank.wedatasphere.dss.visualis.service.WidgetResultService;
import org.apache.linkis.adapt.LinkisUtils;
import org.apache.linkis.common.exception.ErrorException;
import edp.core.exception.ServerException;
import edp.core.model.PaginateWithQueryColumns;
import edp.davinci.dao.WidgetMapper;
import edp.davinci.model.Widget;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WidgetResultServiceImpl implements WidgetResultService {

    private static Logger log = LoggerFactory.getLogger(WidgetResultServiceImpl.class);

    @Autowired
    WidgetMapper widgetMapper;

    @SuppressWarnings("unchecked")
    @Override
    public boolean updateContextId(Long widgetId, String contextId) throws VGErrorException {

        Widget widget = widgetMapper.getById(widgetId);
        if (widget == null) {
            throw new ServerException("Widget does not exist");
        }
        Map<String, Object> configMap = LinkisUtils.gson().fromJson(widget.getConfig(), Map.class);
        if (configMap.get("contextId") == null) {
            throw new ServerException("This Widget does not have contextId");
        }
        String encodedContextId = QueryUtils.encodeContextId(contextId);
        String nodeName = (String) configMap.get("refNodeName");
        if (StringUtils.isNotBlank(nodeName)) {
            try {
                VirtualView virtualView = Iterables.getFirst(QueryUtils.getFromContext(encodedContextId, nodeName), null);
                if (virtualView != null) {
                    configMap.put("view", virtualView);
                }
            } catch (ErrorException e) {
                log.error("Get visualView error by ContextID: {} and nodeName: {}", contextId, nodeName);
                throw new VGErrorException(20003, "get visualView error, due to error error contextId and nodeName.");
            }
        }

        Object viewObj = configMap.get("view");
        if (viewObj != null && (viewObj instanceof Map)) {
            Map<String, Object> viewMap = (Map<String, Object>) viewObj;
            if (viewMap.size() > 0) {
                Map<String, Object> sourceMap = (Map<String, Object>) viewMap.get("source");
                Map<String, Object> dataSourceContentMap = (Map<String, Object>) sourceMap.get("dataSourceContent");
                dataSourceContentMap.put("contextId", contextId);
                sourceMap.put("dataSourceContent", dataSourceContentMap);
                viewMap.put("source", sourceMap);
                configMap.put("view", viewMap);
            }
        }
        log.info("widget:{}", widget);
        configMap.put("contextId", QueryUtils.encodeContextId(contextId));
        widget.setConfig(LinkisUtils.gson().toJson(configMap));
        widgetMapper.update(widget);

        return true;
    }

    @Override
    public PaginateWithQueryColumns getWidgetData() {
        return null;
    }
}
