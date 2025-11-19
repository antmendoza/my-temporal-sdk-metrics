package com.temporal.workflow_one_activity_not_retry;

import com.temporal.Client;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static com.temporal.WorkerSsl.TASK_QUEUE;

public class Starter {


    public static void main(String[] args) throws InterruptedException {

        WorkflowClient client = new Client().getWorkflowClient();


        final int millisSleep = 500;

        final AtomicInteger input = new AtomicInteger();
        //while (millisSleep > 0) {


//        for (int i = 0; i < 20; i++) {

        while (true) {


            final int andIncrement = input.getAndIncrement();
            CompletableFuture.runAsync(() -> {
                final String workflowId = andIncrement + "-" + Math.random();
                try {

                    WorkflowOptions workflowOptions =
                            WorkflowOptions.newBuilder()
                                    .setTaskQueue(TASK_QUEUE)
                                    .setWorkflowId(workflowId)
                                    .build();


                    MyWorkflowDontRetry workflow = client.newWorkflowStub(MyWorkflowDontRetry.class, workflowOptions);
                    System.out.println("Starting workflow...with after = " + millisSleep + " ms");
                    System.out.println(workflowId + "init " + new Date());
                    WorkflowExecution execution = WorkflowClient.start(workflow::run, "" + andIncrement);
                    System.out.println(workflowId + "end " + new Date());


                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Failed workflowId = " + workflowId);
                }
            });


            Thread.sleep(millisSleep);


        }


    }


}
