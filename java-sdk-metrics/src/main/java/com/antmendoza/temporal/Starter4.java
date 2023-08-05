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

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Starter4 {

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


        final int[] a = {0};

        while (true) {


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
                IGreetingWorkflow workflow = client.newWorkflowStub(wfClass, workflowOptions);


                a[0]++;


                WorkflowClient.start(workflow::getGreeting, "Antonio");


//               if(a[0] % 5000 == 0){
//                   try {
//                       Thread.sleep(5000);
//                   } catch (InterruptedException e) {
//                       throw new RuntimeException(e);
//                   }
//               }


                //workflow.getGreeting("Antonio");


            });

            //return;
        }


//        String greeting = workflow.getGreeting("Antonio");

        //System.out.println("Greeting: " + greeting);

        //System.exit(0);
    }


    private static Map<String, Object> generateSearchAttributes() {
        Map<String, Object> searchAttributes = new HashMap<>();
        searchAttributes.put(
                "CustomerId",
                "keys"); // each field can also be array such as: String[] keys = {"k1", "k2"};
        return searchAttributes;
    }
}
