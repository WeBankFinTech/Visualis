package com.webank.wedatasphere.dss.visualis.auth;

public interface ProjectAuth {
    boolean isPorjectOwner(Long projectId, Long userId);
}
