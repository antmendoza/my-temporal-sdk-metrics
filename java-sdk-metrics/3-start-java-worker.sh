#!/bin/sh


docker compose down --remove-orphans && docker volume prune -f

docker-compose up --build --force-recreate