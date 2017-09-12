#!/bin/bash
mysql -P 3306 -h 127.0.0.1 -u root -proot test < $DAVINCI_HOME/bin/davinci.sql
