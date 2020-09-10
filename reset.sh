#!/bin/bash

docker stop jiab-0.1
docker rm jiab-0.1

make clean 

rm -rf /u01/docker_volumes/jiab01/*

make build 

docker run -d --privileged --name jiab-0.1 -p 8080:8080 -p 50000:50000 -v /u01/docker_volumes/jiab01:/var/jenkins_home jiab:0.1 
