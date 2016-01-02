#!/usr/bin/env bash

mvn package -DskipTests;

APP=$(find target/ -name "*fat.jar" 2> /dev/null)

java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5055 \
	-jar "$APP" 
