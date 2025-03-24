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

import static com.temporal.OpenTelemetryConfig.initTracer;


public class WorkerSsl {

    public static final String TASK_QUEUE = "MyTaskQueue_2";

    public static void main(String[] args) throws Exception {


        initTracer();


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

        final WorkflowServiceStubsOptions.Builder builder = WorkflowServiceStubsOptions.newBuilder()
                .setMetricsScope(metricsScope)
                .setTarget(sslContextBuilderProvider.properties.getTemporalWorkerTargetEndpoint());

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
                .setWorkerInterceptors(
                        new OpenTracingWorkerInterceptor(
                        //OpenTracingOptions.newBuilder().setTracer(tracer).build()
                        )
                )
                .build();
        WorkerFactory factory = WorkerFactory.newInstance(client, build);


        WorkerOptions build1 = WorkerOptions.newBuilder()
                .setMaxTaskQueueActivitiesPerSecond(actions)
                .setMaxConcurrentActivityExecutionSize(FromEnv.getConcurrentActivityExecutionSize())
                .setMaxConcurrentWorkflowTaskExecutionSize(FromEnv.getConcurrentWorkflowExecutionSize())
                .setMaxConcurrentActivityTaskPollers(FromEnv.getConcurrentActivityPollers())
                .setMaxConcurrentWorkflowTaskPollers(FromEnv.getConcurrentWorkflowPollers())
                .setDisableEagerExecution(FromEnv.getDisableEagerDispatch())
                .build();

        Worker worker = factory.newWorker(TASK_QUEUE, build1);

        worker.registerWorkflowImplementationTypes(WorkflowHelloActivity.MyWorkflowImpl.class,
                ChildMyWorkflow1Impl.class
        );
        worker.registerActivitiesImplementations(new WorkflowHelloActivity.MyActivitiesImpl());
        factory.start();

        System.getenv().forEach((k, v) -> {
            System.out.println(k + ":" + v);
        });


    }

}
