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
import com.webank.wedatasphere.dss.visualis.auth.ProjectAuth;
import edp.core.exception.NotFoundException;
import edp.core.exception.ServerException;
import edp.core.exception.UnAuthorizedExecption;
import edp.core.model.PaginateWithQueryColumns;
import edp.core.model.QueryColumn;
import edp.core.utils.CollectionUtils;
import edp.core.utils.FileUtils;
import edp.core.utils.ServerUtils;
import edp.davinci.common.utils.ComponentFilterUtils;
import edp.davinci.core.enums.FileTypeEnum;
import edp.davinci.core.enums.LogNameEnum;
import edp.davinci.core.enums.UserPermissionEnum;
import edp.davinci.core.utils.CsvUtils;
import edp.davinci.core.utils.ExcelUtils;
import edp.davinci.dao.MemDashboardWidgetMapper;
import edp.davinci.dao.MemDisplaySlideWidgetMapper;
import edp.davinci.dao.ViewMapper;
import edp.davinci.dao.WidgetMapper;
import edp.davinci.dto.projectDto.ProjectDetail;
import edp.davinci.dto.projectDto.ProjectPermission;
import edp.davinci.dto.viewDto.Aggregator;
import edp.davinci.dto.viewDto.ViewExecuteParam;
import edp.davinci.dto.viewDto.ViewWithProjectAndSource;
import edp.davinci.dto.viewDto.ViewWithSource;
import edp.davinci.dto.widgetDto.WidgetCreate;
import edp.davinci.dto.widgetDto.WidgetUpdate;
import edp.davinci.dto.widgetDto.WidgetUpdateFilters;
import edp.davinci.model.User;
import edp.davinci.model.View;
import edp.davinci.model.Widget;
import edp.davinci.service.ProjectService;
import edp.davinci.service.ShareService;
import edp.davinci.service.ViewService;
import edp.davinci.service.WidgetService;
import lombok.extern.slf4j.Slf4j;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static edp.core.consts.Consts.EMPTY;
import static edp.davinci.common.utils.ScriptUtils.getExecuptParamScriptEngine;
import static edp.davinci.common.utils.ScriptUtils.getViewExecuteParam;


@Service("widgetService")
@Slf4j
public class WidgetServiceImpl implements WidgetService {
    private static final Logger optLogger = LoggerFactory.getLogger(LogNameEnum.BUSINESS_OPERATION.getName());

    @Autowired
    private WidgetMapper widgetMapper;

    @Autowired
    private ViewMapper viewMapper;

    @Autowired
    private MemDashboardWidgetMapper memDashboardWidgetMapper;

    @Autowired
    private MemDisplaySlideWidgetMapper memDisplaySlideWidgetMapper;

    @Autowired
    private ShareService shareService;

    @Autowired
    private ViewService viewService;

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private ServerUtils serverUtils;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectAuth projectAuth;

    @Override
    public synchronized boolean isExist(String name, Long id, Long projectId) {
        Long widgetId = widgetMapper.getByNameWithProjectId(name, projectId);
        if (null != id && null != widgetId) {
            return !id.equals(widgetId);
        }
        return null != widgetId && widgetId.longValue() > 0L;
    }

    /**
     * 获取widgets列表
     *
     * @param projectId
     * @param user
     * @return
     */
    @Override
    public List<Widget> getWidgets(Long projectId, User user) throws NotFoundException, UnAuthorizedExecption, ServerException {

        ProjectDetail projectDetail = null;
        try {
            projectDetail = projectService.getProjectDetail(projectId, user, false);
        } catch (Exception e) {
            log.error("get project detail fail, because: ", e);
            throw new RuntimeException("get project detail fail");
        }

        List<Widget> widgets = null;
        try {
            widgets = widgetMapper.getByProject(projectId);
        } catch (Exception e) {
            log.error("get widget list fail, because: ", e);
            throw new RuntimeException("get widget list fail");
        }

        if (null != widgets) {
            ProjectPermission projectPermission = null;
            try {
                projectPermission = projectService.getProjectPermission(projectDetail, user);
            } catch (Exception e) {
                log.error("get project permission fail, because: ", e);
                throw new RuntimeException("get project permission fail");
            }
            if (projectPermission.getVizPermission() == UserPermissionEnum.HIDDEN.getPermission() &&
                    projectPermission.getWidgetPermission() == UserPermissionEnum.HIDDEN.getPermission()) {
                return null;
            }
        }

        ComponentFilterUtils filter = new ComponentFilterUtils();
        widgets = filter.doFilterWidgets(widgets);

        return widgets;
    }


