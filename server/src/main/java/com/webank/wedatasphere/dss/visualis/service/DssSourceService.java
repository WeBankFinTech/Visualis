package com.webank.wedatasphere.dss.visualis.service;

import com.webank.wedatasphere.dss.visualis.model.optmodel.ExportedProject;
import com.webank.wedatasphere.dss.visualis.model.optmodel.IdCatalog;

public interface DssSourceService {

    void importSource(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog) throws Exception;

}
