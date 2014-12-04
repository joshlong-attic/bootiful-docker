#!/bin/bash
set -e

./build.sh

project_name=bootiful-docker

docker tag $project_name starbuxman/$project_name
docker push starbuxman/$project_name
diego-edge-cli start $project_name -i "docker:///starbuxman/$project_name" -c "/run.sh"
