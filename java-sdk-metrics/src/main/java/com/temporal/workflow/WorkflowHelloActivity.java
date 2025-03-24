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

package com.temporal.workflow;

import com.temporal.WorkerSsl;
import com.temporal.config.FromEnv;
import io.temporal.activity.*;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.Async;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
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


        // Define your activity method which can be called during workflow execution
        @ActivityMethod
        String exception();

        @ActivityMethod
        String dontSleep();
    }


    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class MyWorkflowImpl implements MyWorkflow1 {


        final int starToClose = Integer.parseInt(FromEnv.getActivityLatencyMs());
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
//                        .setHeartbeatTimeout(Duration.ofMillis(starToClose / 2))
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setBackoffCoefficient(1)
                                .setInitialInterval(Duration.ofSeconds(5))
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

            //Create a variable with 1MB size
            name = new byte[1024 * 1024].toString();

            {
                List<Promise<String>> promises = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                    promises.add(Async.function(activities::sleep));
                }

                Promise.allOf(promises).get();
            }


            {
                List<Promise<String>> promises = new ArrayList<>();
                for (int i = 0; i < 100; i++) {

                    final int index = i;
                    final String finalName = name;
                    promises.add(Async.function(() -> {

                        final ChildMyWorkflow1 childWF = Workflow.newChildWorkflowStub(ChildMyWorkflow1.class,
                                ChildWorkflowOptions.newBuilder()
//                                    .setParentClosePolicy(ParentClosePolicy.PARENT_CLOSE_POLICY_ABANDON)
                                        .build());
                        return childWF.run(finalName + "-" + index);

                    }));

                }


                Promise.allOf(promises).get();


            }
            return "done";

        }

        @Override
        public String update() {
            return null;
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


            String sleep_activity_in_ms = FromEnv.getActivityLatencyMs();
            int i = Integer.parseInt(sleep_activity_in_ms);
            log.info("SLEEP_ACTIVITY_IN_MS : " + i);

            activity++;
            //if(Activity.getExecutionContext().getInfo().getAttempt() < 6){

            if (activity % 2 == 0) {
              //  throw new RuntimeException("fake failure");
            }


            final int iteration = i / 1000;
            for (int j = 0; j < iteration; j++) {


                Activity.getExecutionContext().heartbeat("iteration number  " + j + " of " + iteration);

                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }


            return null;
        }

        @Override
        public String exception() {


            if (Activity.getExecutionContext().getInfo().getAttempt() < 3) {

                throw ApplicationFailure.newFailureWithCause(
                        "this is a test",
                        "MyException",
                        new NullPointerException(),
                        "my details");

            }

            return null;

        }

        @Override
        public String dontSleep() {
            return null;
        }

    }


}
