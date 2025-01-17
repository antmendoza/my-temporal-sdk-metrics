### Steps to run this sample:

See [start-env.md](../start-env.md)


- Start temporal server in localhost https://docs.temporal.io/cli


```bash
go mod tidy
```


- Start the client

```bash
go run starter/main.go
```
- Start the worker

```bash
go run worker/main.go
```
