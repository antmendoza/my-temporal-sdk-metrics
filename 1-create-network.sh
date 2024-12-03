#!/bin/sh

docker network rm temporal_network

docker network create -d bridge  temporal_network
