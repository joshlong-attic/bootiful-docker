#!/bin/sh
set -e

export BUILD_TARGET=build
export APP=bootiful-docker
export CURRENT_DIR=`dirname $0`

function clean(){
  target=$1
  rm -rf $target
  mkdir -p $target
}

function build(){
  target=$1
  app=$2
  spring jar $target/service.jar $CURRENT_DIR/service.groovy
  cp run.sh $target
  docker build -t $app .
  docker tag -f $app starbuxman/$app
  docker push starbuxman/$app
}

#clean $BUILD_TARGET
#build $BUILD_TARGET $APP

alias ltc=$HOME/bin/ltc

ltc rm $APP
ltc create $APP starbuxman/$APP -- /run.sh
