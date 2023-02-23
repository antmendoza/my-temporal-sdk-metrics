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

import io.temporal.activity.*;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;


/**
 * Sample Temporal Workflow Definition that executes a single Activity.
 */
public class HelloActivity {

    // Define the task queue name
    static final String TASK_QUEUE = "HelloActivityTaskQueue";

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloActivityWorkflow";


    @WorkflowInterface
    public interface GreetingWorkflow extends IGreetingWorkflow {



        @SignalMethod
        void waitForName(String name);
    }


    @ActivityInterface
    public interface GreetingActivities {

        // Define your activity method which can be called during workflow execution
        @ActivityMethod(name = "greet2")
        String composeGreeting(boolean retry, String activity_3);
    }


    @WorkflowInterface
    public interface GreetingChild {
        @WorkflowMethod
        String composeGreeting(String greeting, String name);


    }

    public static class GreetingChildImpl implements GreetingChild {

        @Override
        public String composeGreeting(String greeting, String name) {

            Workflow.sleep(Duration.ofSeconds(2));

            return greeting + " " + name + "!";
        }


    }

    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class GreetingWorkflowImpl implements GreetingWorkflow {


        private static int test = 1;
        private String name;

        public GreetingWorkflowImpl() {
            System.out.println("Constructor....." + test++);
        }

        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder()
                                .setScheduleToCloseTimeout(
                                        Duration.ofSeconds(2)
                                )
                                .setStartToCloseTimeout(
                                        Duration.ofSeconds(2)
                                ).build());


        private final GreetingActivities localActivities =
                Workflow.newLocalActivityStub(
                        GreetingActivities.class,
                        LocalActivityOptions.newBuilder()
                                .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(2).build())
                                //.setCancellationType(WAIT_CANCELLATION_COMPLETED)
                                .setStartToCloseTimeout(Duration.ofSeconds(2)).build());


        @Override
        public String getGreeting(String name) {



            Workflow.sleep(Duration.ofMinutes(1));

            System.out.println("starting....1");

            //Workflow.upsertSearchAttributes(Map.of("dd", "ddd"));

            System.out.println("starting....2");


            //WorkflowTaskScheduled
            //WorkflowTaskStarted
            //WorkflowTaskCompleted
            //Workflow.sleep(Duration.ofSeconds(5)); //TimerStarted
            //TimerFired

            //WorkflowTaskScheduled
            //WorkflowTaskStarted
            //WorkflowTaskCompleted
            //ActivityTaskScheduled
            //String hello = activities.composeGreeting("Hello", name); //ActivityTaskStarted
            //ActivityTaskCompleted


            //for (int i = 0; i < 10; i++) {

               activities.composeGreeting(true, "activity_3");

            //}


            System.out.println("  Attemp " + Workflow.getInfo().getAttempt());

            boolean b = true;
            if (b) {

                //       throw ApplicationFailure.newFailureWithCause("message", "my exceptino", new Exception("es throwable "), 1,2,3);
            }


            Workflow.sleep(2000);


            int version = Workflow.getVersion("continueAs", Workflow.DEFAULT_VERSION, 1);
            if (version == Workflow.DEFAULT_VERSION) {
                Workflow.continueAsNew(name);

            }


            //WorkflowTaskScheduled
            //WorkflowTaskStarted
            //WorkflowTaskCompleted
            //WorkflowExecutionCompleted

            return "hello";


            //Don't want to wait the child workflow to complete/fails
            //this does not send any command to the server (same with async activities)
            //Promise<String> greeting = Async.function(child::composeGreeting, "Hello", name);

        }


        @Override
        public void waitForName(String name) {
            this.name = name;
        }
    }

    /**
     * Simple activity implementation, that concatenates two strings.
     */
    public static class GreetingActivitiesImpl implements GreetingActivities {
        private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

        @Override
        public String composeGreeting(boolean retry, String activity) {

            System.out.println("executing activity " + activity + "  ");


            try {
              //  Thread.sleep(4000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


            ActivityInfo info = Activity.getExecutionContext().getInfo();
            int attempt = info.getAttempt();
            System.out.println("info activity " + attempt + "  ");
            boolean b = retry && attempt < 2;
            System.out.println("REEXECUTE activity " + b + "  ");
            if (b) {
                //throw new RuntimeException("EXCEPTION... " + activity);
            } else{

                //throw ApplicationFailure.newNonRetryableFailure("my non retryable type", "my non retryable");
            }

            return activity;
        }
    }

}
