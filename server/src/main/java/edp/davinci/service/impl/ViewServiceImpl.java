/*
 * <<
 *  Davinci
 *  ==
 *  Copyright (C) 2016 - 2019 EDP
 *  ==
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  >>
 *
 */

package edp.davinci.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.wedatasphere.dss.visualis.auth.ProjectAuth;
import com.webank.wedatasphere.dss.visualis.model.PaginateWithExecStatus;
import com.webank.wedatasphere.dss.visualis.query.utils.ChartUtils;
import com.webank.wedatasphere.dss.visualis.query.utils.EnvLimitUtils;
import com.webank.wedatasphere.dss.visualis.query.utils.JdbcAsyncUtils;
import com.webank.wedatasphere.dss.visualis.ujes.UJESJob;
import com.webank.wedatasphere.dss.visualis.utils.VisualisUtils;
import org.apache.linkis.adapt.LinkisUtils;
import org.apache.linkis.entrance.utils.JobHistoryHelper;
import org.apache.linkis.protocol.query.cache.CacheTaskResult;
import edp.core.consts.Consts;
import edp.core.exception.NotFoundException;
import edp.core.exception.ServerException;
import edp.core.exception.UnAuthorizedExecption;
import edp.core.model.BaseSource;
import edp.core.model.Paginate;
import edp.core.model.PaginateWithQueryColumns;
import edp.core.utils.CollectionUtils;
import edp.core.utils.RedisUtils;
import edp.core.utils.SqlUtils;
import edp.davinci.common.utils.ComponentFilterUtils;
import edp.davinci.core.common.Constants;
import edp.davinci.core.enums.LogNameEnum;
import edp.davinci.core.enums.SqlVariableTypeEnum;
import edp.davinci.core.enums.SqlVariableValueTypeEnum;
import edp.davinci.core.enums.UserPermissionEnum;
import edp.davinci.core.model.SqlEntity;
import edp.davinci.core.model.SqlFilter;
import edp.davinci.core.utils.SqlParseUtils;
import edp.davinci.dao.RelRoleViewMapper;
import edp.davinci.dao.SourceMapper;
import edp.davinci.dao.ViewMapper;
import edp.davinci.dao.WidgetMapper;
import edp.davinci.dto.projectDto.ProjectDetail;
import edp.davinci.dto.projectDto.ProjectPermission;
import edp.davinci.dto.sourceDto.SourceBaseInfo;
import edp.davinci.dto.sourceDto.SourceWithProject;
import edp.davinci.dto.viewDto.*;
import edp.davinci.model.*;
import edp.davinci.service.ProjectService;
import edp.davinci.service.SourceService;
import edp.davinci.service.ViewService;
import edp.davinci.service.excel.SQLContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static edp.core.consts.Consts.COMMA;
import static edp.davinci.core.common.Constants.N0_AUTH_PERMISSION;
import static edp.davinci.core.enums.SqlVariableTypeEnum.AUTHVARE;
import static edp.davinci.core.enums.SqlVariableTypeEnum.QUERYVAR;

@Slf4j
@Service("viewService")
public class ViewServiceImpl implements ViewService {

    private static final Logger optLogger = LoggerFactory.getLogger(LogNameEnum.BUSINESS_OPERATION.getName());

    @Autowired
    private ViewMapper viewMapper;

    @Autowired
    private SourceMapper sourceMapper;

    @Autowired
    private WidgetMapper widgetMapper;

    @Autowired
    private RelRoleViewMapper relRoleViewMapper;

    @Autowired
    private SqlUtils sqlUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private SqlParseUtils sqlParseUtils;

    @Value("${sql_template_delimiter:$}")
    private String sqlTempDelimiter;

    @Autowired
    private ProjectAuth projectAuth;

    private static final String SQL_VARABLE_KEY = "name";

    @Override
    public synchronized boolean isExist(String name, Long id, Long projectId) {
        Long viewId = viewMapper.getByNameWithProjectId(name, projectId);
        if (null != id && null != viewId) {
            return !id.equals(viewId);
        }
        return null != viewId && viewId.longValue() > 0L;
    }

    /**
     * 获取View列表
     *
     * @param projectId
     * @param user
     * @return
     */
    @Override
    public List<ViewBaseInfo> getViews(Long projectId, User user) throws NotFoundException, UnAuthorizedExecption, ServerException {

        ProjectDetail projectDetail = null;
        try {
            projectDetail = projectService.getProjectDetail(projectId, user, false);
        } catch (Exception e) {
            log.error("get project detail fail, because: ", e);
            throw new RuntimeException("get project detail fail");
        }

        List<ViewBaseInfo> views = null;
        try {
            views = viewMapper.getViewBaseInfoByProject(projectId);
        } catch (Exception e) {
            log.error("get views fail, because: ", e);
            throw new RuntimeException("get views fail");
        }

        if (null != views) {
            ProjectPermission projectPermission = null;
            try {
                projectPermission = projectService.getProjectPermission(projectDetail, user);
            } catch (Exception e) {
                log.error("get project permission fail, because: ", e);
                throw new RuntimeException("get project permission fail");
            }
            if (projectPermission.getVizPermission() == UserPermissionEnum.HIDDEN.getPermission() &&
                    projectPermission.getWidgetPermission() == UserPermissionEnum.HIDDEN.getPermission() &&
                    projectPermission.getViewPermission() == UserPermissionEnum.HIDDEN.getPermission()) {
                return null;
            }
        }

        ComponentFilterUtils filter = new ComponentFilterUtils();
        views = filter.doFilterViews(views);

        return views;
    }

    @Override
    public ViewWithSourceBaseInfo getView(Long id, User user) throws NotFoundException, UnAuthorizedExecption, ServerException {
        ViewWithSourceBaseInfo view = null;
        try {
            view = viewMapper.getViewWithSourceBaseInfo(id);
        } catch (Exception e) {
            log.error("get view fail, because: ", e);
            throw new RuntimeException("get view fail");
        }
        if (null == view) {
            throw new NotFoundException("view is not found");
        }

        ProjectDetail projectDetail = null;
        try {
            projectDetail = projectService.getProjectDetail(view.getProjectId(), user, false);
        } catch (Exception e) {
            log.error("get project detail fail, because: ", e);
            throw new RuntimeException("get project detail fail");
        }
        ProjectPermission projectPermission = null;
        try {
            projectPermission = projectService.getProjectPermission(projectDetail, user);
        } catch (Exception e) {
            log.error("get project permission fail, because: ", e);
            throw new RuntimeException("get project permission fail");
        }
        if (projectPermission.getVizPermission() == UserPermissionEnum.HIDDEN.getPermission() &&
                projectPermission.getWidgetPermission() == UserPermissionEnum.HIDDEN.getPermission() &&
                projectPermission.getViewPermission() == UserPermissionEnum.HIDDEN.getPermission()) {
            throw new UnAuthorizedExecption("Insufficient permissions");
        }

        List<RelRoleView> relRoleViews = relRoleViewMapper.getByView(view.getId());
        view.setRoles(relRoleViews);

        return view;
    }

