
# About this project

[Main README](../README.md)


## Configuration

See 
- [temporal.properties](./src/main/resources/temporal.properties) file.
- [env](./.env) file.




## Start environment

- `1-create-network.sh`

- `2-start-grafana_prometheus.sh`

  Navigate to the [dashboard](http://localhost:3000/)


## Run the worker

- `3-start-java-worker.sh`
Start one worker with the given [env](./.env) variables.


## Start workflows

- `4-create-backlog.sh`


The [dashboard](http://localhost:3000/) will start showing metrics.
