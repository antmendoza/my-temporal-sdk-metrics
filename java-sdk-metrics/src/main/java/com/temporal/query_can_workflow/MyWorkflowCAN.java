package com.temporal.query_can_workflow;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface MyWorkflowCAN extends MyWorkflow {

    @WorkflowMethod
    String run(final String name);

    @QueryMethod
    String status();
}

