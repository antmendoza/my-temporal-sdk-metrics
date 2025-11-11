
# Temporal Java SDK Metrics

See [start-env.md](../start-env.md)

## Configuration

See 
- [temporal.properties](./src/main/resources/temporal.properties) file.


## Run the worker
``` bash
ps aux | grep com.temporal.Worker_1

pkill -f "com.temporal.Worker_1"

for i in {8061..8061}; do export PQL_PORT=$i; ./mvnw compile exec:java -Dexec.mainClass="com.temporal.Worker_1" & done
```


## Start workflows



``` bash
ps aux | grep com.temporal.workflow.Starter

pkill -f "com.temporal.workflow.Starter"

for i in {8071..8071}; do export PQL_PORT=$i; ./mvnw compile exec:java -Dexec.mainClass="com.temporal.workflow.Starter" & done
```



The Java dashboard in [dashboard](http://localhost:3000/) will start showing data.

## Inject gRPC Failures (retry simulation)

Use the built-in client interceptor to force retryable gRPC errors from the Java SDK to the Temporal server. Configure via env vars when starting the worker or starter (examples below):

- INJECT_GRPC_FAILURES: set to `true` to enable.
- INJECT_GRPC_METHOD_SUBSTR: comma-separated substrings to match gRPC methods (default `*`). Examples: `StartWorkflowExecution,PollWorkflowTaskQueue,PollActivityTaskQueue,GetSystemInfo`.
- INJECT_GRPC_FAIL_PERCENT: integer 0–100 to fail that percent of matching calls.
- INJECT_GRPC_FAIL_FIRST_N: fail the first N matching calls per method (once per method if set to 1).

Failures are returned as `UNAVAILABLE` which the SDK retries with backoff.

Examples:

```bash
# Fail the first StartWorkflowExecution once per process (SDK will retry)
export INJECT_GRPC_FAILURES=true
export INJECT_GRPC_METHOD_SUBSTR=StartWorkflowExecution
export INJECT_GRPC_FAIL_FIRST_N=1

# Or: randomly fail ~30% of PollWorkflowTaskQueue calls
export INJECT_GRPC_FAILURES=true
export INJECT_GRPC_METHOD_SUBSTR=PollWorkflowTaskQueue
export INJECT_GRPC_FAIL_PERCENT=30
```

You can combine both `INJECT_GRPC_FAIL_PERCENT` and `INJECT_GRPC_FAIL_FIRST_N`. If both are set, a call fails if either condition triggers.
