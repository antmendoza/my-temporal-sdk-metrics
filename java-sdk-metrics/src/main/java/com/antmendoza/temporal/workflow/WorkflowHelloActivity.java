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
import io.temporal.activity.*;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.workflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

import static io.temporal.activity.ActivityCancellationType.WAIT_CANCELLATION_COMPLETED;


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

        final int starToClose = Integer.parseInt(FromEnv.getActivityLatency());
        private final Logger logger = Workflow.getLogger(MyWorkflowImpl.class.getName());
        private final MyActivities activities = Workflow.newActivityStub(
                MyActivities.class,
                ActivityOptions.newBuilder()
                        .setTaskQueue(WorkerSsl.TASK_QUEUE)
                        .setStartToCloseTimeout(
                                //setting to a very large value for demo purpose.
                                Duration.ofMillis(starToClose + 1000)
                        )
                        .setCancellationType(WAIT_CANCELLATION_COMPLETED)
                        .setHeartbeatTimeout(Duration.ofMillis(starToClose / 2))
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setBackoffCoefficient(1)
                                .build())
                        //.setScheduleToStartTimeout(Duration.ofSeconds(2))
                        .build());

        private final MyActivities localActivity = Workflow.newLocalActivityStub(
                MyActivities.class,
                LocalActivityOptions.newBuilder()
                        .setStartToCloseTimeout(
                                //setting to a very large value for demo purpose.
                                Duration.ofMillis(starToClose + 1000)
                        )
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setBackoffCoefficient(1)
                                .build())
                        //.setScheduleToStartTimeout(Duration.ofSeconds(2))
                        .build());


        public MyWorkflowImpl() {
        }

        public String run(String name) {

            final List<Promise<String>> results = new ArrayList<>();

            CancellationScope cancellationScope = Workflow.newCancellationScope(() -> {


                results.add(Async.function(activities::sleep));
                results.add(Async.function(localActivity::sleep));
                results.add(Async.function(localActivity::dontSleep));
                results.add(Async.function(activities::sleep));
                results.add(Async.function(activities::dontSleep));


                try {

                    Promise.allOf(results).get();
                } catch (ActivityFailure e) {
                    for (Promise<String> result : results) {
                        if (result.getFailure() != null) {
                            System.out.println("Activity failed , cause: " + result.getFailure().getCause());
                        } else {
                            result.get();
                        }
                    }

                }

            });


            Workflow.newTimer(Duration.ofSeconds(3))
                    .thenApply(
                            result -> {
                                // Cancel our activity, note activity has to heartbeat to receive cancellation
                                System.out.println("Cancelling scope as timer fired");
                                if (Integer.parseInt(name) % 5 == 0 && true) {
                                    System.out.println("Cancelling scope: ");
                                    cancellationScope.cancel();
                                }
                                return null;
                            });


            cancellationScope.run();



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

            activity++;
            //if(Activity.getExecutionContext().getInfo().getAttempt() < 6){

            if (activity % 2 == 0) {
                throw new RuntimeException("fake failure");
            }


            final int iteration = i / 1000;
            for (int j = 0; j < iteration; j++) {


                Activity.getExecutionContext().heartbeat("iteration number  " + j + " of " + iteration);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }


            return null;
        }

        @Override
        public String dontSleep() {
            return null;
        }

    }


}