    /**
     * 获取单个widget信息
     *
     * @param id
     * @param user
     * @return
     */
    @Override
    public Widget getWidget(Long id, User user) throws NotFoundException, UnAuthorizedExecption, ServerException {

        Widget widget = widgetMapper.getById(id);

        if (null == widget) {
            log.info("widget {} not found", id);
            throw new NotFoundException("widget is not found");
        }

        ProjectDetail projectDetail = null;
        try {
            projectDetail = projectService.getProjectDetail(widget.getProjectId(), user, false);
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
        if (projectPermission.getWidgetPermission() < UserPermissionEnum.READ.getPermission()) {
            throw new UnAuthorizedExecption();
        }

        return widget;
    }

    /**
     * 创建widget
     *
     * @param widgetCreate
     * @param user
     * @return
     */
    @Override
    @Transactional
    public Widget createWidget(WidgetCreate widgetCreate, User user) throws NotFoundException, UnAuthorizedExecption, ServerException {

        ProjectDetail projectDetail = null;
        try {
            projectDetail = projectService.getProjectDetail(widgetCreate.getProjectId(), user, false);
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

        if (projectPermission.getWidgetPermission() < UserPermissionEnum.WRITE.getPermission()) {
            log.info("user {} have not permisson to create widget", user.getUsername());
            throw new UnAuthorizedExecption("you have not permission to create widget");
        }

        if (isExist(widgetCreate.getName(), null, widgetCreate.getProjectId())) {
            log.info("the widget {} name is already taken", widgetCreate.getName());
            throw new ServerException("the widget name is already taken");
        }

        View view = viewMapper.getById(widgetCreate.getViewId());
        if (null == view) {
            log.info("view (:{}) is not found", widgetCreate.getViewId());
            //throw new NotFoundException("view not found");
        }

        Widget widget = new Widget().createdBy(user.getId());
        BeanUtils.copyProperties(widgetCreate, widget);
        int insert = widgetMapper.insert(widget);
        if (insert > 0) {
            optLogger.info("widget ({}) create by user(:{})", widget, user.getUsername());
            return widget;
        } else {
            throw new ServerException("create widget fail");
        }
    }

    /**
     * 修改widget
     *
     * @param widgetUpdate
     * @param user
     * @return
     */
    @Override
    @Transactional
    public boolean updateWidget(WidgetUpdate widgetUpdate, User user) throws NotFoundException, UnAuthorizedExecption, ServerException {
        Widget widget = widgetMapper.getById(widgetUpdate.getId());
        if (null == widget) {
            log.info("widget (:{}) is not found", widgetUpdate.getId());
            throw new NotFoundException("widget is not found");
        }

        if(!projectAuth.isPorjectOwner(widget.getProjectId(), user.getId())) {
            throw new UnAuthorizedExecption("current user has no permission.");
        }

        // 如果前端带的widget config中的view为空，设置为viewId的值
        Long viewId = widgetUpdate.getViewId();
        Map widgetUpdateConfig = BDPJettyServerHelper.gson().fromJson(widgetUpdate.getConfig(), Map.class);
        if(widgetUpdateConfig.get("view").toString().equals("{}")) {
            widgetUpdateConfig.put("view", viewId);
            widgetUpdate.setConfig(BDPJettyServerHelper.gson().toJson(widgetUpdateConfig));
        }

        ProjectDetail projectDetail = null;
        try {
            projectDetail = projectService.getProjectDetail(widget.getProjectId(), user, false);
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

        //校验权限
        if (projectPermission.getWidgetPermission() < UserPermissionEnum.WRITE.getPermission()) {
            log.info("user {} have not permisson to update widget", user.getUsername());
            throw new UnAuthorizedExecption("you have not permission to update widget");
        }

        if (isExist(widgetUpdate.getName(), widgetUpdate.getId(), projectDetail.getId())) {
            log.info("the widget {} name is already taken", widgetUpdate.getName());
            throw new ServerException("the widget name is already taken");
        }

        if(widgetUpdate.getViewId() != null && widgetUpdate.getViewId() > 0){
            View view = viewMapper.getById(widgetUpdate.getViewId());
            if (null == view) {
                log.info("view (:{}) not found", widgetUpdate.getViewId());
                throw new NotFoundException("view not found");
            }
        }

        String originStr = widget.toString();


        // 判断是否更新过config中的指标
//        if(widgetUpdate.getConfig().equals(widget.getConfig())) {
//            widget.updateByWithoutUpdateTime(user.getId());
//        } else {
//            widget.updatedBy(user.getId());
//        }
        JSONObject jsonObject = JSONObject.parseObject(widgetUpdate.getConfig());
        JSONObject queryJsonObject = jsonObject.getJSONObject("query");
        List<String> groupsList = new ArrayList<>();
        List<String> column = new ArrayList<>();
        List<String> func = new ArrayList<>();
        List<WidgetUpdateFilters> widgetUpdateFilters = new ArrayList<>();
        if (queryJsonObject == null){
            widget.updateByWithoutUpdateTime(user.getId());
        }else {
            // 1. 指标
            JSONArray groups = queryJsonObject.getJSONArray("groups");
            String jsonString = JSONArray.toJSONString(groups);
            //groupsList中存储分类型
            // 2. 维度
            groupsList = JSONArray.parseArray(jsonString, String.class);
            JSONArray aggregators = queryJsonObject.getJSONArray("aggregators");
            String jsonString1 = JSONArray.toJSONString(aggregators);
            //aggregatorList中存储数值型
            List<Aggregator> aggregatorsList = JSONArray.parseArray(jsonString1, Aggregator.class);
            for (Aggregator aggregator : aggregatorsList) {
                // 3. 指标和聚合函数
                column.add(aggregator.getColumn());
                func.add(aggregator.getFunc());
            }
            JSONArray filters = queryJsonObject.getJSONArray("filters");
            String jsonStringFilters = JSONArray.toJSONString(filters);
            widgetUpdateFilters = JSONArray.parseArray(jsonStringFilters, WidgetUpdateFilters.class);
        }

        //widget中config
        JSONObject jsonObject1 = JSONObject.parseObject(widget.getConfig());
        JSONObject queryJsonObject1 = jsonObject1.getJSONObject("query");
        List<String> widgetGroupsList = new ArrayList<>();
        List<String> widgetColumn = new ArrayList<>();
        List<String> widgetFunc = new ArrayList<>();
        List<WidgetUpdateFilters> widgetUpdateFiltersList = new ArrayList<>();
        if (queryJsonObject1 == null){
            widget.updatedBy(user.getId());
        } else {
            // 1. 维度
            JSONArray widgetGroups = queryJsonObject1.getJSONArray("groups");
            String widgetJsonString = JSONArray.toJSONString(widgetGroups);
            //widgetGroupsList中存储数据库中widget表中config中分类型
            widgetGroupsList = JSONArray.parseArray(widgetJsonString, String.class);
            // 2. 指标
            JSONArray widgetAggregators = queryJsonObject1.getJSONArray("aggregators");
            String widgetJsonString1 = JSONArray.toJSONString(widgetAggregators);
            //widgetAggregatorList中存储数据库中widget表中config中分类型
            List<Aggregator> widgetAggregatorList = JSONArray.parseArray(widgetJsonString1, Aggregator.class);
            for (Aggregator aggregator : widgetAggregatorList) {
                // 3. 指标和聚合函数
                widgetColumn.add(aggregator.getColumn());
                widgetFunc.add(aggregator.getFunc());
            }
            JSONArray filters1 = queryJsonObject1.getJSONArray("filters");
            String jsonStringFilters1 = JSONArray.toJSONString(filters1);
            widgetUpdateFiltersList = JSONArray.parseArray(jsonStringFilters1, WidgetUpdateFilters.class);
        }
        if (groupsList.equals(widgetGroupsList) && column.equals(widgetColumn) && func.equals(widgetFunc) && widgetUpdateFilters.equals(widgetUpdateFiltersList)){
            widget.updateByWithoutUpdateTime(user.getId());
        } else {
            widget.updatedBy(user.getId());
        }

        BeanUtils.copyProperties(widgetUpdate, widget);
        int update = widgetMapper.update(widget);
        if (update > 0) {
            optLogger.info("widget ({}) is updated by user(:{}), origin: ({})", widget, user.getId(), originStr);
            return true;
        } else {
            throw new ServerException("update widget fail");
        }
    }

    /**
     * 删除widget
     *
     * @param id
     * @param user
     * @return
     */
    @Override
    @Transactional
    public boolean deleteWidget(Long id, User user) throws NotFoundException, UnAuthorizedExecption, ServerException {

        Widget widget = widgetMapper.getById(id);
        if (null == widget) {
            log.warn("widget (:{}) is not found", id);
            return true;
        } else {
            ProjectDetail projectDetail = null;
            try {
                projectDetail = projectService.getProjectDetail(widget.getProjectId(), user, false);
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

            //校验权限
            if (projectPermission.getWidgetPermission() < UserPermissionEnum.DELETE.getPermission()) {
                log.info("user {} have not permisson to delete widget", user.getUsername());
                throw new UnAuthorizedExecption("you have not permission to delete widget");
            }
        }

        if(!projectAuth.isPorjectOwner(widget.getProjectId(), user.getId())) {
            throw new UnAuthorizedExecption("current user has no permission.");
        }

        //删除引用widget的dashboard
        try {
            memDashboardWidgetMapper.deleteByWidget(id);
        } catch (Exception e) {
            log.error("delete dashboard by widget fail, because: ", e);
            throw new RuntimeException("delete dashboard by widget fail");
        }

        //删除引用widget的displayslide
        try {
            memDisplaySlideWidgetMapper.deleteByWidget(id);
        } catch (Exception e) {
            log.error("delete display by widget fail, because: ", e);
            throw new RuntimeException("delete display by widget fail");
        }

        try {
            widgetMapper.deleteById(id);
        } catch (Exception e) {
            log.error("delete widget fail, because: ", e);
            throw new RuntimeException("delete widget fail");
        }
        optLogger.info("widget ( {} ) delete by user( :{} )", widget.toString(), user.getId());

        return true;
    }


    /**
     * 共享widget
     *
     * @param id
     * @param user
     * @param username
     * @return
     */
    @Override
    public String shareWidget(Long id, User user, String username) throws NotFoundException, UnAuthorizedExecption, ServerException {

        Widget widget = widgetMapper.getById(id);
        if (null == widget) {
            log.info("widget (:{}) is not found", id);
            throw new NotFoundException("widget is not found");
        }

        ProjectPermission projectPermission = null;
        try {
            projectPermission = projectService.getProjectPermission(projectService.getProjectDetail(widget.getProjectId(), user, false), user);
        } catch (Exception e) {
            log.error("get project permission fail, because: ", e);
            throw new RuntimeException("get project permission fail");
        }

        //校验权限
        if (!projectPermission.getSharePermission()) {
            log.info("user {} have not permisson to share the widget {}", user.getUsername(), id);
            throw new UnAuthorizedExecption("you have not permission to share the widget");
        }
        String token = null;
        try {
            token = shareService.generateShareToken(id, username, user.getId());
        } catch (Exception e) {
            log.error("generate share token fail, because: ", e);
            throw new RuntimeException("generate share token fail");
        }
        return token;
    }


    @Override
    public String generationFile(Long id, ViewExecuteParam executeParam, User user, String type) throws NotFoundException, ServerException, UnAuthorizedExecption {
        String filePath = null;
        Widget widget = widgetMapper.getById(id);

        if (null == widget) {
            log.info("widget (:{}) not found", id);
            throw new NotFoundException("widget is not found");
        }

        ProjectDetail projectDetail = null;
        try {
            projectDetail = projectService.getProjectDetail(widget.getProjectId(), user, false);
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
        //校验权限
        if (!projectPermission.getDownloadPermission()) {
            log.info("user {} have not permisson to download the widget {}", user.getUsername(), id);
            throw new UnAuthorizedExecption("you have not permission to download the widget");
        }

        executeParam.setPageNo(-1);
        executeParam.setPageSize(-1);
        executeParam.setLimit(-1);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        String rootPath = fileUtils.fileBasePath +
                File.separator +
                "download" +
                File.separator +
                sdf.format(new Date()) +
                File.separator +
                type +
                File.separator;

        try {
            if (type.equals(FileTypeEnum.CSV.getType())) {
                ViewWithSource viewWithSource = viewMapper.getViewWithSource(widget.getViewId());

                boolean maintainer = projectService.isMaintainer(projectDetail, user);

                PaginateWithQueryColumns paginate = viewService.getResultDataList(maintainer, viewWithSource, executeParam, user, false);
                List<QueryColumn> columns = paginate.getColumns();
                if (!CollectionUtils.isEmpty(columns)) {
                    File file = new File(rootPath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }

                    String csvName = widget.getName() + "_" +
                            System.currentTimeMillis() +
                            UUID.randomUUID().toString().replace("-", EMPTY) +
                            FileTypeEnum.CSV.getFormat();

                    filePath = CsvUtils.formatCsvWithFirstAsHeader(rootPath, csvName, columns, paginate.getResultList());
                }
            } else if (type.equals(FileTypeEnum.XLSX.getType())) {

                String excelName = widget.getName() + "_" +
                        System.currentTimeMillis() +
                        UUID.randomUUID().toString().replace("-", EMPTY) +
                        FileTypeEnum.XLSX.getFormat();


                HashSet<Widget> widgets = new HashSet<>();
                widgets.add(widget);
                Map<Long, ViewExecuteParam> executeParamMap = new HashMap<>();
                executeParamMap.put(widget.getId(), executeParam);

                filePath = rootPath + excelName;
                writeExcel(widgets, projectDetail, executeParamMap, filePath, user, false);
            } else {
                throw new ServerException("unknow file type");
            }
        } catch (Exception e) {
            throw new ServerException("generation " + type + " error!");
        }

        return serverUtils.getHost() + fileUtils.formatFilePath(filePath);
    }


    /**
     * widget列表数据写入指定excle文件
     *
     * @param widgets
     * @param projectDetail
     * @param executeParamMap
     * @param filePath
     * @param user
     * @param containType
     * @return
     * @throws Exception
     */
    public File writeExcel(Set<Widget> widgets,
                           ProjectDetail projectDetail, Map<Long, ViewExecuteParam> executeParamMap,
                           String filePath, User user, boolean containType) throws Exception {
        if (StringUtils.isEmpty(filePath)) {
            throw new ServerException("excel file path is EMPTY");
        }
        if (!filePath.trim().toLowerCase().endsWith(FileTypeEnum.XLSX.getFormat())) {
            throw new ServerException("unknow file format");
        }

        SXSSFWorkbook wb = new SXSSFWorkbook(1000);

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        CountDownLatch countDownLatch = new CountDownLatch(widgets.size());

        Iterator<Widget> iterator = widgets.iterator();
        int i = 1;

        ScriptEngine engine = getExecuptParamScriptEngine();

        boolean maintainer = projectService.isMaintainer(projectDetail, user);

        while (iterator.hasNext()) {
            Widget widget = iterator.next();
            final String sheetName = widgets.size() == 1 ? "Sheet" : "Sheet" + (widgets.size() - (i - 1));
            executorService.execute(() -> {
                Sheet sheet = null;
                try {
                    ViewWithProjectAndSource viewWithProjectAndSource = viewMapper.getViewWithProjectAndSourceById(widget.getViewId());

                    ViewExecuteParam executeParam = null;
                    if (null != executeParamMap && executeParamMap.containsKey(widget.getId())) {
                        executeParam = executeParamMap.get(widget.getId());
                    } else {
                        executeParam = getViewExecuteParam((engine), null, widget.getConfig(), null);
                    }

                    PaginateWithQueryColumns paginate = viewService.getResultDataList(maintainer, viewWithProjectAndSource, executeParam, user, false);

                    sheet = wb.createSheet(sheetName);
                    ExcelUtils.writeSheet(sheet, paginate.getColumns(), paginate.getResultList(), wb, containType, widget.getConfig(), executeParam.getParams());
                } catch (ServerException e) {
                    log.error("Error writing widget data to excel: ", e);
                    throw new ServerException("Error writing widget data to excel");
                } catch (SQLException e) {
                    log.error("Error writing widget data to excel: ", e);
                    throw new RuntimeException("Error writing widget data to excel");
                } finally {
                    sheet = null;
                    countDownLatch.countDown();
                }
            });

            i++;
        }

        countDownLatch.await();
        executorService.shutdown();

        File file = new File(filePath);
        File dir = new File(file.getParent());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        FileOutputStream out = new FileOutputStream(filePath);
        wb.write(out);
        out.flush();
        out.close();
        return file;
    }
}
