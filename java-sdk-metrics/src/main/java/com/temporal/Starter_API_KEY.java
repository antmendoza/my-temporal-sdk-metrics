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

package com.temporal;

import com.temporal.workflow.MyWorkflow1;
import io.grpc.*;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static com.temporal.WorkerSsl.TASK_QUEUE;

public class Starter_API_KEY {


    public static void main(String[] args) throws InterruptedException {



        final ClientInterceptor interceptr = new ClientInterceptor() {
            @Override
            public <ReqT, RespT> io.grpc.ClientCall<ReqT, RespT> interceptCall(
                    io.grpc.MethodDescriptor<ReqT, RespT> method,
                    io.grpc.CallOptions callOptions,
                    io.grpc.Channel next) {



                //Print api key value



                return next.newCall(method, callOptions);
            }
        };
        final Collection<ClientInterceptor> interceptor = List.of(interceptr, new HeaderLoggingInterceptor());

        final String key = "key";


        WorkflowServiceStubsOptions.Builder stubOptions =
                WorkflowServiceStubsOptions.newBuilder()
                        .addApiKey(() -> key)
                        .setEnableHttps(true)
                        .setGrpcClientInterceptors(interceptor)
                        .setTarget("us-east-1.aws.api.temporal.io:7233");

        WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(stubOptions.build());


        WorkflowClientOptions clientOptions =
                WorkflowClientOptions.newBuilder()
                        .setNamespace("antonio-api-key-2.a2dd6")
//                        .setDataConverter(dataConverter)
                        .build();
        WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);


        final int millisSleep = 1000;

        final AtomicInteger input = new AtomicInteger();
        while (input.get() < 2) {


            final int andIncrement = input.getAndIncrement();
            CompletableFuture.runAsync(() -> {
                final String workflowId = andIncrement + "-" + Math.random();
                try {

                    WorkflowOptions workflowOptions =
                            WorkflowOptions.newBuilder()
                                    .setTaskQueue(TASK_QUEUE)
                                    .setWorkflowId(workflowId)
                                    .build();


                    MyWorkflow1 workflow = client.newWorkflowStub(MyWorkflow1.class, workflowOptions);
                    System.out.println("Starting workflow...with after = " + millisSleep + " ms");
                    System.out.println(workflowId + "init " + new Date());
                    WorkflowExecution execution = WorkflowClient.start(workflow::run, "" + andIncrement);
                    System.out.println(workflowId + "end " + new Date());


                } catch (Exception e) {

                    System.out.println("Failed workflowId = " + workflowId);
                }
            });

            Thread.sleep(millisSleep);


        }




    }



    public static class HeaderLoggingInterceptor implements ClientInterceptor {

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {


            return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                    next.newCall(method, callOptions)) {

                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {



                    // Log request headers
                    System.out.println(new Date() + "Request Headers: " + headers);
                    super.start(
                            new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                                    responseListener) {
                                @Override
                                public void onHeaders(Metadata headers) {
                                    // Log response headers
                                    System.out.println(new Date() + "Response Headers: " + headers);
                                    super.onHeaders(headers);
                                }
                            },
                            headers);
                }
            };
        }
    }


}
