package com.antmendoza.temporal.parent_child;

import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.util.Optional;

import static com.antmendoza.temporal.parent_child.Parent2Impl.extracted;
import static com.antmendoza.temporal.parent_child.Parent2Impl.startChildWorkflow;

public class Parent1Impl implements Parent1 {


    Logger log = Workflow.getLogger("Parent1Impl");

    @Override
    public void startWorkflow(String input) {

        extracted(input, log);

    }

}


