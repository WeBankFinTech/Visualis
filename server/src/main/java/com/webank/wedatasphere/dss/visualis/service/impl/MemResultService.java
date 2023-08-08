package com.webank.wedatasphere.dss.visualis.service.impl;

import com.github.pagehelper.cache.GuavaCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.webank.wedatasphere.dss.visualis.service.ResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnProperty(value="preview.asynexecute.cache", havingValue="guava")
public class MemResultService implements ResultService {

    final private GuavaCache<String, File> cache = (GuavaCache<String, File>) CacheBuilder.newBuilder().maximumSize(500).
            expireAfterAccess(4, TimeUnit.HOURS).removalListener(notification -> {
                log.info("Cache invalidation. Cache key is {}", notification.getKey());
            }).build(new CacheLoader<String, File>() {

                @Override
                public File load(String key) throws Exception {
                    return this.load(key);
                }
            });

    @Override
    public void setResult(String keyValue, File object) {
        if(keyValue == null) {
            log.error("when get cache value, key is null");
            return;
        }
        if(cache.get(keyValue) != null) {
            log.warn("There are resource files with the same key in the cache. key is {}", keyValue);
            cache.put(keyValue, object);
        } else {
            cache.put(keyValue, object);
        }
    }

    @Override
    public Object getResult(String keyValue) {
        if(keyValue == null) {
            log.error("when get cache value, key is null");
            return null;
        }
        return cache.get(keyValue);
    }

    @Override
    public boolean exist(String keyValue) {
        if(keyValue != null) {
            log.error("when get cache value, key is null");
            return false;
        }
        return cache.get(keyValue) != null;
    }
}
