package com.temporal.query_can_workflow;

import com.temporal.config.FromEnv;


public class MyActivityImpl implements MyActivity {
    @Override
    public String doSomething(final String name) {
        //String result = name + " " + _1MB;
        String result = "name";


        // Simulate some work
        try {
            Thread.sleep(Long.parseLong(FromEnv.getActivityLatencyMs()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result.substring(0, 1);
    }
}
