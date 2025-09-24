package com.temporal;

import com.temporal.config.FromEnv;
import com.temporal.config.ScopeBuilder;
import com.temporal.config.SslContextBuilderProvider;
import com.temporal.workflow.ChildMyWorkflow1Impl;
import com.temporal.workflow.WorkflowHelloActivity;
import com.uber.m3.tally.Scope;
import com.uber.m3.util.ImmutableMap;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.opentracing.OpenTracingClientInterceptor;
import io.temporal.opentracing.OpenTracingWorkerInterceptor;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;
import com.temporal.grpc.GetSystemInfoLatencyInterceptor;
import java.util.List;

import static com.temporal.WorkerSsl.TASK_QUEUE;


public class Worker_1 {


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



        final WorkflowServiceStubsOptions.Builder builder = WorkflowServiceStubsOptions.newBuilder()
                .setMetricsScope(metricsScope)
                .setTarget(sslContextBuilderProvider.properties.getTemporalWorkerTargetEndpoint())
                //.setGrpcClientInterceptors(List.of(new GetSystemInfoLatencyInterceptor()))
                ;

        if(sslContextBuilderProvider.getSslContext() != null) {
            builder.setSslContext(sslContextBuilderProvider.getSslContext());
        }


        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        builder
                                .build());


        WorkflowClient client =
                WorkflowClient.newInstance(
                        service, WorkflowClientOptions.newBuilder()
                                .setNamespace(sslContextBuilderProvider.properties.getTemporalNamespace())
                                .setInterceptors(
                                        new OpenTracingClientInterceptor(
//                                                //OpenTracingOptions.newBuilder().setTracer(tracer).build()
                                        )

                                )
                                .build());


        WorkerFactoryOptions build = WorkerFactoryOptions.newBuilder()


                .setWorkflowCacheSize(FromEnv.getCacheSize())
                .setMaxWorkflowThreadCount(FromEnv.getMaxWorkflowThreadCount())
//                .setUsingVirtualWorkflowThreads(true)


                .setWorkerInterceptors(
                        new OpenTracingWorkerInterceptor(
                        //OpenTracingOptions.newBuilder().setTracer(tracer).build()
                        )
                )
                .build();
        WorkerFactory factory = WorkerFactory.newInstance(client, build);


        WorkerOptions build1 = WorkerOptions.newBuilder()
//                .setMaxTaskQueueActivitiesPerSecond(actions)
                .setMaxConcurrentActivityExecutionSize(20)
 //               .setMaxConcurrentWorkflowTaskExecutionSize(FromEnv.getConcurrentWorkflowExecutionSize())
                //.setStickyQueueScheduleToStartTimeout()
                //.setMaxConcurrentActivityTaskPollers()
                .setMaxConcurrentActivityTaskPollers(10)
                .setMaxConcurrentWorkflowTaskPollers(10)
                //.setMaxConcurrentWorkflowTaskPollers()
                .setDisableEagerExecution(FromEnv.getDisableEagerDispatch())
                .build();

        Worker worker = factory.newWorker(TASK_QUEUE, build1);


        worker.registerWorkflowImplementationTypes(WorkflowHelloActivity.MyWorkflowImpl.class,
                ChildMyWorkflow1Impl.class
        );
        worker.registerActivitiesImplementations(new WorkflowHelloActivity.MyActivitiesImpl());
        factory.start();
    }

}
