package com.webank.wedatasphere.dss.visualis.restful;

import com.webank.wedatasphere.dss.visualis.configuration.CommonConfig;
import com.webank.wedatasphere.dss.visualis.service.DssProjectService;
import edp.core.annotation.MethodLog;
import edp.core.utils.CollectionUtils;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.dao.ProjectMapper;
import edp.davinci.dao.UserMapper;
import edp.davinci.model.Project;
import edp.davinci.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = Constants.RESTFUL_BASE_PATH + "project", produces = MediaType.APPLICATION_JSON_VALUE)
@ComponentScan(basePackages = {"edp", "com.webank.wedatasphere.dss"})
public class ProjectRestful {

    @Resource(name = "dssProjectService")
    DssProjectService dssProjectService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ProjectMapper projectMapper;

    /**
     * 该接口由Scriptis结果集可视化分析功能请求
     * */
    @MethodLog
    @RequestMapping(path = "default", method = RequestMethod.GET)
    public Message getDefault(HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        User user = userMapper.selectByUsername(userName);
        List<Project> defaultProjects = projectMapper.getProjectByNameWithUserId(CommonConfig.DEFAULT_PROJECT_NAME().getValue(), user.getId());
        Project project = null;
        if (CollectionUtils.isEmpty(defaultProjects)) {
            project = new Project();
            project.setName(CommonConfig.DEFAULT_PROJECT_NAME().getValue());
            project.setCreateTime(new Date());
            project.setCreateUserId(user.getId());
            project.setDescription("");
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
        Message message = Message.ok().data("project", project);
        return message;
    }


    @MethodLog
    @RequestMapping(path = "export", method = RequestMethod.POST)
    public ResponseEntity exportProject(HttpServletRequest req, @RequestBody Map<String, String> params) {
        String userName = SecurityFilter.getLoginUsername(req);
        ResultMap resultMap = null;
        try {
            resultMap = dssProjectService.exportProject(params, userName);
        } catch (Exception e) {
            log.error("export project error, because: " , e);
            resultMap = new ResultMap().fail().message(e.getMessage());
        }
        return ResponseEntity.ok(resultMap);
    }

    @MethodLog
    @RequestMapping(path = "import", method = RequestMethod.POST)
    public ResponseEntity importProject(HttpServletRequest req, @RequestBody Map<String, String> params) {
        String userName = SecurityFilter.getLoginUsername(req);
        ResultMap resultMap = null;
        try {
            resultMap = dssProjectService.importProject(params, userName);
        } catch (Exception e) {
            log.error("import project error, because: " , e);
            resultMap = new ResultMap().fail().message(e.getMessage());
        }
        return ResponseEntity.ok(resultMap);
    }

    @MethodLog
    @RequestMapping(path = "read", method = RequestMethod.GET)
    public ResponseEntity read(HttpServletRequest req, @RequestParam(value = "fileName", required = false) String fileName,
                               @RequestParam(value = "projectId", required = false) Long projectId) {
        String userName = SecurityFilter.getLoginUsername(req);
        ResultMap resultMap = null;
        try {
            resultMap = dssProjectService.readProject(fileName, projectId, userName);
        } catch (Exception e) {
            log.error("read project error, because: " , e);
            resultMap = new ResultMap().fail().message(e.getMessage());
        }
        return ResponseEntity.ok(resultMap);
    }

    @MethodLog
    @RequestMapping(path = "copy", method = RequestMethod.POST)
    public ResponseEntity copy(HttpServletRequest req, @RequestBody Map<String, String> params) {
        String userName = SecurityFilter.getLoginUsername(req);
        ResultMap resultMap = null;
        try {
            resultMap = dssProjectService.copyProject(params, userName);
        } catch (Exception e) {
            log.error("copy project error, because: " , e);
            resultMap = new ResultMap().fail().message(e.getMessage());
        }
        return ResponseEntity.ok(resultMap);
    }

}
