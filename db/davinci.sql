SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 调度任务
DROP TABLE IF EXISTS `cron_job`;
CREATE TABLE `cron_job` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `project_id` bigint(20) NOT NULL,
  `job_type` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `job_status` varchar(10) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `cron_expression` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `config` text COLLATE utf8_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `exec_log` text COLLATE utf8_unicode_ci,
  `create_by` bigint(20) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `name_UNIQUE` (`name`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=13 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- 仪表盘
DROP TABLE IF EXISTS `dashboard`;
CREATE TABLE `dashboard`
(
    `id`                  bigint(20)   NOT NULL AUTO_INCREMENT,
    `name`                varchar(255) NOT NULL,
    `dashboard_portal_id` bigint(20)   NOT NULL,
    `type`                smallint(1)  NOT NULL,
    `index`               int(4)       NOT NULL, -- 1为文件0为DashBoard
    `parent_id`           bigint(20)   NOT NULL DEFAULT '0',
    `config`              text,
    `full_parent_Id`      varchar(100)          DEFAULT NULL,
    `create_by`           bigint(20)            DEFAULT NULL,
    `create_time`         datetime              DEFAULT NULL,
    `update_by`           bigint(20)            DEFAULT NULL,
    `update_time`         datetime              DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_dashboard_id` (`dashboard_portal_id`) USING BTREE,
    KEY `idx_parent_id` (`parent_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- DashBoard顶部控件
DROP TABLE IF EXISTS `dashboard_portal`;
CREATE TABLE `dashboard_portal`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `name`        varchar(255) NOT NULL,
    `description` varchar(255)          DEFAULT NULL,
    `project_id`  bigint(20)   NOT NULL,
    `avatar`      varchar(255)          DEFAULT NULL,
    `publish`     tinyint(1)   NOT NULL DEFAULT '0',
    `create_by`   bigint(20)            DEFAULT NULL,
    `create_time` datetime              DEFAULT NULL,
    `update_by`   bigint(20)            DEFAULT NULL,
    `update_time` datetime              DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_project_id` (`project_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- Display看板
DROP TABLE IF EXISTS `display`;
CREATE TABLE `display`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `name`        varchar(255) NOT NULL,
    `description` varchar(255) DEFAULT NULL,
    `project_id`  bigint(20)   NOT NULL,
    `avatar`      varchar(255) DEFAULT NULL,
    `publish`     tinyint(1)   NOT NULL,
    `create_by`   bigint(20)   DEFAULT NULL,
    `create_time` datetime     DEFAULT NULL,
    `update_by`   bigint(20)   DEFAULT NULL,
    `update_time` datetime     DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_project_id` (`project_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- Display的画布组件
DROP TABLE IF EXISTS `display_slide`;
CREATE TABLE `display_slide`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `display_id`  bigint(20) NOT NULL,
    `index`       int(12)    NOT NULL,
    `config`      text       NOT NULL,
    `create_by`   bigint(20) DEFAULT NULL,
    `create_time` datetime   DEFAULT NULL,
    `update_by`   bigint(20) DEFAULT NULL,
    `update_time` datetime   DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_display_id` (`display_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- 下载记录
DROP TABLE IF EXISTS `download_record`;
CREATE TABLE `download_record`
(
    `id`                 bigint(20)   NOT NULL AUTO_INCREMENT,
    `name`               varchar(255) NOT NULL,
    `user_id`            bigint(20)   NOT NULL,
    `path`               varchar(255) DEFAULT NULL,
    `status`             smallint(1)  NOT NULL,
    `create_time`        datetime     NOT NULL,
    `last_download_time` datetime     DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_user` (`user_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 点赞喜欢
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id`     bigint(20) NOT NULL,
    `project_id`  bigint(20) NOT NULL,
    `create_time` datetime   NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `idx_user_project` (`user_id`, `project_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- dashboard里面wideget成员表
DROP TABLE IF EXISTS `mem_dashboard_widget`;
CREATE TABLE `mem_dashboard_widget`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT,
    `dashboard_id` bigint(20) NOT NULL,
    `widget_Id`    bigint(20)          DEFAULT NULL,
    `x`            int(12)    NOT NULL,
    `y`            int(12)    NOT NULL,
    `width`        int(12)    NOT NULL,
    `height`       int(12)    NOT NULL,
    `polling`      tinyint(1) NOT NULL DEFAULT '0',
    `frequency`    int(12)             DEFAULT NULL,
    `config`       text,
    `create_by`    bigint(20)          DEFAULT NULL,
    `create_time`  datetime            DEFAULT NULL,
    `update_by`    bigint(20)          DEFAULT NULL,
    `update_time`  datetime            DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_protal_id` (`dashboard_id`) USING BTREE,
    KEY `idx_widget_id` (`widget_Id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- Display的画布组件中成员表
DROP TABLE IF EXISTS `mem_display_slide_widget`;
CREATE TABLE `mem_display_slide_widget`
(
    `id`               bigint(20)   NOT NULL AUTO_INCREMENT,
    `display_slide_id` bigint(20)   NOT NULL,
    `widget_id`        bigint(20)            DEFAULT NULL,
    `name`             varchar(255) NOT NULL,
    `params`           text         NOT NULL,
    `type`             smallint(1)  NOT NULL,
    `sub_type`         smallint(2)           DEFAULT NULL,
    `index`            int(12)      NOT NULL DEFAULT '0',
    `create_by`        bigint(20)            DEFAULT NULL,
    `create_time`      datetime              DEFAULT NULL,
    `update_by`        bigint(20)            DEFAULT NULL,
    `update_time`      datetime              DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_slide_id` (`display_slide_id`) USING BTREE,
    KEY `idx_widget_id` (`widget_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- 组织表
DROP TABLE IF EXISTS `organization`;
CREATE TABLE `organization`
(
    `id`                   bigint(20)   NOT NULL AUTO_INCREMENT,
    `name`                 varchar(255) NOT NULL,
    `description`          varchar(255)          DEFAULT NULL,
    `avatar`               varchar(255)          DEFAULT NULL,
    `user_id`              bigint(20)   NOT NULL,
    `project_num`          int(20)               DEFAULT '0',
    `member_num`           int(20)               DEFAULT '0',
    `role_num`             int(20)               DEFAULT '0',
    `allow_create_project` tinyint(1)            DEFAULT '1',
    `member_permission`    smallint(1)  NOT NULL DEFAULT '0',
    `create_time`          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`            bigint(20)   NOT NULL DEFAULT '0',
    `update_time`          timestamp    NULL     DEFAULT NULL,
    `update_by`            bigint(20)            DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- 平台表
DROP TABLE IF EXISTS `platform`;
CREATE TABLE `platform`
(
    `id`               bigint(20)   NOT NULL,
    `name`             varchar(255) NOT NULL,
    `platform`         varchar(255) NOT NULL,
    `code`             varchar(32)  NOT NULL,
    `checkCode`        varchar(255) DEFAULT NULL,
    `checkSystemToken` varchar(255) DEFAULT NULL,
    `checkUrl`         varchar(255) DEFAULT NULL,
    `alternateField1`  varchar(255) DEFAULT NULL,
    `alternateField2`  varchar(255) DEFAULT NULL,
    `alternateField3`  varchar(255) DEFAULT NULL,
    `alternateField4`  varchar(255) DEFAULT NULL,
    `alternateField5`  varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- 工程项目表
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project`
(
    `id`             bigint(20)   NOT NULL AUTO_INCREMENT,
    `name`           varchar(255) NOT NULL,
    `description`    varchar(255)          DEFAULT NULL,
    `pic`            varchar(255)          DEFAULT NULL,
    `org_id`         bigint(20)   NOT NULL,
    `user_id`        bigint(20)   NOT NULL,
    `visibility`     tinyint(1)            DEFAULT '1',
    `star_num`       int(11)               DEFAULT '0',
    `is_transfer`    tinyint(1)   NOT NULL DEFAULT '0',
    `initial_org_id` bigint(20)   NOT NULL,
    `create_by`      bigint(20)            DEFAULT NULL,
    `create_time`    datetime              DEFAULT NULL,
    `update_by`      bigint(20)            DEFAULT NULL,
    `update_time`    datetime              DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- 工程管理员表
DROP TABLE IF EXISTS `rel_project_admin`;
CREATE TABLE `rel_project_admin`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `project_id`  bigint(20) NOT NULL,
    `user_id`     bigint(20) NOT NULL,
    `create_by`   bigint(20) DEFAULT NULL,
    `create_time` datetime   DEFAULT NULL,
    `update_by`   bigint(20) DEFAULT NULL,
    `update_time` datetime   DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `idx_project_user` (`project_id`, `user_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='project admin表';

-- dashboard角色表
DROP TABLE IF EXISTS `rel_role_dashboard`;
CREATE TABLE `rel_role_dashboard`
(
    `role_id`      bigint(20) NOT NULL,
    `dashboard_id` bigint(20) NOT NULL,
    `visible`      tinyint(1) NOT NULL DEFAULT '0',
    `create_by`    bigint(20)          DEFAULT NULL,
    `create_time`  datetime            DEFAULT NULL,
    `update_by`    bigint(20)          DEFAULT NULL,
    `update_time`  datetime            DEFAULT NULL,
    PRIMARY KEY (`role_id`, `dashboard_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- display角色表
DROP TABLE IF EXISTS `rel_role_display`;
CREATE TABLE `rel_role_display`
(
    `role_id`     bigint(20) NOT NULL,
    `display_id`  bigint(20) NOT NULL,
    `visible`     tinyint(1) NOT NULL DEFAULT '0',
    `create_by`   bigint(20)          DEFAULT NULL,
    `create_time` datetime            DEFAULT NULL,
    `update_by`   bigint(20)          DEFAULT NULL,
    `update_time` datetime            DEFAULT NULL,
    PRIMARY KEY (`role_id`, `display_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- dashprotal角色表
DROP TABLE IF EXISTS `rel_role_portal`;
CREATE TABLE `rel_role_portal`
(
    `role_id`     bigint(20) NOT NULL,
    `portal_id`   bigint(20) NOT NULL,
    `visible`     tinyint(1) NOT NULL DEFAULT '0',
    `create_by`   bigint(20)          DEFAULT NULL,
    `create_time` datetime            DEFAULT NULL,
    `update_by`   bigint(20)          DEFAULT NULL,
    `update_time` datetime            DEFAULT NULL,
    PRIMARY KEY (`role_id`, `portal_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 工程角色表
DROP TABLE IF EXISTS `rel_role_project`;
CREATE TABLE `rel_role_project`
(
    `id`                  bigint(20)  NOT NULL AUTO_INCREMENT,
    `project_id`          bigint(20)  NOT NULL,
    `role_id`             bigint(20)  NOT NULL,
    `source_permission`   smallint(1) NOT NULL DEFAULT '1',
    `view_permission`     smallint(1) NOT NULL DEFAULT '1',
    `widget_permission`   smallint(1) NOT NULL DEFAULT '1',
    `viz_permission`      smallint(1) NOT NULL DEFAULT '1',
    `schedule_permission` smallint(1) NOT NULL DEFAULT '1',
    `share_permission`    tinyint(1)  NOT NULL DEFAULT '0',
    `download_permission` tinyint(1)  NOT NULL DEFAULT '0',
    `create_by`           bigint(20)           DEFAULT NULL,
    `create_time`         datetime             DEFAULT NULL,
    `update_by`           bigint(20)           DEFAULT NULL,
    `update_time`         datetime             DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `idx_role_project` (`project_id`, `role_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- display slide画布角色表
DROP TABLE IF EXISTS `rel_role_slide`;
CREATE TABLE `rel_role_slide`
(
    `role_id`     bigint(20) NOT NULL,
    `slide_id`    bigint(20) NOT NULL,
    `visible`     tinyint(1) NOT NULL DEFAULT '0',
    `create_by`   bigint(20)          DEFAULT NULL,
    `create_time` datetime            DEFAULT NULL,
    `update_by`   bigint(20)          DEFAULT NULL,
    `update_time` datetime            DEFAULT NULL,
    PRIMARY KEY (`role_id`, `slide_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 用户角色表
DROP TABLE IF EXISTS `rel_role_user`;
CREATE TABLE `rel_role_user`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id`     bigint(20) NOT NULL,
    `role_id`     bigint(20) NOT NULL,
    `create_by`   bigint(20) DEFAULT NULL,
    `create_time` datetime   DEFAULT NULL,
    `update_by`   bigint(20) DEFAULT NULL,
    `update_time` datetime   DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `idx_role_user` (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 角色视图表
DROP TABLE IF EXISTS `rel_role_view`;
CREATE TABLE `rel_role_view`
(
    `view_id`     bigint(20) NOT NULL,
    `role_id`     bigint(20) NOT NULL,
    `row_auth`    text,
    `column_auth` text,
    `create_by`   bigint(20) DEFAULT NULL,
    `create_time` datetime   DEFAULT NULL,
    `update_by`   bigint(20) DEFAULT NULL,
    `update_time` datetime   DEFAULT NULL,
    PRIMARY KEY (`view_id`, `role_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- 角色组织表
DROP TABLE IF EXISTS `rel_user_organization`;
CREATE TABLE `rel_user_organization`
(
    `id`      bigint(20)  NOT NULL AUTO_INCREMENT,
    `org_id`  bigint(20)  NOT NULL,
    `user_id` bigint(20)  NOT NULL,
    `role`    smallint(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `idx_org_user` (`org_id`, `user_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- 角色表
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `org_id`      bigint(20)   NOT NULL,
    `name`        varchar(100) NOT NULL,
    `description` varchar(255) DEFAULT NULL,
    `create_by`   bigint(20)   DEFAULT NULL,
    `create_time` datetime     DEFAULT NULL,
    `update_by`   bigint(20)   DEFAULT NULL,
    `update_time` datetime     DEFAULT NULL,
    `avatar`      varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_orgid` (`org_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='权限表';

-- 数据源表
DROP TABLE IF EXISTS `source`;
CREATE TABLE `source`
(
    `id`             bigint(20)   NOT NULL AUTO_INCREMENT,
    `name`           varchar(255) NOT NULL,
    `description`    varchar(255) DEFAULT NULL,
    `config`         text         NOT NULL,
    `type`           varchar(10)  NOT NULL,
    `project_id`     bigint(20)   NOT NULL,
    `create_by`      bigint(20)   DEFAULT NULL,
    `create_time`    datetime     DEFAULT NULL,
    `update_by`      bigint(20)   DEFAULT NULL,
    `update_time`    datetime     DEFAULT NULL,
    `parent_id`      bigint(20)   DEFAULT NULL,
    `full_parent_id` varchar(255) DEFAULT NULL,
    `is_folder`      tinyint(1)   DEFAULT NULL,
    `index`          int(5)       DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_project_id` (`project_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- 点赞表
DROP TABLE IF EXISTS `star`;
CREATE TABLE `star`
(
    `id`        bigint(20)  NOT NULL AUTO_INCREMENT,
    `target`    varchar(20) NOT NULL,
    `target_id` bigint(20)  NOT NULL,
    `user_id`   bigint(20)  NOT NULL,
    `star_time` datetime    NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_target_id` (`target_id`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `email`       varchar(255) NOT NULL,
    `username`    varchar(255) NOT NULL,
    `password`    varchar(255) NOT NULL,
    `admin`       tinyint(1)   NOT NULL,
    `active`      tinyint(1)            DEFAULT NULL,
    `name`        varchar(255)          DEFAULT NULL,
    `description` varchar(255)          DEFAULT NULL,
    `department`  varchar(255)          DEFAULT NULL,
    `avatar`      varchar(255)          DEFAULT NULL,
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   bigint(20)   NOT NULL DEFAULT '0',
    `update_time` timestamp    NOT NULL DEFAULT '1970-01-01 08:00:01',
    `update_by`   bigint(20)   NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- 视图view表
DROP TABLE IF EXISTS `view`;
CREATE TABLE `view`
(
    `id`             bigint(20)   NOT NULL AUTO_INCREMENT,
    `name`           varchar(255) NOT NULL,
    `description`    varchar(255) DEFAULT NULL,
    `project_id`     bigint(20)   NOT NULL,
    `source_id`      bigint(20)   NOT NULL,
    `sql`            text,
    `model`          text,
    `variable`       text,
    `config`         text,
    `create_by`      bigint(20)   DEFAULT NULL,
    `create_time`    datetime     DEFAULT NULL,
    `update_by`      bigint(20)   DEFAULT NULL,
    `update_time`    datetime     DEFAULT NULL,
    `parent_id`      bigint(20)   DEFAULT NULL,
    `full_parent_id` varchar(255) DEFAULT NULL,
    `is_folder`      tinyint(1)   DEFAULT NULL,
    `index`          int(5)       DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_project_id` (`project_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- widget表
DROP TABLE IF EXISTS `widget`;
CREATE TABLE `widget`
(
    `id`             bigint(20)   NOT NULL AUTO_INCREMENT,
    `name`           varchar(255) NOT NULL,
    `description`    varchar(255) DEFAULT NULL,
    `view_id`        bigint(20), -- 兼容SQL节点作为Source，需要支持null
    `project_id`     bigint(20)   NOT NULL,
    `type`           bigint(20)   NOT NULL,
    `publish`        tinyint(1)   NOT NULL,
    `config`         longtext     NOT NULL,
    `create_by`      bigint(20)   DEFAULT NULL,
    `create_time`    datetime     DEFAULT NULL,
    `update_by`      bigint(20)   DEFAULT NULL,
    `update_time`    datetime     DEFAULT NULL,
    `parent_id`      bigint(20)   DEFAULT NULL,
    `full_parent_id` varchar(255) DEFAULT NULL,
    `is_folder`      tinyint(1)   DEFAULT NULL,
    `index`          int(5)       DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_project_id` (`project_id`) USING BTREE,
    KEY `idx_view_id` (`view_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- display画布上widget角色权限表
DROP TABLE IF EXISTS `rel_role_display_slide_widget`;
CREATE TABLE `rel_role_display_slide_widget`
(
    `role_id`                     bigint(20) NOT NULL,
    `mem_display_slide_widget_id` bigint(20) NOT NULL,
    `visible`                     tinyint(1) NOT NULL DEFAULT '0',
    `create_by`                   bigint(20)          DEFAULT NULL,
    `create_time`                 datetime            DEFAULT NULL,
    `update_by`                   bigint(20)          DEFAULT NULL,
    `update_time`                 datetime            DEFAULT NULL,
    PRIMARY KEY (`role_id`, `mem_display_slide_widget_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- dashboard上widget角色权限表
DROP TABLE IF EXISTS `rel_role_dashboard_widget`;
CREATE TABLE `rel_role_dashboard_widget`
(
    `role_id`                 bigint(20) NOT NULL,
    `mem_dashboard_widget_id` bigint(20) NOT NULL,
    `visible`                 tinyint(1) NOT NULL DEFAULT '0',
    `create_by`               bigint(20)          DEFAULT NULL,
    `create_time`             datetime            DEFAULT NULL,
    `update_by`               bigint(20)          DEFAULT NULL,
    `update_time`             datetime            DEFAULT NULL,
    PRIMARY KEY (`role_id`, `mem_dashboard_widget_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 访问和操作记录统计表
DROP TABLE IF EXISTS `davinci_statistic_visitor_operation`;
CREATE TABLE `davinci_statistic_visitor_operation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `action` varchar(255) DEFAULT NULL COMMENT 'login/visit/initial/sync/search/linkage/drill/download/print',
  `org_id` bigint(20) DEFAULT NULL,
  `project_id` bigint(20) DEFAULT NULL,
  `project_name` varchar(255) DEFAULT NULL,
  `viz_type` varchar(255) DEFAULT NULL COMMENT 'dashboard/display',
  `viz_id` bigint(20) DEFAULT NULL,
  `viz_name` varchar(255) DEFAULT NULL,
  `sub_viz_id` bigint(20) DEFAULT NULL,
  `sub_viz_name` varchar(255) DEFAULT NULL,
  `widget_id` bigint(20) DEFAULT NULL,
  `widget_name` varchar(255) DEFAULT NULL,
  `variables` varchar(500) DEFAULT NULL,
  `filters` varchar(500) DEFAULT NULL,
  `groups` varchar(500) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 客户端终端信息统计表
DROP TABLE IF EXISTS `davinci_statistic_terminal`;
CREATE TABLE `davinci_statistic_terminal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `browser_name` varchar(255) DEFAULT NULL,
  `browser_version` varchar(255) DEFAULT NULL,
  `engine_name` varchar(255) DEFAULT NULL,
  `engine_version` varchar(255) DEFAULT NULL,
  `os_name` varchar(255) DEFAULT NULL,
  `os_version` varchar(255) DEFAULT NULL,
  `device_model` varchar(255) DEFAULT NULL,
  `device_type` varchar(255) DEFAULT NULL,
  `device_vendor` varchar(255) DEFAULT NULL,
  `cpu_architecture` varchar(255) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 停留时间统计表
DROP TABLE IF EXISTS `davinci_statistic_duration`;
CREATE TABLE `davinci_statistic_duration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `start_time` timestamp NULL DEFAULT NULL,
  `end_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 分享和下载记录表
DROP TABLE IF EXISTS `share_download_record`;
CREATE TABLE `share_download_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(50) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `path` varchar(255) DEFAULT NULL,
  `status` smallint(1) NOT NULL,
  `create_time` datetime NOT NULL,
  `last_download_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- alter table widget modify  view_id bigint null;

SET FOREIGN_KEY_CHECKS = 1;

DELETE FROM source;
INSERT INTO `source` (
    id,name,description,config,type,project_id,create_by,create_time,update_by,update_time,parent_id,full_parent_id,is_folder,`index`)
VALUES (
    1,'hiveDataSource','','{"parameters":"","password":"","url":"test","username":"hiveDataSource-token"}','hive',-1,null,null,null,null,null,null,null,null);
