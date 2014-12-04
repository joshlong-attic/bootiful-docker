#!/bin/bash
set -e

./build.sh

docker push starbuxman/bootiful_lattice

diego-edge-cli start bootiful_lattice -i "docker:///starbuxman/bootiful_lattice" -c "/run.sh"
