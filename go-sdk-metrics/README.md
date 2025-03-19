### Steps to run this sample:

See [start-env.md](../start-env.md)

- Install dependencies


```bash
go mod tidy
```

#### Open telemetry

- Start the client
```bash

export ENABLE_TELEMETRY=true
go run starter/main.go
```
- 
- Start the worker

```bash

for i in {1..5} ; do
    export ENABLE_TELEMETRY=true
    go run worker/main.go
done
```



#### Tally


- Start the client
```bash

export PQL_PORT=true
go run starter/main.go
```
-
- Start the worker

```bash

for i in {1..5} ; do
    export ENABLE_TELEMETRY=true
    go run worker/main.go
done
```






