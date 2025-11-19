package com.temporal.workflow_one_activity_not_retry;

import com.temporal.config.FromEnv;
import io.temporal.activity.*;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;


public class WorkflowHelloActivityDontRetry {




    @ActivityInterface
    public interface MyActivities {

        // Define your activity method which can be called during workflow execution
        @ActivityMethod
        String sleep_time(int ms);

    }


    // Define the workflow implementation which implements our getGreeting workflow method.
    public static class MyWorkflowDontRetryImpl implements MyWorkflowDontRetry {


        final int starToClose = Integer.parseInt(FromEnv.getActivityLatencyMs());
        private final Logger logger = Workflow.getLogger(MyWorkflowDontRetry.class.getName());



        private final MyActivities activities = Workflow.newActivityStub(
                MyActivities.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(
                                //setting to a very large value for demo purpose.
                                Duration.ofMillis(starToClose + 1000_000)
                        )
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setMaximumAttempts(1)
                                .build())

                        //.setScheduleToStartTimeout(Duration.ofSeconds(2))
                        .build());



        public String run(String name) {


            activities.sleep_time(200);



            return "done";

        }




    }


    public static class MyActivitiesImpl implements MyActivities {



        @Override
        public String sleep_time(int ms) {

            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return "";
        }


    }



}
