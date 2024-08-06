#!/bin/sh

cd ../grafana

docker compose down --remove-orphans

cd ../java-sdk-metrics


docker compose down --remove-orphans

docker network rm temporal_network
