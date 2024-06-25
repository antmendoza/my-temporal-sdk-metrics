#!/bin/sh

cd ../grafana

docker compose down --remove-orphans

cd ..


docker compose down --remove-orphans

docker network rm temporal_network
