CREATE DATABASE IF NOT EXISTS pre CHARACTER SET = utf8mb4 COLLATE=utf8mb4_general_ci;

use pre;

CREATE TABLE IF NOT EXISTS `user`
(
    `id`              bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_name`       varchar(40) COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT '用户名',
    `password`        varchar(40) COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT '密码',
    `mobile`          varchar(20) COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT '手机号',
    `avatar_url`      varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '头像URL',
    `sex`             tinyint                                 DEFAULT NULL COMMENT '性别，0女，1男',
    `platform`        tinyint                                 DEFAULT NULL COMMENT '平台',
    `imei`            varchar(40) COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT 'IMEI号',
    `reg_time`        datetime                                DEFAULT NULL COMMENT '注册时间',
    `last_login_time` datetime                                DEFAULT NULL COMMENT '最后一次登录时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;