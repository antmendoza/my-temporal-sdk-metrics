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

package com.antmendoza.temporal.codec;

import com.antmendoza.temporal.SslContextBuilderProvider;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.converter.CodecDataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * Hello World Temporal workflow that executes a single activity. Requires a local instance the
 * Temporal service to be running.
 */
public class EncryptedPayloadsActivity {

    static final String TASK_QUEUE = "EncryptedPayloads";
    static SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();

    public static void main(String[] args) {
        new EncryptedPayloadsActivity().createWorkflow(new Customer("1234", "firstname1 surname1 lastSurname"));
    }

    public  void createWorkflow(Customer customer) {


        // gRPC stubs wrapper that talks to the local docker instance of temporal service.
        WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(WorkflowServiceStubsOptions.newBuilder()
                .setSslContext(sslContextBuilderProvider.getSslContext())
                .setTarget(sslContextBuilderProvider.getTargetEndpoint())
                .build());
        // client that can be used to start and signal workflows
        WorkflowClient client =
                WorkflowClient.newInstance(
                        service,
                        WorkflowClientOptions.newBuilder()
                                .setNamespace(sslContextBuilderProvider.getNamespace())
                                .setDataConverter(
                                        new CodecDataConverter(
                                                DefaultDataConverter.newDefaultInstance(),
                                                Collections.singletonList(new CryptCodec())))
                                .build());

        // worker factory that can be used to create workers for specific task queues
        WorkerFactory factory = WorkerFactory.newInstance(client);
        // Worker that listens on a task queue and hosts both workflow and activity implementations.
        Worker worker = factory.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
        worker.registerActivitiesImplementations(new GreetingActivitiesImpl());
        // Start listening to the workflow and activity task queues.
        factory.start();




        // Start a workflow execution. Usually this is done from another program.
        // Uses task queue from the GreetingWorkflow @WorkflowMethod annotation.
        Object customerId;
        String workflowId = customer.wfId();
        Map<String, String> searchAttributes = Map.of(
                "CustomerId", customer.customerId(),
                "CustomerName", customer.customerName(),
                "MyCustomWid", workflowId
        );
        GreetingWorkflow workflow =
                client.newWorkflowStub(
                        GreetingWorkflow.class,
                        WorkflowOptions.newBuilder()
//                                .setSearchAttributes(searchAttributes)
                                .setWorkflowId(workflowId)
                                .setTaskQueue(TASK_QUEUE)
                                .build());
        // Execute a workflow waiting for it to complete. See {@link
        // io.temporal.samples.hello.HelloSignal}
        // for an example of starting workflow without waiting synchronously for its result.
        String greeting = workflow.getGreeting("My secret text");
        System.out.println(greeting);
        // System.exit(0);
    }

    /**
     * Workflow interface has to have at least one method annotated with @WorkflowMethod.
     */
    @WorkflowInterface
    public interface GreetingWorkflow {
        @WorkflowMethod
        String getGreeting(String name);
    }

    /**
     * Activity interface is just a POJI.
     */
    @ActivityInterface
    public interface GreetingActivities {
        @ActivityMethod
        String composeGreeting(String greeting, String name);

        @ActivityMethod
        void composeGreetingVoid(String greeting);

        @ActivityMethod
        String composeGreetingNull(String greeting);

        @ActivityMethod
        String composeGreetingEmptyStr(String greeting);
    }



    /**
     * GreetingWorkflow implementation that calls GreetingsActivities#composeGreeting.
     */
    public static class GreetingWorkflowImpl implements GreetingWorkflow {

        private String signal;
        /**
         * Activity stub implements activity interface and proxies calls to it to Temporal activity
         * invocations. Because activities are reentrant, only a single stub can be used for multiple
         * activity invocations.
         */
        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

        @Override
        public String getGreeting(String name) {
            // This is a blocking call that returns only after the activity has completed.

            String hello = activities.composeGreeting("Hello", name);

            return hello;
        }
    }

    static class GreetingActivitiesImpl implements GreetingActivities {
        @Override
        public String composeGreeting(String greeting, String name) {
            return greeting + " " + name + "!";
        }

        @Override
        public void composeGreetingVoid(String greeting) {
        }

        @Override
        public String composeGreetingNull(String greeting) {
            return null;
        }

        @Override
        public String composeGreetingEmptyStr(String greeting) {
            return "";
        }
    }
}
