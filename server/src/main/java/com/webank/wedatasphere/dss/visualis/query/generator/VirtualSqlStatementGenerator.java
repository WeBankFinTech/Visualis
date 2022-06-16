package com.webank.wedatasphere.dss.visualis.query.generator;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.webank.wedatasphere.dss.visualis.query.model.VirtualView;
import com.webank.wedatasphere.dss.visualis.query.utils.ChartUtils;
import com.webank.wedatasphere.dss.visualis.utils.VisualisUtils;
import edp.core.consts.Consts;
import edp.core.utils.CollectionUtils;
import edp.core.utils.SqlUtils;
import edp.davinci.core.common.Constants;
import edp.davinci.core.model.SqlEntity;
import edp.davinci.core.model.SqlFilter;
import edp.davinci.core.utils.SqlParseUtils;
import edp.davinci.dao.SourceMapper;
import edp.davinci.dto.viewDto.DistinctParam;
import edp.davinci.dto.viewDto.ViewExecuteParam;
import edp.davinci.model.Source;
import edp.davinci.model.SqlVariable;
import edp.davinci.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edp.core.consts.Consts.MINUS;

@Slf4j
@Component
public class VirtualSqlStatementGenerator implements StatementGenerator {

    @Autowired
    private SqlUtils sqlUtils;

    @Autowired
    private SqlParseUtils sqlParseUtils;

    @Autowired
    private SourceMapper sourceMapper;

    @Value("${sql_template_delimiter:$}")
    private String sqlTempDelimiter;

    @Override
    public String generate(VirtualView virtualView, ViewExecuteParam executeParam, User user) {
        ChartUtils.processViewExecuteParam(executeParam);
        List<SqlVariable> variables = virtualView.getVariables();
        //解析sql
        SqlEntity sqlEntity = sqlParseUtils.parseSql(virtualView.getSql(), variables, sqlTempDelimiter);
        //列权限（只记录被限制访问的字段）
        Set<String> excludeColumns = new HashSet<>();
        String srcSql = sqlParseUtils.replaceParams(sqlEntity.getSql(), sqlEntity.getQuaryParams(), sqlEntity.getAuthParams(), sqlTempDelimiter, user);

        List<String> executeSqlList = sqlParseUtils.getSqls(srcSql, false);
        List<String> querySqlList = sqlParseUtils.getSqls(srcSql, true);

        if (!CollectionUtils.isEmpty(querySqlList)) {

            if (null != executeParam
                    && null != executeParam.getCache()
                    && executeParam.getCache()
                    && executeParam.getExpired() > 0L) {

                StringBuilder slatBuilder = new StringBuilder();
                slatBuilder.append(executeParam.getPageNo());
                slatBuilder.append(MINUS);
                slatBuilder.append(executeParam.getLimit());
                slatBuilder.append(MINUS);
                slatBuilder.append(executeParam.getPageSize());
                excludeColumns.forEach(slatBuilder::append);

            }

        }
        buildQuerySql(querySqlList, executeParam);
        return String.join(Consts.SEMICOLON, querySqlList);
    }

    @Override
    public String generateDistinct(VirtualView virtualView, DistinctParam param, User user) {
        List<SqlVariable> variables = virtualView.getVariables();
        SqlEntity sqlEntity = sqlParseUtils.parseSql(virtualView.getSql(), variables, sqlTempDelimiter);
        String srcSql = sqlParseUtils.replaceParams(sqlEntity.getSql(), sqlEntity.getQuaryParams(), sqlEntity.getAuthParams(), sqlTempDelimiter, user);

        Source source = sourceMapper.getById(VisualisUtils.getHiveDataSourceId());

        SqlUtils sqlUtils = this.sqlUtils.init(source);

        List<String> executeSqlList = sqlParseUtils.getSqls(srcSql, false);
        List<String> querySqlList = sqlParseUtils.getSqls(srcSql, true);
        if (!CollectionUtils.isEmpty(querySqlList)) {
            String cacheKey = null;
            if (null != param) {
                STGroup stg = new STGroupFile(Constants.SQL_TEMPLATE);
                ST st = stg.getInstanceOf("queryDistinctSql");
                st.add("columns", param.getColumns());
                st.add("filters", convertFilters(param.getFilters(), source));
                st.add("sql", querySqlList.get(querySqlList.size() - 1));
                st.add("keywordPrefix", SqlUtils.getKeywordPrefix(source.getJdbcUrl(), source.getDbVersion()));
                st.add("keywordSuffix", SqlUtils.getKeywordSuffix(source.getJdbcUrl(), source.getDbVersion()));

                String sql = st.render();
                querySqlList.set(querySqlList.size() - 1, sql);
            }
        }
        return String.join(Consts.SEMICOLON, querySqlList);
    }


    public void buildQuerySql(List<String> querySqlList, ViewExecuteParam executeParam) {
        Source source = sourceMapper.getById(VisualisUtils.getHiveDataSourceId());
        if (null != executeParam) {
            //构造参数， 原有的被传入的替换
            STGroup stg = new STGroupFile(Constants.SQL_TEMPLATE);
            ST st = stg.getInstanceOf("querySql");
            st.add("nativeQuery", executeParam.isNativeQuery());
            st.add("groups", executeParam.getGroups());

            if (executeParam.isNativeQuery()) {
                st.add("aggregators", executeParam.getAggregators());
            } else {
                st.add("aggregators", executeParam.getAggregators(source.getJdbcUrl(), source.getDbVersion()));
            }
            st.add("orders", executeParam.getOrders(source.getJdbcUrl(), source.getDbVersion()));
            st.add("filters", convertFilters(executeParam.getFilters(), source));
            st.add("keywordPrefix", sqlUtils.getKeywordPrefix(source.getJdbcUrl(), source.getDbVersion()));
            st.add("keywordSuffix", sqlUtils.getKeywordSuffix(source.getJdbcUrl(), source.getDbVersion()));

            for (int i = 0; i < querySqlList.size(); i++) {
                st.add("sql", querySqlList.get(i));
                querySqlList.set(i, st.render());
            }

        }
    }

    public List<String> convertFilters(List<String> filterStrs, Source source) {
        List<String> whereClauses = new ArrayList<>();
        List<SqlFilter> filters = new ArrayList<>();
        try {
            if (null == filterStrs || filterStrs.isEmpty()) {
                return null;
            }

            for (String str : filterStrs) {
                SqlFilter obj = JSON.parseObject(str, SqlFilter.class);
                if (!StringUtils.isEmpty(obj.getName())) {
                    obj.setName(ViewExecuteParam.getField(obj.getName(), source.getJdbcUrl(), source.getDbVersion()));
                }
                filters.add(obj);
            }
            filters.forEach(filter -> whereClauses.add(SqlFilter.dealFilter(filter)));

        }catch (Exception e){
            log.error("convertFilters error . filterStrs = {}, source = {}, filters = {} , whereClauses = {} ",
                    filterStrs, source, filters, whereClauses);
            throw e;
        }
        return whereClauses;
    }
}
