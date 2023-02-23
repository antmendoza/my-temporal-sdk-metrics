package com.antmendoza.temporal;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;

import java.util.Date;
import java.util.concurrent.TimeUnit;


public class WorkerSsl {

    static final String TASK_QUEUE = "MyTaskQueue";

    public static void main(String[] args) throws Exception {


        // Create SSL enabled client by passing SslContext, created by SimpleSslContextBuilder.
        SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();
        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                // Add metrics scope to workflow service stub options
                                .setMetricsScope(ScopeBuilder.getScope())
                                .setSslContext(sslContextBuilderProvider.getSslContext())
                                .setTarget(sslContextBuilderProvider.getTargetEndpoint())
                                .build());


        // Now setup and start workflow worker, which uses SSL enabled gRPC service to communicate with
        // backend.
        // client that can be used to start and signal workflows.
        WorkflowClient client =
                WorkflowClient.newInstance(
                        service, WorkflowClientOptions.newBuilder()
                                .setNamespace(sslContextBuilderProvider.getNamespace())
                                .build());


//        System.out.println(">>> " + client.getWorkflowServiceStubs().healthCheck().getStatus());
        // worker factory that can be used to create workers for specific task queues
        WorkerFactory factory = WorkerFactory.newInstance(client, WorkerFactoryOptions.newBuilder().setWorkflowCacheSize(2000).build());

        //       for (int i = 0; i <= 2; i++) {
        createWorker(factory, TASK_QUEUE);
        //       }
//

        // timeouts, retry & heartbeat impact
        factory.start();
    }

    private static void createWorker(WorkerFactory factory, String taskQueue) {
        // Worker that listens on a task queue and hosts both workflow and activity implementations.
        Worker worker = factory.newWorker(taskQueue, WorkerOptions.newBuilder()
                .setMaxConcurrentWorkflowTaskPollers(1)
                //.setMaxConcurrentLocalActivityExecutionSize(1)
                .build());

        worker.registerWorkflowImplementationTypes(HelloActivity.GreetingWorkflowImpl.class
                //,
                // HelloActivity2.GreetingWorkflowImpl2.class,
                //        HelloActivity3.GreetingWorkflowImpl3.class
        );
        worker.registerActivitiesImplementations(new HelloActivity.GreetingActivitiesImpl());
    }
}
