package com.temporal.workflow_one_activity_not_retry;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MyWorkflowDontRetry {

    @WorkflowMethod
    String run(String name);


}
