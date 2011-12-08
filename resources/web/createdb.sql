DROP TABLE IF EXISTS `bbssh_org`.`bbssh_usage_tracking`;
CREATE TABLE  `bbssh_org`.`bbssh_usage_tracking` (
  `pin_hash` varchar(64) NOT NULL,
  `add_ts` datetime NOT NULL,
  `ip_addr` varchar(64) NOT NULL,
  `bb_device_name` varchar(45) NOT NULL,
  `bbssh_ver` varchar(12) NOT NULL,
  `bb_software_ver` varchar(14) NOT NULL,
  `bb_platform_ver` varchar(14) NOT NULL,
  `update_ts` datetime NOT NULL,
  `update_count` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`pin_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `bbssh_org`.`download_requests`;
CREATE TABLE  `bbssh_org`.`download_requests` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `request_ts` datetime NOT NULL,
  `ip_addr` varchar(64) NOT NULL,
  `os_version` varchar(16) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2461 DEFAULT CHARSET=latin1;
