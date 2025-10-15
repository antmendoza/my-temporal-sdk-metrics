package com.temporal.worker_versioning;

import com.temporal.WorkerSsl;
import com.temporal.config.FromEnv;
import io.temporal.activity.*;
import io.temporal.common.RetryOptions;
import io.temporal.common.VersioningBehavior;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

import static io.temporal.activity.ActivityCancellationType.WAIT_CANCELLATION_COMPLETED;


public class WorkflowHelloActivity {




    @ActivityInterface
    public interface MyActivitiesVersioning {

        @ActivityMethod
        String sleep(String input);

        @ActivityMethod
        String sleep_time(int ms);


        @ActivityMethod
        String exception();

        @ActivityMethod
        String dontSleep();
    }


    public static class MyWorkflowImplVersioning implements MyWorkflowVersioning {


        final int starToClose = Integer.parseInt(FromEnv.getActivityLatencyMs());


        private final MyActivitiesVersioning activities = Workflow.newActivityStub(
                MyActivitiesVersioning.class,
                ActivityOptions.newBuilder()
                        .setTaskQueue(WorkerSsl.TASK_QUEUE)
                        .setStartToCloseTimeout(
                                //setting to a very large value for demo purpose.
                                Duration.ofMillis(starToClose + 5000)
                        )
                        .setCancellationType(WAIT_CANCELLATION_COMPLETED)
//                        .setHeartbeatTimeout(Duration.ofMillis(starToClose / 2))
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setBackoffCoefficient(1)
                                .setInitialInterval(Duration.ofSeconds(5))
                                .build())
                        //.setScheduleToStartTimeout(Duration.ofSeconds(2))
                        .build());


        private final MyActivitiesVersioning localActivity = Workflow.newLocalActivityStub(
                MyActivitiesVersioning.class,
                LocalActivityOptions.newBuilder()
                        .setStartToCloseTimeout(
                                //setting to a very large value for demo purpose.
                                Duration.ofMillis(starToClose + 1000_000)
                        )
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setBackoffCoefficient(1)
                                .build())
                        .setDoNotIncludeArgumentsIntoMarker(true)
                        //.setScheduleToStartTimeout(Duration.ofSeconds(2))
                        .build());

        private boolean updated = false;
        private boolean signaled = false;


      //  @WorkflowVersioningBehavior(VersioningBehavior.AUTO_UPGRADE)
        public String run(int iterations) {



            localActivity.sleep_time(300);

            {
                List<Promise<String>> promises = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                        promises.add(Async.function(activities::sleep, iterations+""));
                }

                Promise.allOf(promises).get();
            }


            if(iterations < 0){

                Workflow.continueAsNew(++iterations);
            }


            throw ApplicationFailure.newFailure("test", "test");



        }



    }


    public static class MyActivitiesImplVersioning implements MyActivitiesVersioning {


        private static final Logger log = LoggerFactory.getLogger("-");
        private static final Map<String, Integer> map = new HashMap<>();
        int activity = 0;

        @Override
        public String sleep(String input) {


            //log.info("Start ******* ");

            Date date = new Date();
            String second_ = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
            int counter = map.get(second_) == null ? 1 : map.get(second_) + 1;
            map.put(second_, counter);
            //log.info("Adding metric " + second_ + " : " + counter);
            Activity.getExecutionContext().getMetricsScope().counter("custom_activity_retries").inc(1);


            String sleep_activity_in_ms = FromEnv.getActivityLatencyMs();
            int i = Integer.parseInt(sleep_activity_in_ms);
            //log.info("SLEEP_ACTIVITY_IN_MS : " + i);

            activity++;
            //if(Activity.getExecutionContext().getInfo().getAttempt() < 6){

            if (activity % 2 == 0) {
                //  throw new RuntimeException("fake failure");
            }


            try {
                Thread.sleep(i);

                // findPrimes(100_000);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }


//            final int iteration = i / 1_000;
//            for (int j = 0; j < iteration; j++) {
//
//
//                Activity.getExecutionContext().heartbeat("iteration number  " + j + " of " + iteration);
//
//                try {
//                    Thread.sleep(1_000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//
//            }


            return null;
        }

        @Override
        public String sleep_time(int ms) {

            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return "";
        }

        public void findPrimes(int max) {
            for (int number = 2; number <= max; number++) {
                boolean isPrime = true;
                for (int divisor = 2; divisor <= Math.sqrt(number); divisor++) {
                    if (number % divisor == 0) {
                        isPrime = false;
                        break;
                    }
                }
            }
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


    public static class SomeKBString {
        public static String get() {
            int sizeInChars = 100_000;
            StringBuilder sb = new StringBuilder(sizeInChars);

            for (int i = 0; i < sizeInChars; i++) {
                sb.append('A'); // Or any other character
            }
            String halfMBString = sb.toString();
            return halfMBString;
        }
    }

}
