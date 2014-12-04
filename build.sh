#!/bin/sh
set -e

spring jar service.jar service.groovy

target=build
rm -rf $target
mkdir -p $target

cp service.jar $target
cp run.sh $target


docker build -t bootiful-docker .
