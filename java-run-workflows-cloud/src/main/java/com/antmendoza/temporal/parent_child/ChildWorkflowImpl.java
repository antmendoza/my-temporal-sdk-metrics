package com.antmendoza.temporal.parent_child;

import io.temporal.workflow.Workflow;

import java.time.Duration;

public class ChildWorkflowImpl implements ChildWorkflow {
    @Override
    public void startWorkflow(String input) {

        Workflow.sleep(Duration.ofSeconds(10));
    }
}