    @Override
    public SQLContext getSQLContext(boolean isMaintainer, ViewWithSource viewWithSource, ViewExecuteParam executeParam, User user) {
        if (null == executeParam || (CollectionUtils.isEmpty(executeParam.getGroups()) && CollectionUtils.isEmpty(executeParam.getAggregators()))) {
            return null;
        }
        if (null == viewWithSource.getSource()) {
            throw new NotFoundException("source is not found");
        }
        if (StringUtils.isEmpty(viewWithSource.getSql())) {
            throw new NotFoundException("sql is not found");
        }

        SQLContext context = new SQLContext();
        //解析变量
        List<SqlVariable> variables = viewWithSource.getVariables();
        //解析sql
        SqlEntity sqlEntity = sqlParseUtils.parseSql(viewWithSource.getSql(), variables, sqlTempDelimiter);
        //列权限（只记录被限制访问的字段）
        Set<String> excludeColumns = new HashSet<>();

        packageParams(isMaintainer, viewWithSource.getId(), sqlEntity, variables, executeParam.getParams(), excludeColumns, user);

        String srcSql = sqlParseUtils.replaceParams(sqlEntity.getSql(), sqlEntity.getQuaryParams(), sqlEntity.getAuthParams(), sqlTempDelimiter, user);
        context.setExecuteSql(sqlParseUtils.getSqls(srcSql, Boolean.FALSE));

        List<String> querySqlList = sqlParseUtils.getSqls(srcSql, Boolean.TRUE);
        if (!CollectionUtils.isEmpty(querySqlList)) {
            Source source = viewWithSource.getSource();
            buildQuerySql(querySqlList, source, executeParam);
            executeParam.addExcludeColumn(excludeColumns, source.getJdbcUrl(), source.getDbVersion());
            context.setQuerySql(querySqlList);
            context.setViewExecuteParam(executeParam);
        }
        if (!CollectionUtils.isEmpty(excludeColumns)) {
            List<String> excludeList = excludeColumns.stream().collect(Collectors.toList());
            context.setExcludeColumns(excludeList);
        }
        return context;
    }

    /**
     * 新建View
     *
     * @param viewCreate
     * @param user
     * @return
     */
    @Override
    @Transactional
    public ViewWithSourceBaseInfo createView(ViewCreate viewCreate, User user, String ticketId) throws NotFoundException, UnAuthorizedExecption, ServerException {
        ProjectDetail projectDetail = null;
        try {
            projectDetail = projectService.getProjectDetail(viewCreate.getProjectId(), user, false);
        } catch (Exception e) {
            log.error("get project detail fail, because: ", e);
            throw new RuntimeException("get project detail fail");
        }
        ProjectPermission projectPermission = null;
        try {
            projectPermission = projectService.getProjectPermission(projectDetail, user);
        } catch (Exception e) {
            log.error("get project permission fail, because: ", e);
            throw new RuntimeException("get project permission fail");
        }

        if (projectPermission.getViewPermission() < UserPermissionEnum.WRITE.getPermission()) {
            throw new UnAuthorizedExecption("you have not permission to create view");
        }

        if (isExist(viewCreate.getName(), null, viewCreate.getProjectId())) {
            log.info("the view {} name is already taken", viewCreate.getName());
            throw new ServerException("the view name is already taken");
        }
        Long sourceId = viewCreate.getSourceId();
        Source source;

        if (sourceId == 0) {
            try {
                source = createViewByDss(viewCreate, user, ticketId);
            } catch (Exception e) {
                log.error("get source fail, because: ", e);
                throw new RuntimeException("get source fail");
            }
        } else {
            try {
                source = sourceMapper.getById(sourceId);
            } catch (Exception e) {
                log.error("get source fail, because: ", e);
                throw new RuntimeException("get source fail");
            }
            if (null == source) {
                log.info("source (:{}) not found", sourceId);
                throw new NotFoundException("source is not found");
            }
        }


        /**
         *update by johnnwang
         * 如果为hive数据源则直接保存
         */
        if (VisualisUtils.isLinkisDataSource(source)) {
            return createViewMethod(viewCreate, source);
        }
        //测试连接
        boolean testConnection = sqlUtils.init(source).testConnection();

        if (testConnection) {
            View view = new View().createdBy(user.getId());
            BeanUtils.copyProperties(viewCreate, view);

            int insert = viewMapper.insert(view);
            if (insert > 0) {
                optLogger.info("view ({}) is create by user (:{})", view.toString(), user.getId());
                if (!CollectionUtils.isEmpty(viewCreate.getRoles()) && !StringUtils.isEmpty(viewCreate.getVariable())) {
                    checkAndInsertRoleParam(viewCreate.getVariable(), viewCreate.getRoles(), user, view);
                }

                SourceBaseInfo sourceBaseInfo = new SourceBaseInfo();
                BeanUtils.copyProperties(source, sourceBaseInfo);

                ViewWithSourceBaseInfo viewWithSource = new ViewWithSourceBaseInfo();
                BeanUtils.copyProperties(view, viewWithSource);
                viewWithSource.setSource(sourceBaseInfo);
                return viewWithSource;
            } else {
                throw new ServerException("create view fail");
            }
        } else {
            throw new ServerException("get source connection fail");
        }
    }

    /**
     * DSS创建view节点，默认传值为0
     *
     * @param viewCreate
     * @return
     */
    private Source createViewByDss(ViewCreate viewCreate, User user, String ticketId) {
        Source source;
        List<Source> sourceList = sourceMapper.getByProject(viewCreate.getProjectId());
        if (sourceList != null && sourceList.size() > 0) {
            source = sourceList.get(0);
            viewCreate.setSourceId(source.getId());
        } else {
            // 看看工程中有没有可用source
            sourceList = sourceService.getSources(viewCreate.getProjectId(), user, ticketId);
            if (sourceList != null && sourceList.size() > 0) {
                source = sourceList.get(0);
                viewCreate.setSourceId(source.getId());
                return source;
            } else {
                log.info("source was not found int project，projectId:{}", viewCreate.getProjectId());
                throw new NotFoundException("source is not found,DSS create view failed");
            }
        }
        return source;
    }


