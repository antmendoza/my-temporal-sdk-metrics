package com.antmendoza.temporal;

import com.antmendoza.temporal.config.FromEnv;
import com.antmendoza.temporal.config.ScopeBuilder;
import com.antmendoza.temporal.config.SslContextBuilderProvider;
import com.antmendoza.temporal.workflow.WorkflowHelloActivity;
import com.uber.m3.tally.RootScopeBuilder;
import com.uber.m3.tally.Scope;
import com.uber.m3.tally.StatsReporter;
import com.uber.m3.util.ImmutableMap;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.reporter.MicrometerClientStatsReporter;
import io.temporal.opentracing.OpenTracingClientInterceptor;
import io.temporal.opentracing.OpenTracingWorkerInterceptor;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;

import static com.antmendoza.temporal.OpenTelemetryConfig.initTracer;


public class WorkerSsl {

    public static final String TASK_QUEUE = "MyTaskQueue_";

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
                // Add metrics scope to workflow service stub options
                //or it is a better option to have the rate limit on workflow code itself?
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
                //.setStickyQueueScheduleToStartTimeout()
                //.setMaxConcurrentActivityTaskPollers()
                .setMaxConcurrentActivityTaskPollers(FromEnv.getConcurrentActivityPollers())
                .setMaxConcurrentWorkflowTaskPollers(FromEnv.getConcurrentWorkflowPollers())
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
