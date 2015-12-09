#!/usr/bin/env bash

mvn package -DskipTests;

APP=$(find target/ -name "*fat.jar" 2> /dev/null)

java -jar "$APP"
