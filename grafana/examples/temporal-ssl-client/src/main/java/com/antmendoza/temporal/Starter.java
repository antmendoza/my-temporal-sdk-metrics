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

import com.uber.m3.util.ImmutableMap;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class Starter {

    public static void main(String[] args) {

        SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                .setSslContext(sslContextBuilderProvider.getSslContext())
                                .setMetricsScope(ScopeBuilder.getScope(ImmutableMap.of(
                                        "starterCustomTag1",
                                        "starterCustomTag1Value",
                                        "starterCustomTag2",
                                        "starterCustomTag2Value")))
                                .setTarget(sslContextBuilderProvider.getTargetEndpoint())
                                .build());


        WorkflowClientOptions clientOptions =
                WorkflowClientOptions.newBuilder()
                        .setNamespace(sslContextBuilderProvider.getNamespace())

                        .build();
        WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);


        AtomicBoolean b = new AtomicBoolean(true);
        while (b.get()) {


            Collections.singletonList(
                    HelloActivity.GreetingWorkflow.class
                    //,HelloActivity2.GreetingWorkflow2.class
                    //,HelloActivity3.GreetingWorkflow3.class
            ).forEach(wfClass -> {

                WorkflowOptions workflowOptions =
                        WorkflowOptions.newBuilder()
                                //                 .setWorkflowId("localhost.test.1")
                                //.setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build())
                                //.setWorkflowTaskTimeout(Duration.ofSeconds(1))
                                .setTaskQueue(WorkerSsl.TASK_QUEUE)
                                .build();


                try{

                    // Create typed workflow stub
                    IGreetingWorkflow workflow = client.newWorkflowStub(wfClass, workflowOptions);

                    //WorkflowClient.start(workflow::getGreeting, "Antonio");


                    workflow.getGreeting("Antonio");


                    b.set(false);
                }catch(Exception e){}



            });

            //return;
        }


//        String greeting = workflow.getGreeting("Antonio");

        //System.out.println("Greeting: " + greeting);

        //System.exit(0);
    }


}
