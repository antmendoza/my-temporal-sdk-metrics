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

package com.antmendoza.temporal.parent_child;

import com.antmendoza.temporal.SslContextBuilderProvider;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.RetryOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import java.time.Duration;

class Starter {

    public static void main(String[] args) {

        SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                .setSslContext(sslContextBuilderProvider.getSslContext())
                                .setTarget(sslContextBuilderProvider.getTargetEndpoint())
                                .build());


        WorkflowClientOptions clientOptions =
                WorkflowClientOptions.newBuilder()
                        .setNamespace(sslContextBuilderProvider.getNamespace())
                        .build();
        WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);


        WorkflowOptions workflowOptions =
                WorkflowOptions.newBuilder()
                        //                 .setWorkflowId("localhost.test.1")
                        .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build())
                        .setWorkflowTaskTimeout(Duration.ofMinutes(30))
                        .setTaskQueue(Worker.TASK_QUEUE)
                        .build();


        // Create typed workflow stub
        Parent1 parent1 = client.newWorkflowStub(Parent1.class, workflowOptions);

        WorkflowClient.start(parent1::startWorkflow, "---");

        // Create typed workflow stub
        Parent2 parent2 = client.newWorkflowStub(Parent2.class, workflowOptions);

        WorkflowClient.start(parent2::startWorkflow, "---");

    }


}
