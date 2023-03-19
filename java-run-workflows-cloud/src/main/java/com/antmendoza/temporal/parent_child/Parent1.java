package com.antmendoza.temporal.parent_child;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface Parent1 {



@WorkflowMethod
    void startWorkflow(String input);
}
