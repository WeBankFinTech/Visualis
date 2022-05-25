package com.webank.wedatasphere.dss.visualis.query.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.webank.wedatasphere.dss.visualis.configuration.CommonConfig;
import com.webank.wedatasphere.dss.visualis.model.PaginateWithExecStatus;
import com.webank.wedatasphere.dss.visualis.utils.VisualisUtils;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.scheduler.queue.SchedulerEventState;
import edp.core.model.PaginateWithQueryColumns;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JdbcAsyncUtils {

    static String EXEC_ID_PREFIX = "JDBC_";

    static Cache<String, PaginateWithQueryColumns> jdbcCache = CacheBuilder.newBuilder()
            .expireAfterWrite((Long) CommonConfig.JDBC_CACHE_FLUSH_WRITE().getValue(), TimeUnit.SECONDS)
            .build();

    public static void putResult(String execId, PaginateWithQueryColumns resultSet) {
        jdbcCache.put(execId, resultSet);
    }

    public static PaginateWithQueryColumns getResult(String execId) {
        String instance = VisualisUtils.getInstanceByHAExecId(execId);
        if (instance.equals(Sender.getThisInstance())) {
            PaginateWithQueryColumns paginateWithQueryColumns = jdbcCache.getIfPresent(execId);
            jdbcCache.invalidate(execId);
            return paginateWithQueryColumns;
        } else {
            return VisualisUtils.getJDBCResult(instance, execId);
        }
    }

    public static String generateExecId() {
        return VisualisUtils.getHAExecId(EXEC_ID_PREFIX + UUID.randomUUID().toString());
    }

    public static boolean isJdbcExecId(String execId) {
        return execId.startsWith(EXEC_ID_PREFIX);
    }

    public static PaginateWithExecStatus getJdbcProgress(String execId) {
        PaginateWithExecStatus paginateWithExecStatus = new PaginateWithExecStatus();
        paginateWithExecStatus.setExecId(execId);
        paginateWithExecStatus.setProgress(1L);
        paginateWithExecStatus.setStatus(SchedulerEventState.Succeed().toString());
        return paginateWithExecStatus;
    }
}
