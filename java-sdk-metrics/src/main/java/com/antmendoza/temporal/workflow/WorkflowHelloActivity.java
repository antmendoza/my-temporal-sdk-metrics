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

package com.antmendoza.temporal.workflow;

import com.antmendoza.temporal.WorkerSsl;
import com.antmendoza.temporal.config.FromEnv;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;


public class WorkflowHelloActivity {

    @ActivityInterface
    public interface MyActivities {

        // Define your activity method which can be called during workflow execution
        @ActivityMethod
        String sleep();

        @ActivityMethod
        String dontSleep();
    }


    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class MyWorkflowImpl implements MyWorkflow {

        private final MyActivities activities =
                Workflow.newActivityStub(
                        MyActivities.class,
                        ActivityOptions.newBuilder()
                                .setTaskQueue(WorkerSsl.TASK_QUEUE)
                                .setStartToCloseTimeout(
                                        //setting to a very large value for demo purpose.
                                        Duration.ofMinutes(10)
                                )
                                .setRetryOptions(RetryOptions.newBuilder()
                                        .setBackoffCoefficient(1)
                                        .build())
                                //.setScheduleToStartTimeout(Duration.ofSeconds(2))
                                .build());
        private final Logger logger = Workflow.getLogger(MyWorkflowImpl.class.getName());


        public MyWorkflowImpl() {
        }

        public String run(String name) {

            activities.sleep();

            final List<Promise<String>> results = new ArrayList<>();
            results.add(Async.function(() -> activities.dontSleep()));
            results.add(Async.function(() -> activities.dontSleep()));
//            results.add(Async.function(() -> activities.sleep()));



            Workflow.sleep(3_000);
            activities.sleep();

            Promise.allOf(results).get();

            activities.sleep();

            return "done";

        }


    }

    /**
     * Simple activity implementation, that concatenates two strings.
     */
    public static class MyActivitiesImpl implements MyActivities {


        private static final Logger log = LoggerFactory.getLogger("-");
        private static final Map<String, Integer> map = new HashMap<>();
        int activity = 0;

        @Override
        public String sleep() {

            log.info("Start ******* ");

            Date date = new Date();
            String second_ = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
            int counter = map.get(second_) == null ? 1 : map.get(second_) + 1;
            map.put(second_, counter);
            log.info("Adding metric " + second_ + " : " + counter);
            Activity.getExecutionContext().getMetricsScope().counter("custom_activity_retries").inc(1);


            String sleep_activity_in_ms = FromEnv.getActivityLatency();
            int i = Integer.parseInt(sleep_activity_in_ms);
            log.info("SLEEP_ACTIVITY_IN_MS : " + i);


            try {
                Thread.sleep(i);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            activity++;
            //if(Activity.getExecutionContext().getInfo().getAttempt() < 6){

            if (activity % 2 == 0) {
                throw new RuntimeException("fake failure");
            }


            return null;
        }

        @Override
        public String dontSleep() {
            return null;
        }

    }


}
