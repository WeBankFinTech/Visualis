package com.webank.wedatasphere.dss.visualis.service.impl;

import com.webank.wedatasphere.dss.visualis.service.DssProjectService;
import com.webank.wedatasphere.dss.visualis.service.DssSourceService;
import com.webank.wedatasphere.dss.visualis.service.DssViewService;
import com.webank.wedatasphere.dss.visualis.service.DssWidgetService;
import com.webank.wedatasphere.dss.visualis.service.DssDisplayService;
import com.webank.wedatasphere.dss.visualis.service.DssDashboradService;
import com.webank.wedatasphere.dss.visualis.service.Utils;
import com.webank.wedatasphere.dss.visualis.configuration.CommonConfig;
import com.webank.wedatasphere.dss.visualis.enums.ModuleEnum;
import com.webank.wedatasphere.dss.visualis.model.optmodel.ExportedProject;
import com.webank.wedatasphere.dss.visualis.model.optmodel.IdCatalog;
import com.webank.wedatasphere.dss.visualis.utils.StringConstant;
import edp.davinci.core.common.ResultMap;
import org.apache.commons.io.FileUtils;
import org.apache.linkis.adapt.LinkisUtils;
import org.apache.linkis.bml.client.BmlClient;
import org.apache.linkis.bml.client.BmlClientFactory;
import org.apache.linkis.bml.protocol.BmlDownloadResponse;
import org.apache.linkis.bml.protocol.BmlUploadResponse;
import org.apache.linkis.common.exception.ErrorException;
import edp.core.exception.ServerException;
import edp.davinci.dao.ProjectMapper;
import edp.davinci.dao.UserMapper;
import edp.davinci.model.Project;
import edp.davinci.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service("dssProjectService")
public class ProjectServiceImpl implements DssProjectService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Resource(name = "dssSourceService")
    private DssSourceService dssSourceService;

    @Resource(name = "dssViewService")
    private DssViewService dssViewService;

    @Resource(name = "dssWidgetService")
    private DssWidgetService dssWidgetService;

    @Resource(name = "dssDisplayService")
    private DssDisplayService dssDisplayService;

    @Resource(name = "dssDashboradService")
    private DssDashboradService dssDashboradService;

    @Override
    public ResultMap getDefaultProject(String userName) throws Exception {

        Map<String, Object> resultDataMap = new HashMap<>();
        ResultMap resultMap = new ResultMap();

        Project project;
        User user = userMapper.selectByUsername(userName);
        List<Project> defaultProjects = projectMapper.getProjectByNameWithUserId(CommonConfig.DEFAULT_PROJECT_NAME().getValue(), user.getId());
        if (CollectionUtils.isEmpty(defaultProjects)) {
            project = new Project();
            project.setName(CommonConfig.DEFAULT_PROJECT_NAME().getValue());
            project.setCreateTime(new Date());
            project.setCreateUserId(user.getId());
            project.setDescription(StringConstant.EMPTY);
            project.setInitialOrgId(null);
            project.setIsTransfer(false);
            project.setPic(null);
            project.setStarNum(0);
            project.setVisibility(true);
            project.setOrgId(null);
            project.setUserId(user.getId());
            projectMapper.insert(project);
        } else {
            project = defaultProjects.get(0);
        }
        resultDataMap.put("project", project);
        return resultMap.success().payload(resultDataMap);
    }

    /**
     * 导出工程的过程步骤：
     * 1. 获取 DSS传递过来的导出的工程JSON.
     * 2. 从JSON中获取工程ID和全量导出标识.
     * 3. 通过工程ID，设置导出工程的以下信息：
     * 3.1. 导出关联的Display.
     * 3.2. 导出关联的DashBoard.
     * 3.3. 导出关联的Widget.
     * 3.4. 导出关联的View.
     * 3.5. 导出关联的Source.
     * 3.6. 生成一个ExportedProject对象.
     * 4. 把ExportedProject对象转换成JSON，上传至BML.
     * 5. 将返回的BML 资源ID和版本号设置到响应消息中并返回.
     */
    @Override
    public ResultMap exportProject(Map<String, String> params, String userName) throws Exception {

        Map<String, Object> resultDataMap = new HashMap<>();
        ResultMap resultMap = new ResultMap();

        ExportedProject exportedProject;

        Long projectId = Long.parseLong(params.get(StringConstant.PROJECT_ID));
        Boolean partial = Boolean.parseBoolean(params.get(StringConstant.PARTIAL));
        Map<String, Set<Long>> moduleIdsMap = Utils.getModuleIdsMap(params);

        exportedProject = doExport(projectId, moduleIdsMap, partial);

        String exported = LinkisUtils.gson().toJson(exportedProject);
        BmlClient bmlClient = BmlClientFactory.createBmlClient(userName);
        BmlUploadResponse bmlUploadResponse = bmlClient.uploadShareResource(userName, exportedProject.getName(),
                StringConstant.BML_FILE_PREFIX + UUID.randomUUID(), new ByteArrayInputStream(exported.getBytes(StandardCharsets.UTF_8)));

        if (bmlUploadResponse == null || !bmlUploadResponse.isSuccess()) {
            throw new ServerException("cannot upload exported data to BML");
        }

        log.info("{} is exporting the project, uploaded to BML the resourceID is {} and the version is {}",
                userName, bmlUploadResponse.resourceId(), bmlUploadResponse.version());

        resultDataMap.put("resourceId", bmlUploadResponse.resourceId());
        resultDataMap.put("version", bmlUploadResponse.version());

        resultMap.success().payload(resultDataMap);
        return resultMap;
    }

    @Override
    public ResultMap importProject(Map<String, String> params, String userName) throws Exception {

        Map<String, Object> resultDataMap = new HashMap<>();
        ResultMap resultMap = new ResultMap();

        String resourceId = params.get(StringConstant.RESOURCE_ID);
        String version = params.get(StringConstant.VERSION);
        Long projectId = Long.parseLong(params.get(StringConstant.PROJECT_ID));
        String projectVersion = params.get(StringConstant.PROJECT_VERSION);
        String flowVersion = params.get(StringConstant.WORKFLOW_VERSION);
        String versionSuffix = projectVersion + "_" + flowVersion;
        BmlClient bmlClient = BmlClientFactory.createBmlClient(userName);
        BmlDownloadResponse bmlDownloadResponse = bmlClient.downloadShareResource(userName, resourceId, version);
        if (bmlDownloadResponse == null || !bmlDownloadResponse.isSuccess()) {
            throw new ServerException("cannot download exported data from BML");
        }
        try {
            String projectJson = IOUtils.toString(bmlDownloadResponse.inputStream());

            IdCatalog idCatalog = doImport(projectJson, projectId, versionSuffix);

            resultDataMap.put("widget", idCatalog.getWidget());
            resultDataMap.put("dashboard", idCatalog.getDashboard());
            resultDataMap.put("dashboardPortal", idCatalog.getDashboardPortal());
            resultDataMap.put("view", idCatalog.getView());
            resultDataMap.put("display", idCatalog.getDisplay());
        } finally {
            IOUtils.closeQuietly(bmlDownloadResponse.inputStream());
        }
        return resultMap.success().payload(resultDataMap);
    }


    @Override
    public ResultMap copyProject(Map<String, String> params,  String userName) throws Exception {

        Map<String, Object> resultDataMap = new HashMap<>();
        ResultMap resultMap = new ResultMap();

        log.info("begin to copy in visualis params is {}", params);

        Map<String, Set<Long>> moduleIdsMap = Utils.getModuleIdsMap(params);

        String projectVersion = params.getOrDefault(StringConstant.PROJECT_VERSION, StringConstant.PROJECT_VERSION_DEFAULT_VALUE);
        String flowVersion = params.get(StringConstant.WORKFLOW_VERSION);
        if (StringUtils.isEmpty(flowVersion)) {
            log.error("flowVersion is null, can not copy flow to a newest version");
            flowVersion = StringConstant.WORKFLOW_VERSION_DEFAULT_VALUE;
        }
        String contextIdStr = params.get("contextID");
        if (StringUtils.isEmpty(contextIdStr)) {
            throw new ErrorException(20012, "contextId is null, visualis can not do copy");
        }

        String projectIdStr = params.get("projectId");
        Long refProjectId = null;
        if (projectIdStr != null) {
            refProjectId = Long.valueOf(projectIdStr);
        }
        Long projectId = getProjectId(moduleIdsMap);

        if (refProjectId != null && !projectId.equals(refProjectId)) {
            projectId = refProjectId;
        }

        ExportedProject exportedProject = doExport(projectId, moduleIdsMap, true);

        doCopy(contextIdStr, moduleIdsMap, exportedProject);

        String projectJson = LinkisUtils.gson().toJson(exportedProject);
        String versionSuffix = projectVersion + "_" + flowVersion;

        IdCatalog idCatalog = doImport(projectJson, projectId, versionSuffix);

        resultDataMap.put("widget", idCatalog.getWidget());
        resultDataMap.put("dashboard", idCatalog.getDashboard());
        resultDataMap.put("dashboardPortal", idCatalog.getDashboardPortal());
        resultDataMap.put("display", idCatalog.getDisplay());
        resultDataMap.put("view", idCatalog.getView());

        return resultMap.success().payload(resultDataMap);
    }

    @Override
    public ResultMap readProject(String fileName, Long projectId, String userName) throws Exception {

        Map<String, Object> resultDataMap = new HashMap<>();
        ResultMap resultMap = new ResultMap();

        String versionSuffix = "";
        String projectJson = FileUtils.readFileToString(new File(CommonConfig.EXPORT_PROJECT_DIR().getValue() + fileName));

        IdCatalog idCatalog = doImport(projectJson, projectId, versionSuffix);

        resultDataMap.put("widget", idCatalog.getWidget());
        resultDataMap.put("dashboard", idCatalog.getDashboard());
        resultDataMap.put("dashboardPortal", idCatalog.getDashboardPortal());
        resultDataMap.put("view", idCatalog.getView());
        resultDataMap.put("display", idCatalog.getDisplay());

        return resultMap.success().payload(resultDataMap);
    }

    private ExportedProject doExport(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial) throws Exception {
        ExportedProject exportedProject = new ExportedProject();
        Project project = projectMapper.getById(projectId);
        exportedProject.setName(project.getName());

        // moduleIdsMap为导出的组件Map

        dssDisplayService.exportDisplays(projectId, moduleIdsMap, partial, exportedProject);

        dssDashboradService.exportDashboardPortals(projectId, moduleIdsMap, partial, exportedProject);

        dssWidgetService.exportWidgets(projectId, moduleIdsMap, partial, exportedProject);

        dssViewService.exportViews(projectId, moduleIdsMap, partial, exportedProject);

        return exportedProject;
    }

    public IdCatalog doImport(String projectJson, Long projectId, String versionSuffix) throws Exception {
        ExportedProject exportedProject = LinkisUtils.gson().fromJson(projectJson, ExportedProject.class);

        // idCatalog为记录导出节点时，其导出的关联关系的记录Map
        IdCatalog idCatalog = new IdCatalog();

        dssSourceService.importSource(projectId, versionSuffix, exportedProject, idCatalog);

        dssViewService.importViews(projectId, versionSuffix, exportedProject, idCatalog);

        dssWidgetService.importWidget(projectId, versionSuffix, exportedProject, idCatalog);

        dssDisplayService.importDisplay(projectId, versionSuffix, exportedProject, idCatalog);

        dssDashboradService.importDashboard(projectId, versionSuffix, exportedProject, idCatalog);

        return idCatalog;
    }

    public void doCopy(String contextIdStr, Map<String, Set<Long>> moduleIdsMap, ExportedProject exportedProject) throws Exception {

        dssWidgetService.copyWidget(contextIdStr, moduleIdsMap, exportedProject);

        dssDisplayService.copyDisplay(moduleIdsMap, exportedProject);

        dssDashboradService.copyDashboardPortal(moduleIdsMap, exportedProject);

        dssViewService.copyView(moduleIdsMap, exportedProject);
    }

    public Long getProjectId(Map<String, Set<Long>> moduleIdsMap) {
        Set<Long> widgets = moduleIdsMap.get(ModuleEnum.WIDGET_IDS.getName());
        Set<Long> displays = moduleIdsMap.get(ModuleEnum.DISPLAY_IDS.getName());
        Set<Long> dashboards = moduleIdsMap.get(ModuleEnum.DASHBOARD_PORTAL_IDS.getName());
        Set<Long> views = moduleIdsMap.get(ModuleEnum.VIEW_IDS.getName());
        if (!widgets.isEmpty()) {
            return projectMapper.getProjectIdByWidgetId(widgets.iterator().next());
        } else if (!displays.isEmpty()) {
            return projectMapper.getProjectByDisplayId(displays.iterator().next());
        } else if (!dashboards.isEmpty()) {
            return projectMapper.getProjectIdByDashboardId(dashboards.iterator().next());
        } else if (!views.isEmpty()) {
            return projectMapper.getProjectIdByViewId(views.iterator().next());
        } else {
            log.error("widgets displays dashboards are all empty");
            return -1L;
        }
    }
}
