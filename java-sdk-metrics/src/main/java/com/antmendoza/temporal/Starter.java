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

import com.antmendoza.temporal.config.ScopeBuilder;
import com.antmendoza.temporal.config.SslContextBuilderProvider;
import com.antmendoza.temporal.workflow.IGreetingWorkflow;
import com.uber.m3.tally.Scope;
import com.uber.m3.util.ImmutableMap;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import java.util.concurrent.CompletableFuture;

import static com.antmendoza.temporal.WorkerSsl.TASK_QUEUE;

public class Starter {


    public static void main(String[] args) throws InterruptedException {

        SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();


        final int port = 8079;
        Scope metricsScope = new ScopeBuilder().create(port, ImmutableMap.of(
                "client",
                "ClientSsl_" + port)
        );

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                .setMetricsScope(metricsScope)
                                .setSslContext(sslContextBuilderProvider.getSslContext())
                                .setTarget(sslContextBuilderProvider.getTargetEndpoint())
                                .build());


        WorkflowClientOptions clientOptions =
                WorkflowClientOptions.newBuilder()
                        .setNamespace(sslContextBuilderProvider.getNamespace())
                        .build();
        WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);


//        Stream<WorkflowExecutionMetadata> result = client.listExecutions("CloseTime < '2022-06-08T16:46:34-08:00'");

        if (false) {

            final DescribeWorkflowExecutionRequest build = DescribeWorkflowExecutionRequest.newBuilder()
                    .setNamespace(sslContextBuilderProvider.getNamespace())
                    .setExecution(WorkflowExecution.newBuilder().setWorkflowId("test").build()).build();
            try {
                client.getWorkflowServiceStubs().blockingStub().describeWorkflowExecution(build);

            } catch (Exception e) {

            }

            Thread.sleep(5000);

        }

        while (true) {

            CompletableFuture.runAsync(() -> {

                WorkflowOptions workflowOptions =
                        WorkflowOptions.newBuilder()
                                //.setWorkflowId("localhost.test.1"+a)
                                //.setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build())
                                .setTaskQueue(TASK_QUEUE)
                                .build();


                IGreetingWorkflow workflow = client.newWorkflowStub(IGreetingWorkflow.class, workflowOptions);
                WorkflowClient.start(workflow::getGreeting, "input");
                System.out.println("Starting workflow... ");
                WorkflowStub untyped = WorkflowStub.fromTyped(workflow);

            });


            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

    }


    private static boolean isaBoolean() {
        return true;
    }


}
