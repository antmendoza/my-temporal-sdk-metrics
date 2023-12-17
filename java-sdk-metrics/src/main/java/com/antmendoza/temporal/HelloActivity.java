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

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;


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


        private final GreetingActivities3 activities =
                Workflow.newActivityStub(
                        GreetingActivities3.class,
                        ActivityOptions.newBuilder()
                                .setTaskQueue(WorkerSsl.TASK_QUEUE)
                                .setStartToCloseTimeout(
                                        Duration.ofSeconds(10)
                                )
                                .setRetryOptions(RetryOptions.newBuilder()
                                        .setBackoffCoefficient(1)
                                        .build())
                                //.setScheduleToStartTimeout(Duration.ofSeconds(2))
                                .build());
        private String name;


        public GreetingWorkflowImpl() {
        }

        @Override
        public String getGreeting(String name) {


            final List<Promise<String>> results = new ArrayList<>();


            results.add(Async.function(() -> activities.sleepForSeconds(1)));
            results.add(Async.function(() -> activities.sleepForSeconds(1)));
            results.add(Async.function(() -> activities.sleepForSeconds(1)));
            results.add(Async.function(() -> activities.sleepForSeconds(1)));
            results.add(Async.function(() -> activities.sleepForSeconds(1)));
            results.add(Async.function(() -> activities.sleepForSeconds(1)));
            results.add(Async.function(() -> activities.sleepForSeconds(1)));
            results.add(Async.function(() -> activities.sleepForSeconds(1)));
            results.add(Async.function(() -> activities.sleepForSeconds(1)));
            results.add(Async.function(() -> activities.sleepForSeconds(1)));
            results.add(Async.function(() -> activities.sleepForSeconds(1)));
            results.add(Async.function(() -> activities.sleepForSeconds(1)));

            Promise.allOf(results).get();


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


        private static final Logger log = LoggerFactory.getLogger("-");
        private static final Map<String, Integer> map = new HashMap<>();
        int activity = 0;

        @Override
        public String sleepForSeconds(int seconds) {


            Date date = new Date();
            String second_ = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
            int counter = map.get(second_) == null ? 1 : map.get(second_) + 1;
            map.put(second_, counter);

            log.info("Adding metric " + second_ + " : " + counter);

            Activity.getExecutionContext().getMetricsScope().counter("custom_activity_retries").inc(1);

            activity++;



            if(Activity.getExecutionContext().getInfo().getAttempt() < 5)
            {
          //      throw new RuntimeException("fake...");
            }

            try {
                Thread.sleep(seconds *  500L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (activity % 2 == 0) {
                 throw new RuntimeException("fake failure");
            }


            return null;
        }

        public int getRandomNumber(int min, int max) {
            return (int) ((Math.random() * (max - min)) + min);
        }

    }


}