    /**
     * update by johnnwang
     * 保存view方法
     *
     * @param viewCreate
     * @param source
     * @return
     */
    private ViewWithSourceBaseInfo createViewMethod(ViewCreate viewCreate, Source source) {
        View view = new View();
        BeanUtils.copyProperties(viewCreate, view);

        int insert = viewMapper.insert(view);
        if (insert > 0) {
            SourceBaseInfo sourceBaseInfo = new SourceBaseInfo();
            BeanUtils.copyProperties(source, sourceBaseInfo);

            ViewWithSourceBaseInfo viewWithSource = new ViewWithSourceBaseInfo();
            BeanUtils.copyProperties(view, viewWithSource);
            viewWithSource.setSource(sourceBaseInfo);
            return viewWithSource;
        } else {
            throw new ServerException("create view fail");
        }
    }
    /**
     * 更新View
     *
     * @param viewUpdate
     * @param user
     * @return
     */
    @Override
    @Transactional
    public boolean updateView(ViewUpdate viewUpdate, User user) throws NotFoundException, UnAuthorizedExecption, ServerException {

        ViewWithSource viewWithSource = viewMapper.getViewWithSource(viewUpdate.getId());
        if (null == viewWithSource) {
            throw new NotFoundException("view is not found");
        }

        if(!projectAuth.isPorjectOwner(viewWithSource.getProjectId(), user.getId())) {
            throw new UnAuthorizedExecption("current user has no permission.");
        }

        ProjectDetail projectDetail = null;
        try {
            projectDetail = projectService.getProjectDetail(viewWithSource.getProjectId(), user, false);
        } catch (Exception e) {
            log.error("get project detail fail, because: ", e);
            throw new RuntimeException("get project detail fail");
        }
        ProjectPermission projectPermission = null;
        try {
            projectPermission = projectService.getProjectPermission(projectDetail, user);
        } catch (Exception e) {
            log.error("get project permission fail, because: ", e);
            throw new RuntimeException("get project permission fail");
        }
        if (projectPermission.getViewPermission() < UserPermissionEnum.WRITE.getPermission()) {
            throw new UnAuthorizedExecption("you have not permission to update this view");
        }

        if (isExist(viewUpdate.getName(), viewUpdate.getId(), viewWithSource.getProjectId())) {
            log.info("the view {} name is already taken", viewUpdate.getName());
            throw new ServerException("the view name is already taken");
        }

        Source source = viewWithSource.getSource();
        if (null == source) {
            log.info("source not found");
            throw new NotFoundException("source is not found");
        }
        //判断下model数据长度，如果数据太长，不允许保存
        String model = viewUpdate.getModel();
        if (!StringUtils.isEmpty(model) && model.length() > Constants.TEXT_MAX_LENGTH) {
            throw new ServerException("your saved view data is too long");
        }

        //如果为hive数据源则直接修改
        if (VisualisUtils.isLinkisDataSource(source)) {
            View view = new View();
            BeanUtils.copyProperties(viewUpdate, view);
            view.setProjectId(projectDetail.getId());
            try {
                viewMapper.update(view);
            } catch (Exception e) {
                log.error("update view fail, because: ", e);
                throw new RuntimeException("update view fail");
            }
            return true;
        }
        //测试连接
        boolean testConnection = sqlUtils.init(source).testConnection();

        if (testConnection) {

            String originStr = viewWithSource.toString();
            BeanUtils.copyProperties(viewUpdate, viewWithSource);
            viewWithSource.updatedBy(user.getId());

            int update = viewMapper.update(viewWithSource);
            if (update > 0) {
                optLogger.info("view ({}) is updated by user(:{}), origin: ({})", viewWithSource.toString(), user.getId(), originStr);
                if (CollectionUtils.isEmpty(viewUpdate.getRoles())) {
                    relRoleViewMapper.deleteByViewId(viewUpdate.getId());
                } else if (!StringUtils.isEmpty(viewUpdate.getVariable())) {
                    checkAndInsertRoleParam(viewUpdate.getVariable(), viewUpdate.getRoles(), user, viewWithSource);
                }

                return true;
            } else {
                throw new ServerException("update view fail");
            }
        } else {
            throw new ServerException("get source connection fail");
        }
    }


    /**
     * 删除View
     *
     * @param id
     * @param user
     * @return
     */
    @Override
    @Transactional
    public boolean deleteView(Long id, User user) throws NotFoundException, UnAuthorizedExecption, ServerException {

        View view = viewMapper.getById(id);

        if (null == view) {
            log.warn("view (:{}) not found", id);
            throw new NotFoundException("view is not found");
        }

        if(!projectAuth.isPorjectOwner(view.getProjectId(), user.getId())) {
            throw new UnAuthorizedExecption("current user has no permission.");
        }

        ProjectDetail projectDetail = null;
        try {
            projectDetail = projectService.getProjectDetail(view.getProjectId(), user, false);
        } catch (NotFoundException e) {
            throw e;
        } catch (UnAuthorizedExecption e) {
            throw new UnAuthorizedExecption("you have not permission to delete this view");
        }

        ProjectPermission projectPermission = null;
        try {
            projectPermission = projectService.getProjectPermission(projectDetail, user);
        } catch (Exception e) {
            log.error("get project permission fail, because: ", e);
            throw new RuntimeException("get project permission fail");
        }
        if (projectPermission.getViewPermission() < UserPermissionEnum.DELETE.getPermission()) {
            throw new UnAuthorizedExecption("you have not permission to delete this view");
        }

        List<Widget> widgets = null;
        try {
            widgets = widgetMapper.getWidgetsByWiew(id);
        } catch (Exception e) {
            log.error("get widgets fail, because: ", e);
            throw new RuntimeException("get widgets fail");
        }
        if (!CollectionUtils.isEmpty(widgets)) {
            throw new ServerException("The current view has been referenced, please delete the reference and then operate");
        }

        int i = viewMapper.deleteById(id);
        if (i > 0) {
            optLogger.info("view ( {} ) delete by user( :{} )", view.toString(), user.getId());
            relRoleViewMapper.deleteByViewId(id);
        } else {
            log.error("delete view fail");
            throw new RuntimeException("delete view fail");
        }

        return true;
    }

    /**
     * 获得默认的SourceWithProject
     *
     * @param
     * @param user
     * @return
     */
    public  SourceWithProject getDefaultSourceWithProject(Long sourceId, User user) {
        SourceWithProject sourceWithProject = new SourceWithProject();
        if (VisualisUtils.getHiveDataSourceId() == sourceId || VisualisUtils.getPrestoDataSourceId() == sourceId) {
            Source source = sourceMapper.getById(sourceId);
            Project project = new Project();
            project.setName(VisualisUtils.DEFAULT_PROJECT_NAME().getValue());
            project.setId((Long) VisualisUtils.DEFAULT_PROJECT_ID().getValue());
            source.setProjectId(project.getId());
            sourceWithProject.setSource(source);
            sourceWithProject.setProject(project);
        }
        return sourceWithProject;
    }


