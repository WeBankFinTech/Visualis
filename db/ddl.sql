CREATE TABLE `visualis_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `scope` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `params` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- visualis使用这个表
DROP TABLE IF EXISTS `visualis_user`;
CREATE TABLE `visualis_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `admin` tinyint(1) DEFAULT NULL COMMENT 'If it is an administrator',
  `active` tinyint(1) DEFAULT NULL COMMENT 'If it is active',
  `name` varchar(255) DEFAULT NULL COMMENT 'User name',
  `description` varchar(255) DEFAULT NULL,
  `department` varchar(255) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL COMMENT 'Path of the avator',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint(20) DEFAULT '0',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint(20) DEFAULT '0',
  `is_first_login` bit(1) DEFAULT NULL COMMENT 'If it is the first time to log in',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `visualis_project`;
CREATE TABLE `visualis_project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `description` text COLLATE utf8_bin,
  `pic` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL COMMENT 'Organization ID',
  `user_id` bigint(20) DEFAULT NULL,
  `star_num` int(11) DEFAULT '0',
  `username` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `create_by` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL,
  `update_by` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT '修改人',
  `visibility` bit(1) DEFAULT NULL,
  `is_transfer` bit(1) DEFAULT NULL COMMENT 'Reserved word',
  `initial_org_id` bigint(20) DEFAULT NULL,
  `isArchive` bit(1) DEFAULT b'0' COMMENT 'If it is archived',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=313 DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;


DROP TABLE IF EXISTS `visualis_preview_result`;
CREATE TABLE `visualis_preview_result` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `execId` varchar(128) COLLATE utf8_bin NOT NULL UNIQUE,
  `name` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `status` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `description` varchar(200) COLLATE utf8_bin DEFAULT NULL, -- [组件名称]_[组件ID]：display_57
  `result` longblob DEFAULT NULL,
  `create_by` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `isArchive` bit(1) DEFAULT b'0' COMMENT 'If it is archived',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;