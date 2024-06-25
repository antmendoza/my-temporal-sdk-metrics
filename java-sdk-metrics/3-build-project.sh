#!/bin/sh

./mvnw clean install

docker build . -t my-metric-worker-java