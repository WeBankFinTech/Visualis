package com.webank.wedatasphere.dss.visualis.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.webank.wedatasphere.dss.visualis.service.DssViewService;
import com.webank.wedatasphere.dss.visualis.entrance.spark.SqlCodeParse;
import com.webank.wedatasphere.dss.visualis.enums.ModuleEnum;
import com.webank.wedatasphere.dss.visualis.exception.VGErrorException;
import com.webank.wedatasphere.dss.visualis.model.DWCResultInfo;
import com.webank.wedatasphere.dss.visualis.model.PaginateWithExecStatus;
import com.webank.wedatasphere.dss.visualis.model.optmodel.ExportedProject;
import com.webank.wedatasphere.dss.visualis.model.optmodel.IdCatalog;
import com.webank.wedatasphere.dss.visualis.res.ResultHelper;
import com.webank.wedatasphere.dss.visualis.utils.HttpUtils;
import com.webank.wedatasphere.dss.visualis.utils.VisualisUtils;
import edp.core.exception.NotFoundException;
import edp.core.model.PaginateWithQueryColumns;
import edp.davinci.core.common.ResultMap;
import edp.davinci.dao.ProjectMapper;
import edp.davinci.dao.SourceMapper;
import edp.davinci.dao.UserMapper;
import edp.davinci.dao.ViewMapper;
import edp.davinci.dto.viewDto.ViewExecuteSql;
import edp.davinci.model.*;
import edp.davinci.service.SourceService;
import edp.davinci.service.ViewService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.webank.wedatasphere.dss.visualis.service.Utils.updateName;

@Slf4j
@Service("dssViewService")
public class ViewServiceImpl implements DssViewService {

    @Autowired
    ViewMapper viewMapper;

    @Autowired
    SourceMapper sourceMapper;

    @Autowired
    SourceService sourceService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    ViewService viewService;


    @Override
    public List<String> getAvailableEngineTypes(HttpServletRequest req, Long id) {
        String userName = SecurityFilter.getLoginUsername(req);
        List<String> engineTypes;
        if (id <= 0) {
            engineTypes = Lists.newArrayList(VisualisUtils.SPARK().getValue());
        } else {
            engineTypes = sourceService.getAvailableEngineTypes(userName);
        }
        return engineTypes;
    }

    @Override
    public ResultMap createView(HttpServletRequest req, DWCResultInfo dwcResultInfo) throws Exception {

        Map<String, Object> resultDataMap = new HashMap<>();
        ResultMap resultMap = new ResultMap();

        try {
            String userName = SecurityFilter.getLoginUsername(req);
            User user = userMapper.selectByUsername(userName);
            Project project = projectMapper.getProejctsByUser(user.getId()).get(0);

            if (project == null) {
                throw new Exception("用户没有默认的项目，请联系管理员");
            }
            if (dwcResultInfo == null) {
                throw new Exception("结果为空，无法做可视化分析");
            }
            if (StringUtils.isEmpty(dwcResultInfo.getExecutionCode())) {
                throw new Exception("脚本为空，无法做可视化分析");
            }
            String[] sqlList = SqlCodeParse.parse(dwcResultInfo.getExecutionCode());
            int index = dwcResultInfo.getResultNumber();
            String code = "";
            if (index < sqlList.length) {
                code = sqlList[index];
            }

            View view = new View();
            view.setProjectId(project.getId());
            view.setName(VisualisUtils.createTmpViewName(user.getName()));
            List<Source> sources = sourceService.getSources(project.getId(), user, HttpUtils.getUserTicketId(req));
            for (Source source : sources) {
                if (VisualisUtils.isHiveDataSource(source)) {
                    view.setSourceId(source.getId());
                }
            }
            view.setSql(code);
            view.setModel(ResultHelper.toModelItem(dwcResultInfo.getResultPath()));
            view.setConfig("{\"" + VisualisUtils.DWC_RESULT_INFO().getValue() + "\":" + BDPJettyServerHelper.gson().toJson(dwcResultInfo) + "}");
            try {
                view = createView(view);
                resultDataMap.put("id", view.getId());
                resultDataMap.put("projectId", view.getProjectId());
            } catch (VGErrorException e) {
                log.error("可视化分析失败：", e);
                throw new Exception("脚本为空，无法做可视化分析", e.getCause());
            }
            return resultMap.success().payload(resultDataMap);
        } catch (Exception e) {
            log.error("可视化分析失败：", e);
            throw e;
        }
    }

    @Override
    public ResultMap getViewData(HttpServletRequest req, Long id) throws Exception {

        ResultMap resultMap = new ResultMap();
        Map<String, Object> resultDataMap = new HashMap<>();

        if (id == null) {
            throw new Exception("viewId is null when dss execute view node");
        }
        String userName = SecurityFilter.getLoginUsername(req);
        User user = userMapper.selectByUsername(userName);
        if (user == null) {
            throw new Exception("user is empty when dss execute view node");
        }
        View view = viewMapper.getById(id);
        if (view == null) {
            throw new Exception("viewInfo is empty when dss execute view node");
        }
        ViewExecuteSql executeSql = new ViewExecuteSql();
        Long sourceId = view.getSourceId();
        if (sourceId == null) {
            throw new Exception("sourceId is null when dss execute view node");
        }
        executeSql.setSourceId(sourceId);
        String sql = view.getSql();
        executeSql.setSql(sql);
        String variableStr = view.getVariable();
        if (StringUtils.isNotEmpty(variableStr)) {
            List<SqlVariable> variables = JSON.parseArray(variableStr, SqlVariable.class);
            log.info("variables:{}", executeSql);
            executeSql.setVariables(variables);
        }
        // view节点执行操作
        PaginateWithQueryColumns paginateWithQueryColumns = viewService.executeSql(executeSql, user);
        if (paginateWithQueryColumns != null) {
            resultDataMap.put("columns", paginateWithQueryColumns.getColumns());
            resultDataMap.put("resultList", paginateWithQueryColumns.getResultList());
        } else {
            return resultMap.fail().payload("View执行失败.");
        }
        return resultMap.success().payload(resultDataMap);
    }

