package com.temporal.query_can_workflow;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface MyActivity {


    String doSomething(String name);

}
