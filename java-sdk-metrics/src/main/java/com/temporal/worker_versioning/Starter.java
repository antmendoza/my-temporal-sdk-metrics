package com.temporal.worker_versioning;

import com.temporal.Client;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.RetryOptions;
import io.temporal.common.VersioningOverride;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static com.temporal.WorkerSsl.TASK_QUEUE_VERSIONING;

public class Starter {


    public static void main(String[] args) throws InterruptedException {

        WorkflowClient client = new Client().getWorkflowClient();

        final int millisSleep = 500;

        final AtomicInteger input = new AtomicInteger();
        //while (millisSleep > 0) {


        for (int i = 0; i < 1; i++) {


            final int andIncrement = input.getAndIncrement();
            CompletableFuture.runAsync(() -> {
                final String workflowId = andIncrement + "-" + Math.random();
                try {

                    WorkflowOptions workflowOptions =
                            WorkflowOptions.newBuilder()
                                    .setTaskQueue(TASK_QUEUE_VERSIONING)
                                    .setWorkflowId(workflowId)
                                    .setVersioningOverride(new VersioningOverride.AutoUpgradeVersioningOverride())
                                    .setRetryOptions(
                                            RetryOptions.newBuilder()
                                                    .setMaximumAttempts(3)
                                                    .build())
                                    .build();


                    MyWorkflowVersioning workflow = client.newWorkflowStub(MyWorkflowVersioning.class, workflowOptions);
                    System.out.println("Starting workflow...with after = " + millisSleep + " ms");
                    System.out.println(workflowId + "init " + new Date());
                    WorkflowExecution execution = WorkflowClient.start(workflow::run, 0);
                    System.out.println(workflowId + "end " + new Date());


                } catch (Exception e) {

                    System.out.println("Failed workflowId = " + workflowId);
                }
            });


            Thread.sleep(millisSleep);


        }


    }


}
