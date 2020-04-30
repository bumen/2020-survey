

DROP TABLE IF EXISTS `shiro_session`;
CREATE TABLE `shiro_session` (
  `sessionId` varchar(128) NOT NULL PRIMARY KEY,
  `session_data` tinyint NOT NULL DEFAULT 0
);

DROP TABLE IF EXISTS `t_user_name`;
CREATE TABLE `t_user_name` (
  `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT
);

DROP TABLE IF EXISTS `text`;
  CREATE TABLE `text` (
    `groupId` varchar(128) NOT NULL PRIMARY KEY,
    `author` varchar(64) NOT NULL,
    `announcement` varchar(64) DEFAULT '',
    `timestamp` bigint(20) NOT NULL,
  );