    /**
     * 执行sql
     *
     * @param executeSql
     * @param user
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public PaginateWithQueryColumns executeSql(ViewExecuteSql executeSql, User user) throws NotFoundException, UnAuthorizedExecption, ServerException {

        Source source = sourceMapper.getById(executeSql.getSourceId());
        if (source == null && VisualisUtils.isLinkisDataSource(source)) {
        source = getDefaultSourceWithProject(source.getId(), user);
    }
        if (null == source) {
        throw new NotFoundException("source is not found");
    }

    ProjectDetail projectDetail = projectService.getProjectDetail(source.getProjectId(), user, false);

    ProjectPermission projectPermission = projectService.getProjectPermission(projectDetail, user);

        if (projectPermission.getSourcePermission() == UserPermissionEnum.HIDDEN.getPermission()
                || projectPermission.getViewPermission() < UserPermissionEnum.WRITE.getPermission()) {
        throw new UnAuthorizedExecption("you have not permission to execute sql");
    }

    //结构化Sql
    PaginateWithQueryColumns paginateWithQueryColumns = null;
        try {
        SqlEntity sqlEntity = sqlParseUtils.parseSql(executeSql.getSql(), executeSql.getVariables(), sqlTempDelimiter);
        if (null != sqlUtils && null != sqlEntity) {
            if (!StringUtils.isEmpty(sqlEntity.getSql())) {

                if (isMaintainer(user, projectDetail)) {
                    sqlEntity.setAuthParams(null);
                }

                if (!CollectionUtils.isEmpty(sqlEntity.getQuaryParams())) {
                    sqlEntity.getQuaryParams().forEach((k, v) -> {
                        if (v instanceof List && ((List) v).size() > 0) {
                            v = ((List) v).stream().collect(Collectors.joining(COMMA)).toString();
                        }
                        sqlEntity.getQuaryParams().put(k, v);
                    });
                }

                String srcSql = sqlParseUtils.replaceParams(sqlEntity.getSql(), sqlEntity.getQuaryParams(), sqlEntity.getAuthParams(), sqlTempDelimiter, user);

                SqlUtils sqlUtils = this.sqlUtils.init(source);

                List<String> executeSqlList = sqlParseUtils.getSqls(srcSql, false);

                List<String> querySqlList = sqlParseUtils.getSqls(srcSql, true);

                if (VisualisUtils.isLinkisDataSource(source)) {
                    List<String> limitedQuerySqlList = Lists.newArrayList();
                    for (String querySql : querySqlList) {
                        String limitedQuerySql;
                        if (!org.apache.commons.lang.StringUtils.containsIgnoreCase(querySql, "limit")
                                && executeSql.getLimit() > 0) {
                            if (org.apache.commons.lang.StringUtils.containsIgnoreCase(querySql, ";")) {
                                limitedQuerySql = querySql.replaceAll(";", " limit " + executeSql.getLimit() + ";");
                            } else {
                                limitedQuerySql = querySql + " limit " + executeSql.getLimit() + ";";
                            }
                            limitedQuerySqlList.add(limitedQuerySql);
                        } else {
                            limitedQuerySqlList.add(querySql);
                        }
                    }
                    srcSql = org.apache.commons.lang.StringUtils.join(executeSqlList, ";");
                    if (org.apache.commons.lang.StringUtils.isNotBlank(srcSql)) {
                        srcSql = srcSql + ";";
                    }
                    srcSql = srcSql + org.apache.commons.lang.StringUtils.join(limitedQuerySqlList, ";");
                    paginateWithQueryColumns = sqlUtils.syncQuery4Paginate(getRunningScript(user, source, null, projectDetail, false, srcSql, false, 300L), null, null, null, executeSql.getLimit(), null);

                } else {
                    if (!CollectionUtils.isEmpty(executeSqlList)) {
                        executeSqlList.forEach(sql -> sqlUtils.execute(sql));
                    }
                    if (!CollectionUtils.isEmpty(querySqlList)) {
                        for (String sql : querySqlList) {
                            paginateWithQueryColumns = sqlUtils.syncQuery4Paginate(sql, null, null, null, executeSql.getLimit(), null);
                        }
                    }
                }

            } else {
                log.warn("sql is empty, we will ignore it");
                throw new ServerException("您提交的sql是空");
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        throw new ServerException(e.getMessage());
    }
        return paginateWithQueryColumns;
}

@SuppressWarnings("unchecked")
public PaginateWithExecStatus AsyncSubmitSql(ViewExecuteSql executeSql, User user) throws NotFoundException, UnAuthorizedExecption, ServerException{

    Source source = sourceMapper.getById(executeSql.getSourceId());
    if (source == null && VisualisUtils.isLinkisDataSource(source)) {
        source = getDefaultSourceWithProject(source.getId(), user);
    }
    if (null == source) {
        throw new NotFoundException("source is not found");
    }

    ProjectDetail projectDetail = projectService.getProjectDetail(source.getProjectId(), user, false);

    ProjectPermission projectPermission = projectService.getProjectPermission(projectDetail, user);

    if (projectPermission.getSourcePermission() == UserPermissionEnum.HIDDEN.getPermission()
            || projectPermission.getViewPermission() < UserPermissionEnum.WRITE.getPermission()) {
        throw new UnAuthorizedExecption("you have not permission to execute sql");
    }

    //结构化Sql
    PaginateWithExecStatus paginateWithExecStatus = null;
    try {
        SqlEntity sqlEntity = sqlParseUtils.parseSql(executeSql.getSql(), executeSql.getVariables(), sqlTempDelimiter);
        if (null != sqlUtils && null != sqlEntity) {
            if (!StringUtils.isEmpty(sqlEntity.getSql())) {

                if (isMaintainer(user, projectDetail)) {
                    sqlEntity.setAuthParams(null);
                }

                if (!CollectionUtils.isEmpty(sqlEntity.getQuaryParams())) {
                    sqlEntity.getQuaryParams().forEach((k, v) -> {
                        if (v instanceof List && ((List) v).size() > 0) {
                            v = ((List) v).stream().collect(Collectors.joining(COMMA)).toString();
                        }
                        sqlEntity.getQuaryParams().put(k, v);
                    });
                }

                String srcSql = sqlParseUtils.replaceParams(sqlEntity.getSql(), sqlEntity.getQuaryParams(), sqlEntity.getAuthParams(), sqlTempDelimiter, user);

                SqlUtils sqlUtils = this.sqlUtils.init(source);

                List<String> executeSqlList = sqlParseUtils.getSqls(srcSql, false);

                List<String> querySqlList = sqlParseUtils.getSqls(srcSql, true);

                if (VisualisUtils.isLinkisDataSource(source)) {
                    List<String> limitedQuerySqlList = Lists.newArrayList();
                    for (String querySql : querySqlList) {
                        String limitedQuerySql;
                        if (!org.apache.commons.lang.StringUtils.containsIgnoreCase(querySql, "limit")
                                && executeSql.getLimit() > 0) {
                            if (org.apache.commons.lang.StringUtils.containsIgnoreCase(querySql, ";")) {
                                limitedQuerySql = querySql.replaceAll(";", " limit " + executeSql.getLimit() + ";");
                            } else {
                                limitedQuerySql = querySql + " limit " + executeSql.getLimit() + ";";
                            }
                            limitedQuerySqlList.add(limitedQuerySql);
                        } else {
                            limitedQuerySqlList.add(querySql);
                        }
                    }
                    srcSql = org.apache.commons.lang.StringUtils.join(executeSqlList, ";");
                    if (org.apache.commons.lang.StringUtils.isNotBlank(srcSql)) {
                        srcSql = srcSql + ";";
                    }
                    srcSql = srcSql + org.apache.commons.lang.StringUtils.join(limitedQuerySqlList, ";");
                    paginateWithExecStatus = sqlUtils.asyncQuery4Exec(getRunningScript(user, source, null, projectDetail, false, srcSql, false, 300L), null, null, null, executeSql.getLimit(), null);

                } else {
                    // TODO: 2021/10/30 jdbc执行还不支持异步
/*                    if (!CollectionUtils.isEmpty(executeSqlList)) {
                        executeSqlList.forEach(sql -> sqlUtils.execute(sql));
                    }
                    if (!CollectionUtils.isEmpty(querySqlList)) {
                        for (String sql : querySqlList) {
                            paginateWithExecStatus = sqlUtils.asyncQuery4Exec(sql, null, null, null, executeSql.getLimit(), null);
                        }
                    }*/
                }

            } else {
                log.warn("sql is empty, we will ignore it");
                throw new ServerException("您提交的sql是空");
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        throw new ServerException(e.getMessage());
    }
    return paginateWithExecStatus;
}

    private boolean isMaintainer(User user, ProjectDetail projectDetail) {
        return projectService.isMaintainer(projectDetail, user);
    }

    /**
     * 返回view源数据集
     *
     * @param id
     * @param executeParam
     * @param user
     * @param async
     * @return
     */
    @Override
    public Paginate<Map<String, Object>> getData(Long id, ViewExecuteParam executeParam, User user, boolean async) throws NotFoundException, UnAuthorizedExecption, ServerException, SQLException {
        if (null == executeParam || (CollectionUtils.isEmpty(executeParam.getGroups()) && CollectionUtils.isEmpty(executeParam.getAggregators()))) {
            return null;
        }

        ViewWithSource viewWithSource = viewMapper.getViewWithSource(id);
        if (null == viewWithSource) {
            log.info("view (:{}) not found", id);
            throw new NotFoundException("view is not found");
        }
        if (EnvLimitUtils.isProdEnv()) {
            executeParam.setEngineType(VisualisUtils.SPARK().getValue());
        }
        if (org.apache.commons.lang.StringUtils.isNotBlank(executeParam.getEngineType())) {
            String dataSourceName = VisualisUtils.getDataSourceName(executeParam.getEngineType()) + "DataSource";
            if (!dataSourceName.equalsIgnoreCase(viewWithSource.getSource().getName())) {
                Long realDataSourceId = sourceMapper.getByNameWithProjectId(dataSourceName, viewWithSource.getProjectId());
                Source realDataSource = sourceMapper.getById(realDataSourceId);
                if (realDataSource == null) {
                    List<Source> sources = sourceService.getSources(viewWithSource.getProjectId(), user, "");
                    for (Source source : sources) {
                        if (source.getName().contains(dataSourceName)) {
                            realDataSource = source;
                        }
                    }
                }
                viewWithSource.setSource(realDataSource);
            }
        }

        ProjectDetail projectDetail = projectService.getProjectDetail(viewWithSource.getProjectId(), user, false);

        boolean allowGetData = projectService.allowGetData(projectDetail, user);

        if (!allowGetData) {
            throw new UnAuthorizedExecption("you have not permission to get data");
        }

        boolean maintainer = projectService.isMaintainer(projectDetail, user);
        return getResultDataList(maintainer, viewWithSource, executeParam, user, async);
    }

    public Paginate<Map<String, Object>> getAsyncProgress(String execId, User user) throws Exception {
        if (JdbcAsyncUtils.isJdbcExecId(execId)) {
            return JdbcAsyncUtils.getJdbcProgress(execId);
        }
        BaseSource source = null;
        if (NumberUtils.isDigits(execId)) {
            source = sourceMapper.getById(VisualisUtils.getPrestoDataSourceId());
        }
        SqlUtils sqlUtils = this.sqlUtils.init(source);
        return sqlUtils.getProgress4Exec(execId, user.username);
    }

    public Paginate<Map<String, Object>> killAsyncJob(String execId, User user) throws Exception {
        if (JdbcAsyncUtils.isJdbcExecId(execId)) {
            return JdbcAsyncUtils.getJdbcProgress(execId);
        }
        BaseSource source = null;
        if (NumberUtils.isDigits(execId)) {
            source = sourceMapper.getById(VisualisUtils.getPrestoDataSourceId());
        }
        SqlUtils sqlUtils = this.sqlUtils.init(source);
        return sqlUtils.kill4Exec(execId, user.username);
    }


    public Paginate<Map<String, Object>> getAsyncResult(String execId, User user) throws Exception {
        if (JdbcAsyncUtils.isJdbcExecId(execId)) {
            return JdbcAsyncUtils.getResult(execId);
        }
        BaseSource source = null;
        if (NumberUtils.isDigits(execId)) {
            source = sourceMapper.getById(VisualisUtils.getPrestoDataSourceId());
        }
        SqlUtils sqlUtils = this.sqlUtils.init(source);
        return sqlUtils.getResultSet4Exec(execId, user.username);
    }


    public void buildQuerySql(List<String> querySqlList, Source source, ViewExecuteParam executeParam) {
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

        } catch (Exception e) {
            log.error("convertFilters error . filterStrs = {}, source = {}, filters = {} , whereClauses = {} ",
                    JSON.toJSON(filterStrs), JSON.toJSON(source), JSON.toJSON(filters), JSON.toJSON(whereClauses));
            throw e;
        }
        return whereClauses;
    }


    /**
     * 获取结果集
     *
     * @param isMaintainer
     * @param viewWithSource
     * @param executeParam
     * @param user
     * @param async
     * @return
     * @throws ServerException
     */
    @Override
    public PaginateWithQueryColumns getResultDataList(boolean isMaintainer, ViewWithSource viewWithSource, ViewExecuteParam executeParam, User user, boolean async) throws ServerException, SQLException {
        PaginateWithQueryColumns paginate = new PaginateWithQueryColumns();

        if (null == executeParam || (CollectionUtils.isEmpty(executeParam.getGroups()) && CollectionUtils.isEmpty(executeParam.getAggregators()))) {
            return null;
        }

        if (null == viewWithSource.getSource()) {
            throw new NotFoundException("source is not found");
        }

        String cacheKey = null;
        try {

            ChartUtils.processViewExecuteParam(executeParam);

            if (!StringUtils.isEmpty(viewWithSource.getSql())) {
                //解析变量
                List<SqlVariable> variables = viewWithSource.getVariables();
                //解析sql
                SqlEntity sqlEntity = sqlParseUtils.parseSql(viewWithSource.getSql(), variables, sqlTempDelimiter);
                //列权限（只记录被限制访问的字段）
                Set<String> excludeColumns = new HashSet<>();
                packageParams(isMaintainer, viewWithSource.getId(), sqlEntity, variables, executeParam.getParams(), excludeColumns, user);
                String srcSql = sqlParseUtils.replaceParams(sqlEntity.getSql(), sqlEntity.getQuaryParams(), sqlEntity.getAuthParams(), sqlTempDelimiter, user);

                Source source = viewWithSource.getSource();

                SqlUtils sqlUtils = this.sqlUtils.init(source);
                List<String> executeSqlList = sqlParseUtils.getSqls(srcSql, false);
                List<String> querySqlList = sqlParseUtils.getSqls(srcSql, true);

                if (!CollectionUtils.isEmpty(querySqlList)) {
                    executeParam.addExcludeColumn(excludeColumns, source.getJdbcUrl(), source.getDbVersion());

//                    if (null != executeParam
//                            && null != executeParam.getCache()
//                            && executeParam.getCache()
//                            && executeParam.getExpired() > 0L) {
//
//                        StringBuilder slatBuilder = new StringBuilder();
//                        slatBuilder.append(executeParam.getPageNo());
//                        slatBuilder.append(MINUS);
//                        slatBuilder.append(executeParam.getLimit());
//                        slatBuilder.append(MINUS);
//                        slatBuilder.append(executeParam.getPageSize());
//                        excludeColumns.forEach(slatBuilder::append);
//
//                        cacheKey = MD5Util.getMD5(slatBuilder.toString() + querySqlList.get(querySqlList.size() - 1), true, 32);
//
//                        if (!executeParam.getFlush()) {
//                            try {
//                                Object object = redisUtils.get(cacheKey);
//                                if (null != object && executeParam.getCache()) {
//                                    paginate = (PaginateWithQueryColumns) object;
//                                    return paginate;
//                                }
//                            } catch (Exception e) {
//                                log.warn("get data by cache: {}", e.getMessage());
//                            }
//                        }
//                    }

                    if (VisualisUtils.isLinkisDataSource(source)) {
                        Project project = projectService.getProjectDetail(source.getProjectId(), user, false);
                        String viewSql = querySqlList.get(0);
                        CacheTaskResult cacheTaskResult = null;
                        if (executeParam.getCache()) {
                            cacheTaskResult = findOrSubmitCache(sqlUtils, viewSql, user, source, viewWithSource, project, executeParam.getExpired());
                        }
                        if (cacheTaskResult != null) {
                            buildScala(querySqlList, executeParam, source, cacheTaskResult);
                            for (String sql : querySqlList) {
                                if (executeParam.getFlush()) {
                                    VisualisUtils.deleteCache(sql, user.username);
                                    VisualisUtils.deleteCache(viewSql, user.username);
                                    break;
                                }
                                paginate = async ?
                                        sqlUtils.asyncQuery4Exec(
                                                getRunningScript(user, source, viewWithSource, project, true, sql, executeParam.getCache(), executeParam.getExpired()),
                                                executeParam.getPageNo(),
                                                executeParam.getPageSize(),
                                                executeParam.getTotalCount(),
                                                executeParam.getLimit(),
                                                excludeColumns)
                                        :
                                        sqlUtils.syncQuery4Paginate(
                                                getRunningScript(user, source, viewWithSource, project, true, sql, executeParam.getCache(), executeParam.getExpired()),
                                                executeParam.getPageNo(),
                                                executeParam.getPageSize(),
                                                executeParam.getTotalCount(),
                                                executeParam.getLimit(),
                                                excludeColumns);
                            }
                        } else {
                            buildQuerySql(querySqlList, source, executeParam);
                            String script = String.join(Consts.SEMICOLON, executeSqlList);
                            if (org.apache.commons.lang.StringUtils.isNotBlank(script)) {
                                script = script + Consts.SEMICOLON;
                            }
                            script = script + String.join(Consts.SEMICOLON, querySqlList);
                            if (executeParam.getFlush()) {
                                VisualisUtils.deleteCache(script, user.username);
                                VisualisUtils.deleteCache(viewSql, user.username);
                                return paginate;
                            }
                            paginate = async ?
                                    sqlUtils.asyncQuery4Exec(
                                            getRunningScript(user, source, viewWithSource, project, false, script, executeParam.getCache(), executeParam.getExpired()),
                                            executeParam.getPageNo(),
                                            executeParam.getPageSize(),
                                            executeParam.getTotalCount(),
                                            executeParam.getLimit(),
                                            excludeColumns)
                                    :
                                    sqlUtils.query4Paginate(
                                            getRunningScript(user, source, viewWithSource, project, false, script, executeParam.getCache(), executeParam.getExpired()),
                                            executeParam.getPageNo(),
                                            executeParam.getPageSize(),
                                            executeParam.getTotalCount(),
                                            executeParam.getLimit(),
                                            excludeColumns);
                        }
                    } else {
                        buildQuerySql(querySqlList, source, executeParam);
                        if (!CollectionUtils.isEmpty(executeSqlList)) {
                            executeSqlList.forEach(sql -> sqlUtils.execute(sql));
                        }

                        for (String sql : querySqlList) {
                            paginate = sqlUtils.syncQuery4Paginate(
                                    sql,
                                    executeParam.getPageNo(),
                                    executeParam.getPageSize(),
                                    executeParam.getTotalCount(),
                                    executeParam.getLimit(),
                                    excludeColumns);
                        }
                        // fake async for jdbc
                        String execId = JdbcAsyncUtils.generateExecId();
                        JdbcAsyncUtils.putResult(execId, paginate);
                        paginate = JdbcAsyncUtils.getJdbcProgress(execId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("failed to get resultSet ", e);
            throw new ServerException(e.getMessage());
        }

//        if (null != executeParam
//                && null != executeParam.getCache()
//                && executeParam.getCache()
//                && executeParam.getExpired() > 0L
//                && null != paginate && !CollectionUtils.isEmpty(paginate.getResultList())) {
//            redisUtils.set(cacheKey, paginate, executeParam.getExpired(), TimeUnit.SECONDS);
//        }

        return paginate;
    }

    private CacheTaskResult findOrSubmitCache(SqlUtils sqlUtils, String querySql, User user, Source source, View view, Project project, Long expired) throws Exception {
        CacheTaskResult cacheTaskResult = JobHistoryHelper.getCache(querySql, user.username, Lists.newArrayList(VisualisUtils.SPARK().getValue() + "-*"), expired);
        if (cacheTaskResult == null) {
            sqlUtils.syncQuery4Paginate(getRunningScript(user, source, view, project, false, "--set ide.engine.no.limit.allow=true\n" + querySql, true, expired), 0, 0, 0, -1, null);
        }
        cacheTaskResult = JobHistoryHelper.getCache(querySql, user.username, Lists.newArrayList(VisualisUtils.SPARK().getValue() + "-*"), expired);
        return cacheTaskResult;
    }

    private String getRunningScript(User user, Source source, View view, Project project, Boolean isScala, String script, Boolean cache, Long expired) {
        if (!VisualisUtils.isLinkisDataSource(source)) {
            return script;
        } else {
            UJESJob ujesJob = null;
            String querySource = project.getName();
            if (view != null) {
                querySource = querySource + "/" + view.getName();
            }
            HashMap<String, String> sourceMap = Maps.newHashMap();
            sourceMap.put("fileName", querySource);
            if (isScala) {
                ujesJob = UJESJob.apply(script, user.getName(), UJESJob.SCALA_TYPE(), sourceMap, cache, expired, cache, expired);
            } else {
                ujesJob = UJESJob.apply(script, user.getName(), UJESJob.SQL_TYPE(), sourceMap, cache, expired, cache, expired);
                if (VisualisUtils.isPrestoDataSource(source)) {
                    ujesJob.engine_$eq(UJESJob.PRESTO_ENGINE());
                    ujesJob.jobType_$eq(UJESJob.PSQL_TYPE());
                }
            }
            return LinkisUtils.gsonNoContert().toJson(ujesJob);
        }
    }

    private void buildScala(List<String> querySqlList, ViewExecuteParam executeParam, Source source, CacheTaskResult cacheTaskResult) {
        String tempViewName = "view_res_" + FilenameUtils.getBaseName(cacheTaskResult.getResultLocation());
        String resultLocation = cacheTaskResult.getResultLocation() + VisualisUtils.RESULT_FILE_NAME().getValue();
        querySqlList.set(0, tempViewName);
        buildQuerySql(querySqlList, source, executeParam);
        querySqlList.set(0, VisualisUtils.buildScala(querySqlList.get(0), resultLocation, tempViewName));
    }

    @Override
    public List<Map<String, Object>> getDistinctValue(Long id, DistinctParam param, User user) throws NotFoundException, ServerException, UnAuthorizedExecption {
        ViewWithSource viewWithSource = viewMapper.getViewWithSource(id);
        if (null == viewWithSource) {
            log.info("view (:{}) not found", id);
            throw new NotFoundException("view is not found");
        }

        ProjectDetail projectDetail = projectService.getProjectDetail(viewWithSource.getProjectId(), user, false);

        boolean allowGetData = projectService.allowGetData(projectDetail, user);

        if (!allowGetData) {
            throw new UnAuthorizedExecption();
        }

        return getDistinctValueData(projectService.isMaintainer(projectDetail, user), viewWithSource, param, user);
    }


    @Override
    public List<Map<String, Object>> getDistinctValueData(boolean isMaintainer, ViewWithSource viewWithSource, DistinctParam param, User user) throws ServerException {

        try {
            if (!StringUtils.isEmpty(viewWithSource.getSql())) {
                List<SqlVariable> variables = viewWithSource.getVariables();
                SqlEntity sqlEntity = sqlParseUtils.parseSql(viewWithSource.getSql(), variables, sqlTempDelimiter);
                packageParams(isMaintainer, viewWithSource.getId(), sqlEntity, variables, param.getParams(), null, user);

                String srcSql = sqlParseUtils.replaceParams(sqlEntity.getSql(), sqlEntity.getQuaryParams(), sqlEntity.getAuthParams(), sqlTempDelimiter, user);

                Source source = viewWithSource.getSource();


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

//                        if (null != param.getCache() && param.getCache() && param.getExpired().longValue() > 0L) {
//                            cacheKey = MD5Util.getMD5("DISTINCI" + sql, true, 32);
//
//                            try {
//                                Object object = redisUtils.get(cacheKey);
//                                if (null != object) {
//                                    return (List) object;
//                                }
//                            } catch (Exception e) {
//                                log.warn("get distinct value by cache: {}", e.getMessage());
//                            }
//                        }
                    }

                    List<Map<String, Object>> list = null;
                    if (VisualisUtils.isLinkisDataSource(source)) {
                        Project project = projectService.getProjectDetail(source.getProjectId(), user, false);
                        list = sqlUtils.query4List(getRunningScript(user, source, viewWithSource, project, false, String.join(Consts.SEMICOLON, executeSqlList) + Consts.SEMICOLON + String.join(Consts.SEMICOLON, querySqlList), param.getCache(), param.getExpired()), -1);
                    } else {
                        if (!CollectionUtils.isEmpty(executeSqlList)) {
                            executeSqlList.forEach(sql -> sqlUtils.execute(sql));
                        }
                        for (String sql : querySqlList) {
                            list = sqlUtils.query4List(sql, -1);
                        }
                    }

//                    if (null != param.getCache() && param.getCache() && param.getExpired().longValue() > 0L) {
//                        redisUtils.set(cacheKey, list, param.getExpired(), TimeUnit.SECONDS);
//                    }

                    if (null != list) {
                        return list;
                    }
                }
            }
        } catch (Exception e) {
            log.error("failed to get distinct value data, ", e);
            throw new ServerException(e.getMessage());
        }

        return null;
    }


    private Set<String> getExcludeColumns(List<RelRoleView> roleViewList) {
        if (!CollectionUtils.isEmpty(roleViewList)) {
            Set<String> columns = new HashSet<>();
            roleViewList.forEach(r -> {
                if (!StringUtils.isEmpty(r.getColumnAuth())) {
                    columns.addAll(JSONObject.parseArray(r.getColumnAuth(), String.class));
                }
            });
            return columns;
        }
        return null;
    }


    private List<SqlVariable> getQueryVariables(List<SqlVariable> variables) {
        if (!CollectionUtils.isEmpty(variables)) {
            return variables.stream().filter(v -> QUERYVAR == SqlVariableTypeEnum.typeOf(v.getType())).collect(Collectors.toList());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<SqlVariable> getAuthVariables(List<RelRoleView> roleViewList, List<SqlVariable> variables) {
        if (!CollectionUtils.isEmpty(variables)) {

            List<SqlVariable> list = new ArrayList<>();

            variables.forEach(v -> {
                if (null != v.getChannel()) {
                    list.add(v);
                }
            });

            if (!CollectionUtils.isEmpty(roleViewList)) {
                Map<String, SqlVariable> map = new HashMap<>();

                List<SqlVariable> authVarables = variables.stream().filter(v -> AUTHVARE == SqlVariableTypeEnum.typeOf(v.getType())).collect(Collectors.toList());
                authVarables.forEach(v -> map.put(v.getName(), v));
                List<SqlVariable> dacVars = authVarables.stream().filter(v -> null != v.getChannel() && !v.getChannel().getBizId().equals(0L)).collect(Collectors.toList());

                roleViewList.forEach(r -> {
                    if (!StringUtils.isEmpty(r.getRowAuth())) {
                        List<AuthParamValue> authParamValues = JSONObject.parseArray(r.getRowAuth(), AuthParamValue.class);
                        authParamValues.forEach(v -> {
                            if (map.containsKey(v.getName())) {
                                SqlVariable sqlVariable = map.get(v.getName());
                                if (v.isEnable()) {
                                    if (CollectionUtils.isEmpty(v.getValues())) {
                                        List values = new ArrayList<>();
                                        values.add(N0_AUTH_PERMISSION);
                                        sqlVariable.setDefaultValues(values);
                                    } else {
                                        List<Object> values = sqlVariable.getDefaultValues() == null ? new ArrayList<>() : sqlVariable.getDefaultValues();
                                        values.addAll(v.getValues());
                                        sqlVariable.setDefaultValues(values);
                                    }
                                } else {
                                    sqlVariable.setDefaultValues(new ArrayList<>());
                                }
                                list.add(sqlVariable);
                            }
                        });
                    } else {
                        dacVars.forEach(v -> list.add(v));
                    }
                });
            }
            return list;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void packageParams(boolean isProjectMaintainer, Long viewId, SqlEntity sqlEntity, List<SqlVariable> variables, List<Param> paramList, Set<String> excludeColumns, User user) {

        List<SqlVariable> queryVariables = getQueryVariables(variables);
        List<SqlVariable> authVariables = null;

        if (!isProjectMaintainer) {
            List<RelRoleView> roleViewList = relRoleViewMapper.getByUserAndView(user.getId(), viewId);
            authVariables = getAuthVariables(roleViewList, variables);
            if (null != excludeColumns) {
                Set<String> eclmns = getExcludeColumns(roleViewList);
                if (!CollectionUtils.isEmpty(eclmns)) {
                    excludeColumns.addAll(eclmns);
                }
            }
        }

        //查询参数
        if (!CollectionUtils.isEmpty(queryVariables) && !CollectionUtils.isEmpty(sqlEntity.getQuaryParams())) {
            if (!CollectionUtils.isEmpty(paramList)) {
                Map<String, List<SqlVariable>> map = queryVariables.stream().collect(Collectors.groupingBy(SqlVariable::getName));
                paramList.forEach(p -> {
                    if (map.containsKey(p.getName())) {
                        List<SqlVariable> list = map.get(p.getName());
                        if (!CollectionUtils.isEmpty(list)) {
                            SqlVariable v = list.get(list.size() - 1);
                            if (null == sqlEntity.getQuaryParams()) {
                                sqlEntity.setQuaryParams(new HashMap<>());
                            }
                            sqlEntity.getQuaryParams().put(p.getName().trim(), SqlVariableValueTypeEnum.getValue(v.getValueType(), p.getValue(), v.isUdf()));
                        }
                    }
                });
            }

            sqlEntity.getQuaryParams().forEach((k, v) -> {
                if (v instanceof List && ((List) v).size() > 0) {
                    v = ((List) v).stream().collect(Collectors.joining(COMMA)).toString();
                }
                sqlEntity.getQuaryParams().put(k, v);
            });
        }

        //如果当前用户是project的维护者，直接不走行权限
        if (isProjectMaintainer) {
            sqlEntity.setAuthParams(null);
            return;
        }

        //权限参数
        if (!CollectionUtils.isEmpty(authVariables)) {
            ExecutorService executorService = Executors.newFixedThreadPool(8);
            CountDownLatch countDownLatch = new CountDownLatch(authVariables.size());
            Map<String, Set<String>> map = new Hashtable<>();
            List<Future> futures = new ArrayList<>(authVariables.size());
            try {
                authVariables.forEach(sqlVariable -> {
                    try {
                        futures.add(executorService.submit(() -> {
                            if (null != sqlVariable) {
                                Set<String> vSet = null;
                                if (map.containsKey(sqlVariable.getName().trim())) {
                                    vSet = map.get(sqlVariable.getName().trim());
                                } else {
                                    vSet = new HashSet<>();
                                }

                                List<String> values = sqlParseUtils.getAuthVarValue(sqlVariable, user.getEmail());
                                if (null == values) {
                                    vSet.add(N0_AUTH_PERMISSION);
                                } else if (!values.isEmpty()) {
                                    vSet.addAll(values);
                                }
                                map.put(sqlVariable.getName().trim(), vSet);
                            }
                        }));
                    } finally {
                        countDownLatch.countDown();
                    }
                });
                try {
                    for (Future future : futures) {
                        future.get();
                    }
                    countDownLatch.await();
                } catch (ExecutionException e) {
                    executorService.shutdownNow();
                    throw (ServerException) e.getCause();
                }
            } catch (InterruptedException e) {
                log.error("thread package params get interrupted", e);
            } finally {
                executorService.shutdown();
            }

            if (!CollectionUtils.isEmpty(map)) {
                if (null == sqlEntity.getAuthParams()) {
                    sqlEntity.setAuthParams(new HashMap<>());
                }
                map.forEach((k, v) -> sqlEntity.getAuthParams().put(k, new ArrayList<String>(v)));
            }
        } else {
            sqlEntity.setAuthParams(new HashMap<>());
        }
    }


    @SuppressWarnings("unchecked")
    private void checkAndInsertRoleParam(String sqlVarible, List<RelRoleViewDto> roles, User user, View view) {
        List<SqlVariable> variables = JSONObject.parseArray(sqlVarible, SqlVariable.class);

        if (CollectionUtils.isEmpty(roles)) {
            relRoleViewMapper.deleteByViewId(view.getId());
        } else {
            new Thread(() -> {
                Set<String> vars = null, columns = null;

                if (!CollectionUtils.isEmpty(variables)) {
                    vars = variables.stream().map(SqlVariable::getName).collect(Collectors.toSet());
                }
                if (!StringUtils.isEmpty(view.getModel())) {
                    columns = JSONObject.parseObject(view.getModel(), HashMap.class).keySet();
                }

                Set<String> finalColumns = columns;
                Set<String> finalVars = vars;

                List<RelRoleView> relRoleViews = new ArrayList<>();
                roles.forEach(r -> {
                    if (r.getRoleId().longValue() > 0L) {
                        String rowAuth = null, columnAuth = null;
                        if (!StringUtils.isEmpty(r.getRowAuth())) {
                            JSONArray rowAuthArray = JSONObject.parseArray(r.getRowAuth());
                            if (!CollectionUtils.isEmpty(rowAuthArray)) {
                                JSONArray newArray = new JSONArray();
                                for (int i = 0; i < rowAuthArray.size(); i++) {
                                    JSONObject jsonObject = rowAuthArray.getJSONObject(i);
                                    String name = jsonObject.getString(SQL_VARABLE_KEY);
                                    if (finalVars.contains(name)) {
                                        newArray.add(jsonObject);
                                    }
                                }
                                rowAuth = newArray.toJSONString();
                                newArray.clear();
                            }
                        }

                        if (null != finalColumns && !StringUtils.isEmpty(r.getColumnAuth())) {
                            List<String> clms = JSONObject.parseArray(r.getColumnAuth(), String.class);
                            List<String> collect = clms.stream().filter(c -> finalColumns.contains(c)).collect(Collectors.toList());
                            columnAuth = JSONObject.toJSONString(collect);
                        }

                        RelRoleView relRoleView = new RelRoleView(view.getId(), r.getRoleId(), rowAuth, columnAuth)
                                .createdBy(user.getId());
                        relRoleViews.add(relRoleView);
                    }
                });
                if (!CollectionUtils.isEmpty(relRoleViews)) {
                    relRoleViewMapper.insertBatch(relRoleViews);
                }
            }).start();
        }
    }


}

