package com.temporal.query_can_workflow;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MyWorkflowRunForever extends MyWorkflow {

    @WorkflowMethod
    String run(String name);


    @QueryMethod
    String status();
}
