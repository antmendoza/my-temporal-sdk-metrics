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

package com.temporal.query_can_workflow;

import com.temporal.workflow.Client;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

import static com.temporal.WorkerSsl.TASK_QUEUE;


public class Starter {


    public static void main(String[] args) throws InterruptedException {

        WorkflowClient client = new Client().getWorkflowClient();


        CompletableFuture.runAsync(() -> {
            final String workflowIdentifier = MyWorkflowRunForever.class.getSimpleName();
            executeAndQueryWorkflow(client, workflowIdentifier);
        });


        CompletableFuture.runAsync(() -> {

            try {

            final String workflowIdentifier = MyWorkflowCAN.class.getSimpleName();
            executeAndQueryWorkflow(client, workflowIdentifier);
            }catch (Exception e) {
                e.printStackTrace();

            }

        });


        //wait forever
        Thread.currentThread().join();
    }


    private static void executeAndQueryWorkflow(final WorkflowClient client, final String workflowIdentifier) {

        for (int i = 0; i < 5; i++) {

            final String workflowId = workflowIdentifier + i;


            try {

                WorkflowStub workflow = client.newUntypedWorkflowStub(workflowIdentifier,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(workflowId)
                                .setTaskQueue(TASK_QUEUE)
                                .setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_TERMINATE_EXISTING)
                                .build());

                System.out.println("Starting workflow " + workflowId);
                workflow.start(workflowIdentifier);
                System.out.println("Started workflow " + workflowId);

            } catch (Exception e) {
                e.printStackTrace();
            }
            CompletableFuture.runAsync(() -> {

                final int queryEvery = 500;

                while (true) {

                    try {

                        Date date1 = new Date();
                        client.newUntypedWorkflowStub(workflowId).query("status", Void.class);
                        Date date2 = new Date();
                        long difference = date2.getTime() - date1.getTime();


                        int threshold = 500;
                        if (difference > threshold) {

                            DescribeWorkflowExecutionResponse executionDetails = client.getWorkflowServiceStubs().blockingStub()
                                    .describeWorkflowExecution(DescribeWorkflowExecutionRequest.newBuilder()
                                            .setNamespace(client.getOptions().getNamespace())
                                            .setExecution(WorkflowExecution.newBuilder().setWorkflowId(workflowId).build())
                                            .build());


                            System.out.println("\n" + new Date() + " :: " + workflowId +
                                    "\n    -------------" +
                                    "\n    Printing if query takes > " + threshold + " ms; querying every " + queryEvery + " ms" +
                                    "\n            > Query took [ " + difference + " ms]" +
                                    "\n            > HistoryLength [ " + executionDetails.getWorkflowExecutionInfo().getHistoryLength() + " events]"
                            );
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(queryEvery);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
}
