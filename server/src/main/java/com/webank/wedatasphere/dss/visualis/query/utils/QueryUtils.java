package com.webank.wedatasphere.dss.visualis.query.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.wedatasphere.dss.visualis.query.model.VirtualSource;
import com.webank.wedatasphere.dss.visualis.query.model.VirtualView;
import com.webank.wedatasphere.dss.visualis.res.ResultHelper;
import com.webank.wedatasphere.dss.visualis.ujes.UJESJob;
import com.webank.wedatasphere.dss.visualis.utils.VisualisUtils;
import org.apache.linkis.adapt.LinkisUtils;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.cs.client.service.CSTableService;
import org.apache.linkis.cs.client.utils.SerializeHelper;
import org.apache.linkis.cs.common.entity.metadata.CSTable;
import org.apache.linkis.cs.common.entity.metadata.Column;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.utils.CSCommonUtils;
import org.apache.linkis.server.BDPJettyServerHelper;
import edp.davinci.common.model.VisualViewModel;
import edp.davinci.model.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryUtils {

    private static final Logger logger = LoggerFactory.getLogger(QueryUtils.class);


    final static Base64.Decoder decoder = Base64.getDecoder();
    final static Base64.Encoder encoder = Base64.getEncoder();

    public static String getLinkisSparkJob(User user, String scriptName, String script, String jobType, String creator, String engine, String contextId, String nodeName, Boolean cache, Long cacheExpireAfter, Boolean readFromCache, Long readCacheBefore) {
        HashMap<String, String> sourceMap = Maps.newHashMap();
        sourceMap.put("fileName", scriptName);
        UJESJob ujesJob = new UJESJob(script, user.getName(), jobType, sourceMap, creator, engine, nodeName, contextId, cache, cacheExpireAfter, readFromCache, readCacheBefore);
        return BDPJettyServerHelper.gson().toJson(ujesJob);
    }

    public static List<VirtualView> getFromContext(String encodedContextId, String nodeName) throws ErrorException {
        List<VirtualView> virtualViews = Lists.newArrayList();
        String contextId = decodeContextId(encodedContextId);
        CSTableService csTableService = CSTableService.getInstance();
        List<ContextKeyValue> csTables = csTableService.searchUpstreamTableKeyValue(contextId, nodeName);
        for (ContextKeyValue contextKeyValue : csTables) {
            VirtualView virtualView = getVirtualViewByContextKeyValue(contextId, nodeName, contextKeyValue);
            virtualViews.add(virtualView);
        }
        return virtualViews;
    }

    public static VirtualView getExactFromContext(String encodedContextId, String nodeName) throws ErrorException {
        VirtualView virtualView = null;
        String contextId = decodeContextId(encodedContextId);
        CSTableService csTableService = CSTableService.getInstance();
        List<ContextKeyValue> csTables = csTableService.searchUpstreamTableKeyValue(contextId, nodeName);
        for (ContextKeyValue contextKeyValue : csTables) {
            if (nodeName.equals(StringUtils.substringBetween(contextKeyValue.getContextKey().getKey(), CSCommonUtils.NODE_PREFIX, "."))) {
                virtualView = getVirtualViewByContextKeyValue(contextId, nodeName, contextKeyValue);
                return virtualView;
            }
        }
        return virtualView;
    }

    public static VirtualView refreshFromContext(VirtualView virtualView) throws ErrorException {
        String contextId = virtualView.getSource().getDataSourceContent().get("contextId");
        String contextKey = virtualView.getSource().getDataSourceContent().get("contextKey");
        CSTableService csTableService = CSTableService.getInstance();
        CSTable csTable = csTableService.getCSTable(contextId, contextKey);
        if (csTable == null) {
            logger.info("use nodeName to refresh context, because no metadata found by contextId[" + contextId + "] and contextKey[" + contextKey + "]");
            String nodeName = virtualView.getSource().getDataSourceContent().get("nodeName");
            ContextKeyValue contextKeyValue = Iterables.getFirst(csTableService.searchUpstreamTableKeyValue(contextId, nodeName), null);
            if (contextKeyValue == null) {
                logger.warn("no metadata found by contextId[" + contextId + "] and nodeName[" + nodeName + "]");
                return virtualView;
            }
            logger.info("found metadata by nodeName[" + nodeName + "] , key[" + contextKeyValue.getContextKey().getKey() + "]");
            String nodeNameFromKey = StringUtils.substringBetween(contextKeyValue.getContextKey().getKey(), CSCommonUtils.NODE_PREFIX, ".");
            if (!nodeName.equals(nodeNameFromKey)) {
                logger.warn("metadata node does not match! expected:[" + nodeName + "], actual:[" + nodeNameFromKey + "]");
                return virtualView;
            }
            return getVirtualViewByContextKeyValue(contextId, nodeName, contextKeyValue);
        }
        virtualView.getSource().getDataSourceContent().put("tableName", csTable.getName());
        if (csTable.getDb() != null) {
            virtualView.getSource().getDataSourceContent().put("dbName", csTable.getDb().getName());
        }
        if (StringUtils.isNotBlank(csTable.getLocation())) {
            virtualView.getSource().getDataSourceContent().put("location", csTable.getLocation());
        }
        return virtualView;
    }

    public static VirtualView getVirtualViewByContextKeyValue(String contextId, String nodeName, ContextKeyValue contextKeyValue) throws ErrorException {
        CSTable csTable = (CSTable) (contextKeyValue.getContextValue().getValue());
        VirtualView virtualView = new VirtualView();
        VirtualSource virtualSource = new VirtualSource();
        virtualSource.setCreator(VisualisUtils.VG_CREATOR().getValue());
        virtualSource.setEngineType(VisualisUtils.SPARK().getValue());
        virtualSource.setDataSourceType("context");
        Map<String, String> dataSourceContent = Maps.newHashMap();
        dataSourceContent.put("contextId", contextId);
        dataSourceContent.put("nodeName", nodeName);
        dataSourceContent.put("contextKey", SerializeHelper.serializeContextKey(contextKeyValue.getContextKey()));
        dataSourceContent.put("tableName", csTable.getName());
        if (csTable.getDb() != null) {
            dataSourceContent.put("dbName", csTable.getDb().getName());
        }
        if (StringUtils.isNotBlank(csTable.getLocation())) {
            dataSourceContent.put("location", csTable.getLocation());
        }
        virtualSource.setDataSourceContent(dataSourceContent);

        virtualView.setSource(virtualSource);
        virtualView.setName(csTable.getName());
        Map<String, Object> model = Maps.newLinkedHashMap();
        for (Column column : csTable.getColumns()) {
            String sqlType = column.getType().toUpperCase();
            String visualType = ResultHelper.toVisualType(sqlType);
            String modelType = ResultHelper.NUMBER_TYPE().equals(visualType) ? "value" : "category";
            VisualViewModel visualViewModel = new VisualViewModel();
            visualViewModel.setSqlType(sqlType);
            visualViewModel.setVisualType(visualType);
            visualViewModel.setModelType(modelType);
            model.put(column.getName(), visualViewModel);
        }
        virtualView.setModel(LinkisUtils.gson().toJson(model));
        virtualView.setParams(Maps.newHashMap());
        return virtualView;
    }

    public static String encodeContextId(String contextId) {
        return encoder.encodeToString(contextId.getBytes());
    }

    public static String decodeContextId(String encodedContextId) {
        return new String(decoder.decode(encodedContextId));
    }

    public static String getCreateTempViewScala(String tempViewName, String resultLocation) {
        return "org.apache.spark.sql.execution.datasources.csv.DolphinToSpark.createTempView(spark,\""
                + tempViewName + "\",\"" + resultLocation + "\", true);";
    }

    public static String getQueryTempViewScala(String sql, String createTempViewScala) {
        return "val sql = \"\"\" " + sql + "\"\"\"\n"
                + createTempViewScala
                + "show(spark.sql(sql))";
    }

}
