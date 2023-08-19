package com.antmendoza.temporal.sinch;

import com.antmendoza.temporal.ScopeBuilder;
import com.antmendoza.temporal.SslContextBuilderProvider;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;


public class ServiceFactory {

    private static final boolean ssl = false;


    private static SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();


    public static WorkflowServiceStubs createService() {

        if (ssl) {
            WorkflowServiceStubs service =
                    WorkflowServiceStubs.newServiceStubs(
                            WorkflowServiceStubsOptions.newBuilder()
                                    // Add metrics scope to workflow service stub options
                                    .setMetricsScope(ScopeBuilder.getScope())
                                    .setSslContext(sslContextBuilderProvider.getSslContext())
                                    .setTarget(sslContextBuilderProvider.getTargetEndpoint())
                                    .build());
            return service;

        }


        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                // Add metrics scope to workflow service stub options
                                .setMetricsScope(ScopeBuilder.getScope())
                                .build());
        return service;


    }



    public static String getNamespace(){

        if (ssl) {
            return sslContextBuilderProvider.getNamespace();
        }

        return "default";
    }
}