package com.webank.wedatasphere.dss.visualis.service.impl;

import com.webank.wedatasphere.dss.visualis.service.DssSourceService;
import com.webank.wedatasphere.dss.visualis.service.Utils;
import com.webank.wedatasphere.dss.visualis.model.optmodel.ExportedProject;
import com.webank.wedatasphere.dss.visualis.model.optmodel.IdCatalog;
import edp.davinci.dao.SourceMapper;
import edp.davinci.model.Source;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("dssSourceService")
public class SourceServiceImpl implements DssSourceService {

    @Autowired
    SourceMapper sourceMapper;

    @Override
    public void importSource(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog) throws Exception {
        List<Source> sources = exportedProject.getSources();
        if (sources == null) {
            return;
        }
        for (Source source : sources) {
            Long oldId = source.getId();
            source.setProjectId(projectId);
            Long existingId = sourceMapper.getByNameWithProjectId(source.getName(), projectId);
            if (existingId != null) {
                idCatalog.getSource().put(oldId, existingId);
            } else {
                sourceMapper.insert(source);
                idCatalog.getSource().put(oldId, source.getId());
            }
        }
    }
}
