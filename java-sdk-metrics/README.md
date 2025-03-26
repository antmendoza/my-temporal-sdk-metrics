
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
