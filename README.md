# Temporal onboarding 


`cd temporal-ssl-client && mvn compile exec:java -Dexec.mainClass="com.antmendoza.temporal.WorkerSsl`

`mvn compile exec:java -Dexec.mainClass="com.antmendoza.temporal.WorkerSsl"`

It has two main components: 
- the starter that starts the workflow [GreetingWorkflow](./src/main/java/com/antmendoza/temporal/HelloActivity.java)
- the worker that connects to the server and pulls and execute tasks.

## Configuration
Configuration resides in the [temporal.properties](./src/main/resources/temporal.properties) file.


## Start the worker

`mvn compile exec:java -Dexec.mainClass="com.antmendoza.temporal.WorkerSsl"`