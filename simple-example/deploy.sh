#!/bin/sh
set -e

#globals
export BUILD_TARGET=build
export APP=bootiful-docker
export CURRENT_DIR=`dirname $0`

alias ltc=$HOME/bin/ltc

function clean(){
  target=$1
  rm -rf $target
  mkdir -p $target
}

function build_docker_image(){
  target=$1
  app=$2
  spring jar $target/service.jar $CURRENT_DIR/service.groovy
  cp $CURRENT_DIR/run.sh $target
  docker build -t $app .
  docker tag -f $app starbuxman/$app
  docker push starbuxman/$app
}



function deploy_to_lattice(){
  app=$1

  ltc rm $APP
  ltc create $APP starbuxman/$APP -- /run.sh
  ltc scale $app 5

  ltc list
  ltc status $app

}


clean $BUILD_TARGET
build_docker_image $BUILD_TARGET $APP
deploy_to_lattice $APP
