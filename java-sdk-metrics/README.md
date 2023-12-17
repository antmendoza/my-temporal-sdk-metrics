# Example to connect with temporal cloud

It has two main components: 
- the starter that starts the workflow [GreetingWorkflow](./src/main/java/com/antmendoza/temporal/HelloActivity.java)
- the worker that connects to the server and pulls and execute tasks.

## Configuration
Configuration resides in the [temporal.properties](./src/main/resources/temporal.properties) file.


## Start the worker

`mvn compile exec:java -Dexec.mainClass="com.antmendoza.temporal.WorkerSsl"`


docker stop $(docker ps -q --filter ancestor=my-synk-worker )


mvn clean install

docker build .  -t my-synk-worker


docker stop $(docker ps -q --filter ancestor=my-synk-worker )



export ACTIONS_PER_SECOND=180
export PQL_PORT=8071
docker run -d -p $PQL_PORT:$PQL_PORT  -e PQL_PORT=$PQL_PORT -e ACTIONS_PER_SECOND=$ACTIONS_PER_SECOND my-synk-worker


export PQL_PORT=8072
docker run -d -p $PQL_PORT:$PQL_PORT  -e PQL_PORT=$PQL_PORT -e ACTIONS_PER_SECOND=$ACTIONS_PER_SECOND my-synk-worker


export PQL_PORT=8073
docker run -d -p $PQL_PORT:$PQL_PORT  -e PQL_PORT=$PQL_PORT -e ACTIONS_PER_SECOND=$ACTIONS_PER_SECOND my-synk-worker


export PQL_PORT=8074
docker run -d -p $PQL_PORT:$PQL_PORT  -e PQL_PORT=$PQL_PORT -e ACTIONS_PER_SECOND=$ACTIONS_PER_SECOND my-synk-worker


export PQL_PORT=8075
docker run -d -p $PQL_PORT:$PQL_PORT  -e PQL_PORT=$PQL_PORT -e ACTIONS_PER_SECOND=$ACTIONS_PER_SECOND my-synk-worker

export PQL_PORT=8076
docker run -d -p $PQL_PORT:$PQL_PORT  -e PQL_PORT=$PQL_PORT -e ACTIONS_PER_SECOND=$ACTIONS_PER_SECOND my-synk-worker

export PQL_PORT=8077
docker run -d -p $PQL_PORT:$PQL_PORT  -e PQL_PORT=$PQL_PORT -e ACTIONS_PER_SECOND=$ACTIONS_PER_SECOND my-synk-worker

export PQL_PORT=8078
docker run -d -p $PQL_PORT:$PQL_PORT  -e PQL_PORT=$PQL_PORT -e ACTIONS_PER_SECOND=$ACTIONS_PER_SECOND my-synk-worker

export PQL_PORT=8079
docker run -d -p $PQL_PORT:$PQL_PORT  -e PQL_PORT=$PQL_PORT -e ACTIONS_PER_SECOND=$ACTIONS_PER_SECOND my-synk-worker




sum by (namespace) (rate(custom_activity_retries_total{namespace=~"$Namespace"}[$__rate_interval]))



docker service create --name xxx --env PR=amqp://rabbitmq:xxxx --detach testj

docker container stop $(docker container ls -q --filter name=my-synk-worker)


```
17:21 start 9 workers with maxTaskQueueActivitiesPerSecond = 180

17:29 stop all workers
17:29 start 9 workers with maxTaskQueueActivitiesPerSecond = 100


17:45 stop all workers
17:45 start 9 workers with maxTaskQueueActivitiesPerSecond = 250




17:57 stop all workers
17:57 start 9 workers with maxTaskQueueActivitiesPerSecond = 180



```



## Mongo

```
docker run  \
-p 27017:27017 \
--name data \
-v mongo-data:/data/db \
-e MONGODB_INITDB_ROOT_USERNAME=user \
-e MONGODB_INITDB_ROOT_PASSWORD=12345 \
mongo:latest
```
