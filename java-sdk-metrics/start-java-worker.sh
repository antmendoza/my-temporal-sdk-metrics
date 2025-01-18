#!/bin/sh

./mvnw clean install

docker compose down --remove-orphans

docker-compose up --build --force-recreate