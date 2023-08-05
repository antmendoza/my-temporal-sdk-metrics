package com.antmendoza.temporal.parent_child;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface Parent2 {



@WorkflowMethod
    void startWorkflow(String input);
}
