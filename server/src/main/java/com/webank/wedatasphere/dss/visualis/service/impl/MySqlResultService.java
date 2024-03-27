package com.webank.wedatasphere.dss.visualis.service.impl;

import com.webank.wedatasphere.dss.visualis.service.ResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@ConditionalOnProperty(value="preview.asynexecute.cache", havingValue="mysql")
public class MySqlResultService implements ResultService {

    @Override
    public void setResult(String keyValue, File object) {

    }

    @Override
    public Object getResult(String key) {
        return null;
    }

    @Override
    public boolean exist(String key) {
        return false;
    }
}