    @Override
    public ResultMap submitQuery(HttpServletRequest req, Long id) throws Exception {

        ResultMap resultMap = new ResultMap();
        Map<String, Object> resultDataMap = new HashMap<>();

        if (id == null) {
            throw new Exception("viewId is null when dss execute view node");
        }
        String userName = SecurityFilter.getLoginUsername(req);
        User user = userMapper.selectByUsername(userName);
        if (user == null) {
            throw new Exception("user is empty when dss execute view node");
        }
        View view = viewMapper.getById(id);
        if (view == null) {
            throw new Exception("viewInfo is empty when dss execute view node");
        }
        ViewExecuteSql executeSql = new ViewExecuteSql();
        Long sourceId = view.getSourceId();
        if (sourceId == null) {
            throw new Exception("sourceId is null when dss execute view node");
        }
        executeSql.setSourceId(sourceId);
        String sql = view.getSql();
        executeSql.setSql(sql);
        String variableStr = view.getVariable();
        if (StringUtils.isNotEmpty(variableStr)) {
            List<SqlVariable> variables = JSON.parseArray(variableStr, SqlVariable.class);
            log.info("variables:{}", executeSql);
            executeSql.setVariables(variables);
        }
        // 异步执行view语句
        PaginateWithExecStatus paginateWithExecStatus = viewService.AsyncSubmitSql(executeSql, user);
        if (paginateWithExecStatus != null) {
            resultDataMap.put("paginateWithExecStatus", paginateWithExecStatus);
        } else {
            resultMap.fail().payload("view执行失败.");
        }
        return resultMap.success().payload(resultDataMap);
    }

    @Override
    public ResultMap isHiveDataSource(HttpServletRequest req, Long id) throws Exception {

        ResultMap resultMap = new ResultMap();
        Map<String, Object> resultDataMap = new HashMap<>();

        if (id == null) {
            throw new Exception("viewId is null when dss execute view node");
        }
        String userName = SecurityFilter.getLoginUsername(req);
        User user = userMapper.selectByUsername(userName);
        if (user == null) {
            throw new Exception("user is empty when dss execute view node");
        }
        View view = viewMapper.getById(id);
        if (view == null) {
            throw new Exception("viewInfo is empty when dss execute view node");
        }
        Long sourceId = view.getSourceId();
        if (sourceId == null) {
            throw new Exception("sourceId is null when dss execute view node");
        }
        Source source = sourceMapper.getById(sourceId);
        if (null == source) {
            throw new NotFoundException("source is not found");
        }
        if (VisualisUtils.isLinkisDataSource(source)) {
            resultDataMap.put("isLinkisDataSource", true);
        } else {
            resultDataMap.put("isLinkisDataSource", false);
        }
        return resultMap.success().payload(resultDataMap);
    }

    @Override
    public void exportViews(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial, ExportedProject exportedProject) throws Exception {
        if (partial) {
            Set<Long> longs = moduleIdsMap.get(ModuleEnum.VIEW_IDS.getName());
            if (longs.size() > 0) {
                exportedProject.setViews(viewMapper.getByIds(longs));
                Set<Long> sourceIds = exportedProject.getViews().stream().map(View::getSourceId).collect(Collectors.toSet());
                List<Source> sources = sourceMapper.getByProject(projectId).stream().filter(s -> sourceIds.contains(s.getId())).collect(Collectors.toList());
                exportedProject.setSources(sources);
            }
        } else {
            exportedProject.setSources(sourceMapper.getByProject(projectId));
            List<View> exportedViews = Lists.newArrayList();
            for (Source source : exportedProject.getSources()) {
                exportedViews.addAll(viewMapper.getBySourceId(source.getId()));
            }
            exportedProject.setViews(exportedViews);
        }
        log.info("exporting project, export views: {}", exportedProject);
    }

    @Override
    public void importViews(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog) throws Exception {
        List<View> views = exportedProject.getViews();
        if (views == null) {
            return;
        }
        for (View view : views) {
            Long oldId = view.getId();
            view.setProjectId(projectId);
            view.setName(updateName(view.getName(), versionSuffix));
            if (idCatalog.getSource().get(view.getSourceId()) != null) {
                view.setSourceId(idCatalog.getSource().get(view.getSourceId()));
            }
            Long existingId = viewMapper.getByNameWithProjectId(view.getName(), projectId);
            if (existingId != null) {
                idCatalog.getView().put(oldId, existingId);
            } else {
                viewMapper.insert(view);
                idCatalog.getView().put(oldId, view.getId());
            }
        }
    }

    @Override
    public void copyView(Map<String, Set<Long>> moduleIdsMap, ExportedProject exportedProject) throws Exception {
        Set<Long> viewIds = moduleIdsMap.get(ModuleEnum.VIEW_IDS.getName());
        if (!viewIds.isEmpty()) {
            View view = exportedProject.getViews().get(0);
            exportedProject.setViews(Lists.newArrayList(view));
        }
    }


    @Transactional
    private View createView(View view) throws VGErrorException {
        int id = viewMapper.insert(view);
        if (id < 0) {
            throw new VGErrorException(70002, "将view 插入数据库失败");
        }
        return view;
    }

}
