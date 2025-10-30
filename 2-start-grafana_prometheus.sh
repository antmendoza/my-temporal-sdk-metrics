#!/bin/sh


cd ./grafana

docker network create -d bridge  temporal-network

docker compose down --remove-orphans

docker compose up --remove-orphans



