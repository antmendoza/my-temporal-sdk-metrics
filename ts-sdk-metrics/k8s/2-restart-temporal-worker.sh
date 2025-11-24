#!/bin/bash

kubectl -n temporal-metrics rollout restart deploy/temporal-worker

kubectl -n temporal-metrics get pods -w
