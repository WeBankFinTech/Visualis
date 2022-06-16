package com.webank.wedatasphere.dss.visualis.auth;

import com.webank.wedatasphere.dss.visualis.configuration.CommonConfig;
import edp.davinci.dao.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectAuthImpl implements ProjectAuth {

    private static final Boolean CHECK_PROJECT_USER  = (Boolean) CommonConfig.CHECK_PROJECT_USER().getValue();

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public boolean isPorjectOwner(Long projectId, Long userId) {
        if(CHECK_PROJECT_USER) {
            Integer projectUserId = projectMapper.getProjectUserId(projectId);
            if(null == projectUserId) {
                return false;
            }
            if(projectUserId.intValue() == userId.intValue()) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
}
