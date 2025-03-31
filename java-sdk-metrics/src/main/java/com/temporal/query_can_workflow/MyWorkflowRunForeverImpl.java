package com.temporal.query_can_workflow;

import com.temporal.config.FromEnv;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.unsafe.WorkflowUnsafe;

import java.time.Duration;

public class MyWorkflowRunForeverImpl implements MyWorkflowRunForever {

    private final MyActivity activity = Workflow.newActivityStub(MyActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMillis(
                            Long.parseLong(FromEnv.getActivityLatencyMs())
                                    +
                                    2000)
                    )
                    .build());


    @Override
    public String run(final String name) {

        //Simulate object stored as workflow variable
        //final String result = new String(_1MB);

        if (WorkflowUnsafe.isReplaying()) {
            // System.out.println(Workflow.getInfo().getWorkflowId() +  ":  Replaying workflow ");
        }

        while (true) {
            activity.doSomething(name);
            Workflow.sleep(Duration.ofSeconds(1));
        }
    }

    @Override
    public String status() {
        return "";
    }
}
