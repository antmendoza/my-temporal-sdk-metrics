package com.temporal.worker_versioning;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MyWorkflowVersioning {

    @WorkflowMethod
    String run(int iterations);


}
