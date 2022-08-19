package com.webank.wedatasphere.dss.visualis.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ExecThreadPool {

    @Value("${thread-pool.corePoolSize:100}")
    private Integer corePoolSize;

    @Value("${thread-pool.maxPoolSize:200}")
    private Integer maximumPoolSize;

    @Value("${thread-pool.queueSize:100}")
    private Integer queueSize;

    // 120s
    @Value("${thread-pool.keepAliveTime:600}")
    private Long keepAliveTime;

    private TimeUnit unit = TimeUnit.SECONDS;

    @Bean("execPool")
    public ThreadPoolExecutor threadPool() {
        // 线程工厂
        ThreadFactory threadFactory = new CustomizableThreadFactory("exec-pool-");
        // 初始化线程池
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
                unit, new LinkedBlockingQueue<>(queueSize), threadFactory);
    }

}
