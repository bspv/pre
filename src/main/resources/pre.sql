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


CREATE TABLE `hotel_detail`
(
    `data_id`    int(10) unsigned NOT NULL AUTO_INCREMENT,
    `name`       varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `card_no`    varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `descriot`   varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `ctf_tp`     varchar(50) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `ctf_id`     varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `gender`     varchar(50) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `birthday`   varchar(50) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `address`    varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `zip`        varchar(50) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `dirty`      varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `district_1` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `district_2` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `district_3` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `district_4` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `district_5` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `district_6` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `first_num`  varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `last_num`   varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `duty`       varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `mobile`     varchar(50) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `tel`        varchar(50) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `fax`        varchar(50) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `email`      varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `nation`     varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `taste`      varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `education`  varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `company`    varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `c_tel`      varchar(50) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `c_address`  varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `c_zip`      varchar(50) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `family`     varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `version`    varchar(50) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `id`         varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    PRIMARY KEY (`data_id`),
    KEY          `idx_ctf_id` (`ctf_id`(20)),
    KEY          `idx_mobile` (`mobile`(15)),
    KEY          `idx_name` (`name`(20)),
    KEY          `idx_card_no` (`card_no`(20))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;