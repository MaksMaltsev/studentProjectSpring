create table users (
  id varchar(36) NOT NULL,
  last_name varchar(100) DEFAULT NULL,
  login varchar(100) DEFAULT NULL,
  name varchar(100) DEFAULT NULL,
  password varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
