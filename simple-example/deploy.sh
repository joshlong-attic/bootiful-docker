#!/bin/bash
set -e

#globals
export BUILD_TARGET=build
export APP=bootiful-docker

function clean(){
  curdir=`dirname $0`
  echo "curdir is $curdir."
  target=$curdir/$1
  rm -rf $target
  mkdir -p $target
  echo "removed and restored $target"
}

function build_docker_image(){
  curdir=`dirname $0`
  target=$curdir/$1
  app=$3
  user=$2
  spring jar $target/service.jar $curdir/service.groovy
  cp $curdir/run.sh $target
  docker build -t $app $curdir
  docker tag -f $app $user/$app
  docker push $user/$app
}



function deploy_to_lattice(){
  export PATH=$PATH:$HOME/bin/ltc
  alias ltc=$HOME/bin/ltc
  app=$2
  user=$1

  ltc rm $app
  ltc create $app $user/$app -- /run.sh
  ltc scale $app 5

  ltc list
  ltc status $app
}


#clean $BUILD_TARGET
#build_docker_image $BUILD_TARGET starbuxman $APP
deploy_to_lattice starbuxman $APP

## TODO
ltc create --run-as-root $APP-redis redis
ltc create --run-as-root $APP-mongo mongo
