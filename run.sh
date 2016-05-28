#!/usr/bin/env bash

APP=$(find target/ -name "*fat.jar" 2> /dev/null)

java -jar "$APP" 
