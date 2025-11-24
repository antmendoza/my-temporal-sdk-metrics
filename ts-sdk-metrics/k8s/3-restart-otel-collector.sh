#!/bin/bash


kubectl apply -f otel-collector-configmap.yaml
kubectl apply -f otel-collector-deployment.yaml

kubectl -n temporal-metrics rollout restart deploy/otel-collector

kubectl -n temporal-metrics get pods -w
