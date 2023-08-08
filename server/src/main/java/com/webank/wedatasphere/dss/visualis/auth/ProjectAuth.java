package com.webank.wedatasphere.dss.visualis.auth;

/**
 * 由于Visualis只有个人项目的概念，DSS项目是一个共享项目，存在编辑用户，查看用户，运维用户，
 * Visualis不存在这些概念，所以Visualis的工程和组件都没有限制用户权限，导致存在用户可以通过
 * 接口进行越权操作。
 * */
public interface ProjectAuth {
    boolean isPorjectOwner(Long projectId, Long userId);
}
