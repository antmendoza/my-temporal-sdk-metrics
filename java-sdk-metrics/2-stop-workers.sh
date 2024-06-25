#!/bin/sh

docker stop $(docker ps -q --filter ancestor=my-metric-worker-java )

