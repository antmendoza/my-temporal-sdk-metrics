#!/bin/sh


# git pull --recurse-submodules

cd ./temporal_server

docker compose down --remove-orphans

docker compose up --remove-orphans



