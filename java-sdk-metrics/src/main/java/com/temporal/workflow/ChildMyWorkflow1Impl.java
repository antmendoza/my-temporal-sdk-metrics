package com.temporal.workflow;

import io.temporal.workflow.Workflow;

public class ChildMyWorkflow1Impl implements ChildMyWorkflow1 {
    @Override
    public String run(final String name) {

        Workflow.sleep(1500);
        return "";
    }

    @Override
    public String update() {
        return "";
    }
}
