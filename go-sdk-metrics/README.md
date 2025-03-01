### Steps to run this sample:

See [start-env.md](../start-env.md)

- Install dependencies


```bash
go mod tidy
```


```bash
cd opentelemetry-collector-contrib
docker-compose down -v
docker-compose up 
```

http://localhostock:9091



- Start the client

```bash

## command with variable to enable telemetry 
export ENABLE_TELEMETRY=true
go run starter/main.go
```


- Start the worker

```bash
## command with variable to enable telemetry 
export ENABLE_TELEMETRY=true
go run worker/main.go
```





