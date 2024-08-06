package com.antmendoza.temporal;

import com.antmendoza.temporal.config.FromEnv;
import com.antmendoza.temporal.config.ScopeBuilder;
import com.antmendoza.temporal.config.SslContextBuilderProvider;
import com.antmendoza.temporal.workflow.WorkflowHelloActivity;
import com.uber.m3.tally.Scope;
import com.uber.m3.util.ImmutableMap;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;


public class WorkerSsl {

    public static final String TASK_QUEUE = "MyTaskQueue3";

    public static void main(String[] args) throws Exception {


        // Create SSL enabled client by passing SslContext, created by SimpleSslContextBuilder.
        SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();


        System.out.println("PORT : " + FromEnv.getWorkerPort());
        int port = Integer.parseInt(FromEnv.getWorkerPort());
        System.out.println("PORT : " + port);

        String MAX_TASKQUEUE_ACTIVITIES_PER_SECOND = FromEnv.getActivitiesPerSecondPerTQ();
        System.out.println("Actions per second" + MAX_TASKQUEUE_ACTIVITIES_PER_SECOND);
        int actions = Integer.parseInt(MAX_TASKQUEUE_ACTIVITIES_PER_SECOND);
        System.out.println("Actions per second" + actions);


        Scope metricsScope = new ScopeBuilder().create(port, ImmutableMap.of(
                "worker",
                "WorkerSsl_" + port)
        );


        metricsScope.gauge("MAX_TASKQUEUE_ACTIVITIES_PER_SECOND").update(actions);
        metricsScope.gauge("CONCURRENT_ACTIVITY_EXECUTION_SIZE").update(FromEnv.getConcurrentActivityExecutionSize());
        metricsScope.gauge("CACHE_SIZE").update(FromEnv.getCacheSize());
        metricsScope.gauge("MAX_WORKFLOW_THREAD_COUNT").update(FromEnv.getMaxWorkflowThreadCount());
        metricsScope.gauge("CONCURRENT_WORKFLOW_EXECUTION_SIZE").update(FromEnv.getConcurrentWorkflowExecutionSize());

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                // Add metrics scope to workflow service stub options
                                //or it is a better option to have the rate limit on workflow code itself?
                                .setMetricsScope(metricsScope)
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
        WorkerFactoryOptions build = WorkerFactoryOptions.newBuilder()
                .setWorkflowCacheSize(FromEnv.getCacheSize())
                .setMaxWorkflowThreadCount(FromEnv.getMaxWorkflowThreadCount())
                .build();
        WorkerFactory factory = WorkerFactory.newInstance(client, build);

        //       for (int i = 0; i <= 2; i++) {
        // Worker that listens on a task queue and hosts both workflow and activslity implementations.


        WorkerOptions build1 = WorkerOptions.newBuilder()
                .setMaxTaskQueueActivitiesPerSecond(actions)
                .setMaxConcurrentActivityExecutionSize(FromEnv.getConcurrentActivityExecutionSize())
                .setMaxConcurrentWorkflowTaskExecutionSize(FromEnv.getConcurrentWorkflowExecutionSize())
                //.setStickyQueueScheduleToStartTimeout()
                //.setMaxConcurrentActivityTaskPollers()
                //.setMaxConcurrentActivityTaskPollers(10)
                //.setMaxConcurrentWorkflowTaskPollers(10)
                //.setMaxConcurrentWorkflowTaskPollers()
                .setDisableEagerExecution(FromEnv.getDisableEagerDispatch())
                .build();

        Worker worker = factory.newWorker(TASK_QUEUE, build1);

        worker.registerWorkflowImplementationTypes(WorkflowHelloActivity.MyWorkflowImpl.class
                //,
                // HelloActivity2.GreetingWorkflowImpl2.class,
                //        HelloActivity3.GreetingWorkflowImpl3.class
        );
        worker.registerActivitiesImplementations(new WorkflowHelloActivity.MyActivitiesImpl());
        //       }
//

        // timeouts, retry & heartbeat impact
        factory.start();
    }

}
