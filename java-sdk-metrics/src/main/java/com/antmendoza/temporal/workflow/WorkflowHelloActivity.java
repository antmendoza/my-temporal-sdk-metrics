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
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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



            localActivity.dontSleep();
            localActivity.dontSleep();
            localActivity.dontSleep();
            try {

                activities.exception();
            } catch (Exception e) {

            }

            activities.dontSleep();
            activities.dontSleep();
            activities.dontSleep();


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
        public String exception() {

            final String s = "                {\n" +
                    "                  \"metadata\": {\n" +
                    "                    \"encoding\": \"anNvbi9wbGFpbg==\"\n" +
                    "                  },\n" +
                    "                  \"data\": {\n" +
                    "                    \"cause\": null,\n" +
                    "                    \"stackTrace\": [\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": null,\n" +
                    "                        \"moduleVersion\": null,\n" +
                    "                        \"methodName\": \"handleCspApiErrorBadRequest\",\n" +
                    "                        \"fileName\": \"TemporalUtils.java\",\n" +
                    "                        \"lineNumber\": 302,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"com.twilio.messaging.a2p.temporal.TemporalUtils\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": null,\n" +
                    "                        \"moduleVersion\": null,\n" +
                    "                        \"methodName\": \"lambda$resend2faEmail$1\",\n" +
                    "                        \"fileName\": \"TcrCspResend2FaEmailActivityImpl.java\",\n" +
                    "                        \"lineNumber\": 59,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"com.twilio.messaging.a2p.temporal.activities.tcr.csp.TcrCspResend2FaEmailActivityImpl\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": null,\n" +
                    "                        \"moduleVersion\": null,\n" +
                    "                        \"methodName\": \"fold\",\n" +
                    "                        \"fileName\": \"Resend2FaEmailResponse.java\",\n" +
                    "                        \"lineNumber\": 123,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"com.twilio.messaging.a2p.providers.kaleyra.client.csp.Brand.Resend2FaEmailResponse\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": null,\n" +
                    "                        \"moduleVersion\": null,\n" +
                    "                        \"methodName\": \"resend2faEmail\",\n" +
                    "                        \"fileName\": \"TcrCspResend2FaEmailActivityImpl.java\",\n" +
                    "                        \"lineNumber\": 51,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"com.twilio.messaging.a2p.temporal.activities.tcr.csp.TcrCspResend2FaEmailActivityImpl\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": \"java.base\",\n" +
                    "                        \"moduleVersion\": \"17.0.9\",\n" +
                    "                        \"methodName\": \"invoke0\",\n" +
                    "                        \"fileName\": \"NativeMethodAccessorImpl.java\",\n" +
                    "                        \"lineNumber\": -2,\n" +
                    "                        \"nativeMethod\": true,\n" +
                    "                        \"className\": \"jdk.internal.reflect.NativeMethodAccessorImpl\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": \"java.base\",\n" +
                    "                        \"moduleVersion\": \"17.0.9\",\n" +
                    "                        \"methodName\": \"invoke\",\n" +
                    "                        \"fileName\": \"NativeMethodAccessorImpl.java\",\n" +
                    "                        \"lineNumber\": 77,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"jdk.internal.reflect.NativeMethodAccessorImpl\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": \"java.base\",\n" +
                    "                        \"moduleVersion\": \"17.0.9\",\n" +
                    "                        \"methodName\": \"invoke\",\n" +
                    "                        \"fileName\": \"DelegatingMethodAccessorImpl.java\",\n" +
                    "                        \"lineNumber\": 43,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"jdk.internal.reflect.DelegatingMethodAccessorImpl\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": \"java.base\",\n" +
                    "                        \"moduleVersion\": \"17.0.9\",\n" +
                    "                        \"methodName\": \"invoke\",\n" +
                    "                        \"fileName\": \"Method.java\",\n" +
                    "                        \"lineNumber\": 568,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"java.lang.reflect.Method\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": null,\n" +
                    "                        \"moduleVersion\": null,\n" +
                    "                        \"methodName\": \"executeActivity\",\n" +
                    "                        \"fileName\": \"RootActivityInboundCallsInterceptor.java\",\n" +
                    "                        \"lineNumber\": 64,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"io.temporal.internal.activity.RootActivityInboundCallsInterceptor$POJOActivityInboundCallsInterceptor\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": null,\n" +
                    "                        \"moduleVersion\": null,\n" +
                    "                        \"methodName\": \"execute\",\n" +
                    "                        \"fileName\": \"RootActivityInboundCallsInterceptor.java\",\n" +
                    "                        \"lineNumber\": 43,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"io.temporal.internal.activity.RootActivityInboundCallsInterceptor\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": null,\n" +
                    "                        \"moduleVersion\": null,\n" +
                    "                        \"methodName\": \"execute\",\n" +
                    "                        \"fileName\": \"ActivityTaskExecutors.java\",\n" +
                    "                        \"lineNumber\": 107,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"io.temporal.internal.activity.ActivityTaskExecutors$BaseActivityTaskExecutor\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": null,\n" +
                    "                        \"moduleVersion\": null,\n" +
                    "                        \"methodName\": \"handle\",\n" +
                    "                        \"fileName\": \"ActivityTaskHandlerImpl.java\",\n" +
                    "                        \"lineNumber\": 124,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"io.temporal.internal.activity.ActivityTaskHandlerImpl\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": null,\n" +
                    "                        \"moduleVersion\": null,\n" +
                    "                        \"methodName\": \"handleActivity\",\n" +
                    "                        \"fileName\": \"ActivityWorker.java\",\n" +
                    "                        \"lineNumber\": 278,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"io.temporal.internal.worker.ActivityWorker$TaskHandlerImpl\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": null,\n" +
                    "                        \"moduleVersion\": null,\n" +
                    "                        \"methodName\": \"handle\",\n" +
                    "                        \"fileName\": \"ActivityWorker.java\",\n" +
                    "                        \"lineNumber\": 243,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"io.temporal.internal.worker.ActivityWorker$TaskHandlerImpl\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": null,\n" +
                    "                        \"moduleVersion\": null,\n" +
                    "                        \"methodName\": \"handle\",\n" +
                    "                        \"fileName\": \"ActivityWorker.java\",\n" +
                    "                        \"lineNumber\": 216,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"io.temporal.internal.worker.ActivityWorker$TaskHandlerImpl\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": null,\n" +
                    "                        \"moduleVersion\": null,\n" +
                    "                        \"methodName\": \"lambda$process$0\",\n" +
                    "                        \"fileName\": \"PollTaskExecutor.java\",\n" +
                    "                        \"lineNumber\": 105,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"io.temporal.internal.worker.PollTaskExecutor\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": \"java.base\",\n" +
                    "                        \"moduleVersion\": \"17.0.9\",\n" +
                    "                        \"methodName\": \"runWorker\",\n" +
                    "                        \"fileName\": \"ThreadPoolExecutor.java\",\n" +
                    "                        \"lineNumber\": 1136,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"java.util.concurrent.ThreadPoolExecutor\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": \"java.base\",\n" +
                    "                        \"moduleVersion\": \"17.0.9\",\n" +
                    "                        \"methodName\": \"run\",\n" +
                    "                        \"fileName\": \"ThreadPoolExecutor.java\",\n" +
                    "                        \"lineNumber\": 635,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"java.util.concurrent.ThreadPoolExecutor$Worker\"\n" +
                    "                      },\n" +
                    "                      {\n" +
                    "                        \"classLoaderName\": null,\n" +
                    "                        \"moduleName\": \"java.base\",\n" +
                    "                        \"moduleVersion\": \"17.0.9\",\n" +
                    "                        \"methodName\": \"run\",\n" +
                    "                        \"fileName\": \"Thread.java\",\n" +
                    "                        \"lineNumber\": 840,\n" +
                    "                        \"nativeMethod\": false,\n" +
                    "                        \"className\": \"java.lang.Thread\"\n" +
                    "                      }\n" +
                    "                    ],\n" +
                    "                    \"httpStatus\": 500,\n" +
                    "                    \"messageForCustomer\": null,\n" +
                    "                    \"tcrErrors\": [\n" +
                    "                      {\n" +
                    "                        \"translatedFailureReason\": \"2FA verification is already complete\",\n" +
                    "                        \"apiErrorCode\": \"GENERAL_ERROR\"\n" +
                    "                      }\n" +
                    "                    ],\n" +
                    "                    \"failureReason\": \"Registrar response: Fields Error: 2FA verification is already complete\",\n" +
                    "                    \"message\": \"TCR CSP error: Registrar response: Fields Error: 2FA verification is already complete\",\n" +
                    "                    \"suppressed\": [],\n" +
                    "                    \"localizedMessage\": \"TCR CSP error: Registrar response: Fields Error: 2FA verification is already complete\"\n" +
                    "                  }\n" +
                    "                }\n";

            if (Activity.getExecutionContext().getInfo().getAttempt() < 3) {

                throw ApplicationFailure.newFailureWithCause("this is a test", "MyException", new NullPointerException(), s);

            }

            throw ApplicationFailure.newNonRetryableFailureWithCause("this is a test", "MyException", new NullPointerException(), s);

        }

        @Override
        public String dontSleep() {
            return null;
        }

    }


}
