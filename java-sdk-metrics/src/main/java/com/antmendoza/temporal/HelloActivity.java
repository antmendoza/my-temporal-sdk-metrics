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

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


/**
 * Sample Temporal Workflow Definition that executes a single Activity.
 */
public class HelloActivity {

    // Define our workflow unique id
    static final String WORKFLOW_ID = "HelloActivityWorkflow";


    @WorkflowInterface
    public interface GreetingWorkflow extends IGreetingWorkflow {


        @SignalMethod
        void waitForName(String name);
    }





    @ActivityInterface
    public interface GreetingActivities3 {

        // Define your activity method which can be called during workflow execution
        @ActivityMethod
        String sleepForSeconds(int seconds);
    }


    @WorkflowInterface
    public interface GreetingChild {
        @WorkflowMethod
        String composeGreeting(String greeting, String name);


    }


    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class GreetingWorkflowImpl implements GreetingWorkflow {


        private static int test = 1;
        private final GreetingActivities3 activities =
                Workflow.newActivityStub(
                        GreetingActivities3.class,
                        ActivityOptions.newBuilder()
                                .setTaskQueue(WorkerSsl.TASK_QUEUE)
                                .setStartToCloseTimeout(
                                        Duration.ofSeconds(10)

                                )
                                //.setScheduleToStartTimeout(Duration.ofSeconds(2))
                                .build());


        private final GreetingActivities3 localActivities =
                Workflow.newLocalActivityStub(
                        GreetingActivities3.class,
                        LocalActivityOptions.newBuilder()
                                //.setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(2).build())
                                //.setCancellationType(WAIT_CANCELLATION_COMPLETED)
                                .setStartToCloseTimeout(Duration.ofSeconds(10)).build());
        private String name;


        public GreetingWorkflowImpl() {
        }

        @Override
        public String getGreeting(String name) {


            List<Promise<String>> result = new ArrayList<>();

            result.add(Async.function(activities::sleepForSeconds, 3));
            result.add(Async.function(activities::sleepForSeconds, 3));


            result.add(Async.function(localActivities::sleepForSeconds, 1));
            result.add(Async.function(localActivities::sleepForSeconds, 1));


            Promise.allOf(result).get();

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
    public static class GreetingActivitiesImpl implements GreetingActivities3 {
        private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);


        @Override
        public String sleepForSeconds(int seconds) {

            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }

}
