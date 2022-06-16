package com.webank.wedatasphere.dss.visualis.query.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.webank.wedatasphere.dss.visualis.query.generator.VirtualSqlStatementGenerator;
import com.webank.wedatasphere.dss.visualis.query.initializer.*;
import com.webank.wedatasphere.dss.visualis.query.utils.QueryUtils;
import com.webank.wedatasphere.dss.visualis.ujes.UJESJob;
import edp.core.model.Paginate;
import edp.core.utils.SqlUtils;
import edp.davinci.dto.viewDto.DistinctParam;
import edp.davinci.dto.viewDto.ViewExecuteParam;
import edp.davinci.model.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class VirtualViewQueryServiceImpl implements VirtualViewQueryService, InitializingBean {

    @Autowired
    SqlUtils sqlUtils;

    @Autowired
    VirtualSqlStatementGenerator virtualSqlStatementGenerator;

    @Autowired
    ResultSetSourceInitializer resultSetSourceInitializer;

    @Autowired
    ContextSourceInitializer contextSourceInitializer;

    @Autowired
    UrlSourceInitializer urlSourceInitializer;

    Map<String, SourceInitializer> typeToSourceInitializer;

    @Override
    public void afterPropertiesSet() {
        sqlUtils = sqlUtils.init(null);
        typeToSourceInitializer = Maps.newHashMap();
        typeToSourceInitializer.put(resultSetSourceInitializer.getType(), resultSetSourceInitializer);
        typeToSourceInitializer.put(contextSourceInitializer.getType(), contextSourceInitializer);
        typeToSourceInitializer.put(urlSourceInitializer.getType(), urlSourceInitializer);
    }

    public Paginate<Map<String, Object>> getData(ViewExecuteParam executeParam, User user, boolean async) throws Exception {
        SourceInitializer sourceInitializer = typeToSourceInitializer.get(executeParam.getView().getSource().getDataSourceType());
        SourceInitJob sourceInitJob = sourceInitializer.init(executeParam.getView(), user);
        String queryScripts = virtualSqlStatementGenerator.generate(executeParam.getView(), executeParam, user);
        String contextId = executeParam.getView().getSource().getDataSourceContent().get("contextId");
        String nodeName = executeParam.getView().getSource().getDataSourceContent().get("nodeName");
        contextId = contextId == null ? "" : contextId;
        String jobType = UJESJob.SQL_TYPE();
        if (sourceInitJob != null) {
            queryScripts = QueryUtils.getQueryTempViewScala(queryScripts, sourceInitJob.getScriptContent());
            jobType = sourceInitJob.getScriptType();
        }
        String linkisJob = QueryUtils.getLinkisSparkJob(
                user,
                executeParam.getView().getName(),
                queryScripts,
                jobType,
                executeParam.getView().getSource().getCreator(),
                executeParam.getView().getSource().getEngineType(),
                contextId,
                nodeName,
                executeParam.getCache(),
                executeParam.getExpired(),
                executeParam.getCache(),
                executeParam.getExpired());
        if (async) {
            return sqlUtils.asyncQuery4Exec(
                    linkisJob,
                    executeParam.getPageNo(),
                    executeParam.getPageSize(),
                    executeParam.getTotalCount(),
                    executeParam.getLimit(),
                    Sets.newHashSet());
        } else {
            return sqlUtils.syncQuery4Paginate(
                    linkisJob,
                    executeParam.getPageNo(),
                    executeParam.getPageSize(),
                    executeParam.getTotalCount(),
                    executeParam.getLimit(),
                    Sets.newHashSet()
            );
        }
    }

    @Override
    public List<Map<String, Object>> getDistinctValue(DistinctParam param, User user) throws Exception {
        SourceInitializer sourceInitializer = typeToSourceInitializer.get(param.getView().getSource().getDataSourceType());
        SourceInitJob sourceInitJob = sourceInitializer.init(param.getView(), user);
        String queryScripts = virtualSqlStatementGenerator.generateDistinct(param.getView(), param, user);
        String contextId = param.getView().getSource().getDataSourceContent().get("contextId");
        contextId = contextId == null ? "" : contextId;
        String nodeName = param.getView().getSource().getDataSourceContent().get("nodeName");
        String jobType = UJESJob.SQL_TYPE();
        if (sourceInitJob != null) {
            queryScripts = QueryUtils.getQueryTempViewScala(queryScripts, sourceInitJob.getScriptContent());
            jobType = sourceInitJob.getScriptType();
        }
        String linkisJob = QueryUtils.getLinkisSparkJob(
                user,
                param.getView().getName(),
                queryScripts, jobType,
                param.getView().getSource().getCreator(),
                param.getView().getSource().getEngineType(),
                contextId,
                nodeName,
                param.getCache(),
                param.getExpired(),
                param.getCache(),
                param.getExpired());
        return sqlUtils.query4List(linkisJob, -1);
    }

}
