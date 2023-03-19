package com.antmendoza.temporal.parent_child;

import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.failure.ChildWorkflowFailure;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Workflow;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;

import java.util.Optional;

import static com.antmendoza.temporal.parent_child.Worker.TASK_QUEUE;

public class Parent2Impl implements Parent2 {



    Logger log = Workflow.getLogger("Parent2Impl");

    @Override
    public void startWorkflow(String input) {
        extracted(input, log);
    }

    static void extracted(String input, Logger log) {
        RetryOptions childWfRetryOptions = RetryOptions.newBuilder()
                .setMaximumAttempts(2).build();

        Workflow.sleep(200);


        Workflow.retry(
                childWfRetryOptions,
                Optional.empty(),
                () -> startChildWorkflow(input, log));


    }

     static void startChildWorkflow(String input, Logger log) {
        String businessKey = "my-child-workflow";
        ChildWorkflowOptions options = ChildWorkflowOptions.newBuilder()
                .setTaskQueue(TASK_QUEUE)
                .setWorkflowId(businessKey)
                .build();
        ChildWorkflow childWf = Workflow.newChildWorkflowStub(ChildWorkflow.class, options);
        try {
            log.info("Starting child workflow for {}", input);
            childWf.startWorkflow(input);
        } catch (ChildWorkflowFailure e) {


            log.info("ChildWorkflowFailure {}", e);

            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof WorkflowExecutionAlreadyStarted) {

                log.info("rootCause {}", rootCause);



                throw ApplicationFailure.newFailure(
                        String.format("A child workflow with the business key %s already exists. " +
                                "System will retry after some time", businessKey), "my exception");
            }
            throw e;
        }
    }
}


