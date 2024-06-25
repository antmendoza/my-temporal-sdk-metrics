# About this project

[Main readme](../README.md)


## Configuration

See [temporal.properties](./src/main/resources/temporal.properties) file.

## Start environment

- `1-start-grafana_prometheus.sh`

  Navigate to the [dashboard](http://localhost:3000/d/whtBuu0Vkddd/sdk-metrics?orgId=1)

- `2-stop-workers.sh`


## Build the docker image

- `3-build-project.sh`

## Create workflows, it will create one workflow every 100ms

- `5-create-backlog.sh`

## Start workers to drain the backlog

- `6-start-workers.sh`

- Open the script ^^ to change workers configuration