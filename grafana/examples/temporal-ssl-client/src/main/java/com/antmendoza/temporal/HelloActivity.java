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
import io.temporal.workflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;


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

            Workflow.sleep(Duration.ofMinutes(2));

            return greeting + " " + name + "!";
        }


    }

    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class GreetingWorkflowImpl implements GreetingWorkflow {


        private static int test = 1;
        private final GreetingActivities activities =
                Workflow.newActivityStub(
                        GreetingActivities.class,
                        ActivityOptions.newBuilder()
                                .setScheduleToCloseTimeout(
                                        Duration.ofSeconds(5)
                                )
                                .setStartToCloseTimeout(
                                        Duration.ofSeconds(5)
                                ).setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(1).build()).build());
        private final GreetingActivities localActivities =
                Workflow.newLocalActivityStub(
                        GreetingActivities.class,
                        LocalActivityOptions.newBuilder()
                                .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(2).build())
                                //.setCancellationType(WAIT_CANCELLATION_COMPLETED)
                                .setStartToCloseTimeout(Duration.ofSeconds(3))
                                .setScheduleToCloseTimeout(
                                        Duration.ofSeconds(3)
                                ).build());
        private String name;


        public GreetingWorkflowImpl() {
            System.out.println("Constructor....." + test++);
        }

        @Override
        public String getGreeting(String name) {



            //localActivities.composeGreeting(true, "jespin");
            Promise<String> get = Async.function(activities::composeGreeting, true, "activity_3");


//            int version = Workflow.getVersion("continueAs", Workflow.DEFAULT_VERSION, 1);
//            if (version == Workflow.DEFAULT_VERSION) {
//                Workflow.continueAsNew(name);
//            }


            get.get();

            //Workflow.continueAsNew(name);

            return "hello";

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
                Thread.sleep(10000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//
//
            ActivityInfo info = Activity.getExecutionContext().getInfo();
            int attempt = info.getAttempt();
//            System.out.println("info activity " + attempt + "  ");
            boolean b = retry && attempt < 2;
//            System.out.println("REEXECUTE activity " + b + "  ");
            if (b) {
               //throw new RuntimeException("EXCEPTION... " + activity);
            }
//            } else{
//
//                //throw ApplicationFailure.newNonRetryableFailure("my non retryable type", "my non retryable");
//            }

            return activity;
        }
    }

}
