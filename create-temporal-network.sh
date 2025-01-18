#!/bin/sh

docker network rm temporal-network

docker network create -d bridge  temporal-network
