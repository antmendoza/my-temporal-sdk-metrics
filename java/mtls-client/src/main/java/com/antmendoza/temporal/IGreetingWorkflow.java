package com.antmendoza.temporal;

import io.temporal.workflow.WorkflowMethod;

public interface IGreetingWorkflow {

    @WorkflowMethod
    String getGreeting(String name);
}
