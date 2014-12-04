#!/bin/sh
set -e



target=build
rm -rf $target
mkdir -p $target

spring jar $target/service.jar service.groovy
cp run.sh $target

docker build -t bootiful-docker .


project_name=bootiful-docker

docker tag $project_name starbuxman/$project_name
docker push starbuxman/$project_name
diego-edge-cli start $project_name -i "docker:///starbuxman/$project_name" -c "/run.sh"
