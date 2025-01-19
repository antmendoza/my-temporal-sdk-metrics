#!/bin/sh


# git pull --recurse-submodules

cd ./temporal_server

docker compose down --remove-orphans

docker compose -f docker-compose.yml -f ../docker-compose-temporal_server-override.yml up --remove-orphans


