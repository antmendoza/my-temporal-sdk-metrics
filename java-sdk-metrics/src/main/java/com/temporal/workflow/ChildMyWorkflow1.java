package com.temporal.workflow;

import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ChildMyWorkflow1 {

    @WorkflowMethod
    String run(String name);


    @UpdateMethod
    String update();
}
