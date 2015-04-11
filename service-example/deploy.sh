#!/bin/bash
set -e

#globals
export BUILD_TARGET=target
export APP=bootiful-docker-api-client


function build_docker_image(){
  curdir=`dirname $0`
  target=$curdir/$1
  app=$3
  user=$2
  echo $curdir $target $app $user
  mvn -DskipTests=true clean install
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
  ltc scale $app 1

  ltc list
  ltc status $app
}


build_docker_image $BUILD_TARGET starbuxman $APP
deploy_to_lattice starbuxman $APP
