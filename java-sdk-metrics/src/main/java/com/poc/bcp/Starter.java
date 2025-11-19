package com.poc.bcp;

import io.temporal.activity.ActivityInterface;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.time.Duration;
import java.util.List;

import static com.temporal.WorkerSsl.TASK_QUEUE;

public class Starter {

    public static Duration workflowTaskTimeout = Duration.ofMillis(1_000);

    public static void main(String[] args) {


        final WorkflowServiceStubsOptions.Builder builder = WorkflowServiceStubsOptions.newBuilder()
                .setGrpcClientInterceptors(List.of(
                        new BCPFailureInjectionInterceptor()
                ));


        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        builder
                                .build());

        WorkflowClientOptions clientOptions =
                WorkflowClientOptions.newBuilder()
                        .build();
        WorkflowClient client1 = WorkflowClient.newInstance(service, clientOptions);

        WorkerFactory factory = WorkerFactory.newInstance(client1);

        Worker worker = factory.newWorker(TASK_QUEUE, WorkerOptions.newBuilder().
                setStickyQueueScheduleToStartTimeout(workflowTaskTimeout)
                .build());

        worker.registerWorkflowImplementationTypes(HelloWorkflowImpl.class);

        worker.registerActivitiesImplementations(new HelloActivitiesImpl());

        factory.start();


        WorkflowOptions workflowOptions =
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId(Math.random() + "")
                        .setWorkflowTaskTimeout(workflowTaskTimeout)
                        .build();


        HelloWorkflow workflow = client1.newWorkflowStub(HelloWorkflow.class, workflowOptions);


        workflow.hello("hello");


        System.exit(0);

    }


    @ActivityInterface
    public interface HelloActivity {

        String hello_sleep(int ms);


    }

    public static class HelloActivitiesImpl implements HelloActivity {

        @Override
        public String hello_sleep(int ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Slept for " + ms + " ms";
        }
    }


    @WorkflowInterface
    public interface HelloWorkflow {

        @WorkflowMethod
        String hello(String name);

    }


    public static class HelloWorkflowImpl implements HelloWorkflow {


        HelloActivity activity = Workflow.newActivityStub(HelloActivity.class,
                io.temporal.activity.ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(java.time.Duration.ofSeconds(30_000))
                        .build()
        );


        @Override
        public String hello(String name) {

            activity.hello_sleep(200);

            activity.hello_sleep(200);

            activity.hello_sleep(200);

            activity.hello_sleep(200);

            activity.hello_sleep(200);

            return "";
        }
    }

}
