package com.temporal;

import com.temporal.config.FromEnv;
import com.temporal.config.ScopeBuilder;
import com.temporal.config.SslContextBuilderProvider;
import com.temporal.query_can_workflow.MyDataConverter;
import com.uber.m3.tally.Scope;
import com.uber.m3.util.ImmutableMap;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

public class Client {



    public  WorkflowClient getWorkflowClient() {
        SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();


        final int port = Integer.parseInt(FromEnv.getWorkerPort());
        Scope metricsScope = new ScopeBuilder().create(port, ImmutableMap.of(
                "client",
                "ClientSsl_" + port)
        );

        final WorkflowServiceStubsOptions.Builder builder = WorkflowServiceStubsOptions.newBuilder()
                .setMetricsScope(metricsScope)
//                                .setRpcTimeout(Duration.ofMillis(167))
                .setTarget(sslContextBuilderProvider.properties.getTemporalStarterTargetEndpoint());

        if (sslContextBuilderProvider.getSslContext() != null) {
            builder.setSslContext(sslContextBuilderProvider.getSslContext());
        }

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        builder
                                .build());

        WorkflowClientOptions clientOptions =
                WorkflowClientOptions.newBuilder()
                        .setNamespace(sslContextBuilderProvider.properties.getTemporalNamespace())
                        .setDataConverter(new MyDataConverter())
                        .build();
        WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);
        return client;
    }
}
