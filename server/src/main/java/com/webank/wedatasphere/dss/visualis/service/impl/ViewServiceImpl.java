package com.webank.wedatasphere.dss.visualis.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.webank.wedatasphere.dss.visualis.entrance.spark.SqlCodeParse;
import com.webank.wedatasphere.dss.visualis.exception.VGErrorException;
import com.webank.wedatasphere.dss.visualis.model.DWCResultInfo;
import com.webank.wedatasphere.dss.visualis.model.PaginateWithExecStatus;
import com.webank.wedatasphere.dss.visualis.res.ResultHelper;
import com.webank.wedatasphere.dss.visualis.service.IViewService;
import com.webank.wedatasphere.dss.visualis.utils.HttpUtils;
import com.webank.wedatasphere.dss.visualis.utils.VisualisUtils;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import edp.core.exception.NotFoundException;
import edp.core.model.PaginateWithQueryColumns;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@Service("dssViewService")
@Slf4j
public class ViewServiceImpl implements IViewService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private SourceService sourceService;


    @Autowired
    private ViewMapper viewMapper;

    @Autowired
    private SourceMapper sourceMapper;

    @Autowired
    private ViewService viewService;


    @Override
    public Message getViewData(HttpServletRequest req, Long id) throws Exception {
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
        Message message = Message.ok("get view data success when dss execute view node");
        if (paginateWithQueryColumns != null) {
            message.data("columns", paginateWithQueryColumns.getColumns());
            message.data("resultList", paginateWithQueryColumns.getResultList());
        }
        return message;
    }


    @Override
    public Message createView(HttpServletRequest req, DWCResultInfo dwcResultInfo) {
        Message message;
        try {
            String userName = SecurityFilter.getLoginUsername(req);
            User user = userMapper.selectByUsername(userName);
            Project project = projectMapper.getProejctsByUser(user.getId()).get(0);

            if (project == null) {
                message = Message.error("用户没有默认的项目，请联系管理员");
                return message;
            }
            if (dwcResultInfo == null) {
                message = Message.error("结果为空，无法做可视化分析");
                return message;
            }
            if (StringUtils.isEmpty(dwcResultInfo.getExecutionCode())) {
                message = Message.error("脚本为空，无法做可视化分析");
                return message;
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
                message = Message.ok();
                message.data("id", view.getId());
                message.data("projectId", view.getProjectId());
            } catch (VGErrorException e) {
                log.error("可视化分析失败：", e);
                message = Message.error("可视化分析失败：" + e.getMessage());
            }
            return message;
        } catch (Throwable e) {
            log.error("可视化分析失败：", e);
            message = Message.error("可视化分析失败：" + e.getMessage());
            return message;
        }
    }

    @Override
    public Message getAvailableEngineTypes(HttpServletRequest req, Long id) {
        String userName = SecurityFilter.getLoginUsername(req);
        List<String> engineTypes;
        if (id <= 0) {
            engineTypes = Lists.newArrayList(VisualisUtils.SPARK().getValue());
        } else {
            engineTypes = sourceService.getAvailableEngineTypes(userName);
        }
        Message message = Message.ok().data("engineTypes", engineTypes);
        return message;
    }

    @Override
    public Message submitQuery(HttpServletRequest req, Long id) throws Exception {
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
        Message message = Message.ok("get view data success when dss execute view node");
        if (paginateWithExecStatus != null) {
            message.data("paginateWithExecStatus", paginateWithExecStatus);
        }
        return message;
    }

    @Override
    public Message isHiveDataSource(HttpServletRequest req, Long id) throws Exception {
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
        Message message = Message.ok("get DataSource success when dss execute view node");
        if (VisualisUtils.isLinkisDataSource(source)) {
            message.data("isLinkisDataSource", true);
        } else {
            message.data("isLinkisDataSource", false);
        }
        return message;
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
