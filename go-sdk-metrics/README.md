### Steps to run this sample:

See [start-env.md](../start-env.md)

- Install dependencies


```bash
go mod tidy
```

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





