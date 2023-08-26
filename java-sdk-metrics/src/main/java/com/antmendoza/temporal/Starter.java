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

import io.temporal.client.*;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Starter {

    String a = "{\"a\":\"a\"}";

    public static void main(String[] args) {

        SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        WorkflowServiceStubsOptions.newBuilder()
                                //.setMetricsScope(ScopeBuilder.getScope())
                                .setSslContext(sslContextBuilderProvider.getSslContext())
                                .setTarget(sslContextBuilderProvider.getTargetEndpoint())
                                .build());


        WorkflowClientOptions clientOptions =
                WorkflowClientOptions.newBuilder()
                        .setNamespace(sslContextBuilderProvider.getNamespace())
                        .build();
        WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);


//        Stream<WorkflowExecutionMetadata> result = client.listExecutions("CloseTime < '2022-06-08T16:46:34-08:00'");



        final int[] a = {0};

        while (isaBoolean()) {


            Collections.singletonList(
                    HelloActivity.GreetingWorkflow.class
                    //,HelloActivity2.GreetingWorkflow2.class
                    //,HelloActivity3.GreetingWorkflow3.class
            ).forEach(wfClass -> {

                WorkflowOptions workflowOptions =
                        WorkflowOptions.newBuilder()
                                //.setWorkflowId("localhost.test.1"+a)
                                //.setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build())
                                .setTaskQueue(WorkerSsl.TASK_QUEUE)
                                .build();


                // Create typed workflow stub


                a[0]++;

                IGreetingWorkflow workflow = client.newWorkflowStub(wfClass, workflowOptions);
                WorkflowClient.start(workflow::getGreeting, "input");
                WorkflowStub untyped = WorkflowStub.fromTyped(workflow);
                untyped.getResultAsync( String.class).thenApply(result -> {
                    System.out.println("result " + result);
                    return result;
                });



               if(a[0] % 10 == 0){
                   try {
                       Thread.sleep(500);
                   } catch (InterruptedException e) {
                       throw new RuntimeException(e);
                   }
               }


                //workflow.getGreeting("Antonio");


            });

            //return;
        }


//        String greeting = workflow.getGreeting("Antonio");

        //System.out.println("Greeting: " + greeting);

        //System.exit(0);
    }

    private static boolean isaBoolean() {
        return true;
    }


    private static Map<String, Object> generateSearchAttributes() {
        Map<String, Object> searchAttributes = new HashMap<>();
        searchAttributes.put(
                "CustomerId",
                "keys"); // each field can also be array such as: String[] keys = {"k1", "k2"};
        return searchAttributes;
    }
}
