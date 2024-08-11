/*
 *  Copyright (c) 2020 Temporal Technologies, Inc. All Rights Reserved
 *
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package com.antmendoza.temporal;

import com.antmendoza.temporal.config.SslContextBuilderProvider;
import com.antmendoza.temporal.workflow.MyWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static com.antmendoza.temporal.WorkerSsl.TASK_QUEUE;

public class Starter {


    public static void main(String[] args) throws InterruptedException {

        SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();


//        final int port = 8079;
//        Scope metricsScope = new ScopeBuilder().create(port, ImmutableMap.of(
//                "client",
//                "ClientSsl_" + port)
//        );

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                // .setMetricsScope(metricsScope)
                                .setSslContext(sslContextBuilderProvider.getSslContext())
                                .setTarget(sslContextBuilderProvider.getTargetEndpoint())
                                .build());


        WorkflowClientOptions clientOptions =
                WorkflowClientOptions.newBuilder()
                        .setNamespace(sslContextBuilderProvider.getNamespace())
                        .build();
        WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);


        final int millisSleep = 500;


        final AtomicInteger input = new AtomicInteger();
        while (true) {
            CompletableFuture.runAsync(() -> {
                try {
                    WorkflowOptions workflowOptions =
                            WorkflowOptions.newBuilder()
                                    .setTaskQueue(TASK_QUEUE)
                                    .build();


                    MyWorkflow workflow = client.newWorkflowStub(MyWorkflow.class, workflowOptions);
                    WorkflowClient.start(workflow::run, ""+ input.getAndIncrement());
                    System.out.println("Starting workflow...with after = "+ millisSleep+" ms");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            try {
                Thread.sleep(millisSleep);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

    }


}
