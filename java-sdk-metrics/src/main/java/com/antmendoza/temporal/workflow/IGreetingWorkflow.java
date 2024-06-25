package com.antmendoza.temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface IGreetingWorkflow {

    @WorkflowMethod
    String getGreeting(String name);
}
