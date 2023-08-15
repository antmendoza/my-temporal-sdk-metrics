package com.antmendoza.temporal.sinch._3;

import com.antmendoza.temporal.sinch.ProcessMessage;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.antmendoza.temporal.sinch.Taskqueue.TASK_QUEUE;

public class ChatBotRequestImpl implements ChatBotRequest {
    private final ProcessMessage processMessage =
            Workflow.newActivityStub(
                    ProcessMessage.class,
                    ActivityOptions.newBuilder()
                            .setTaskQueue(TASK_QUEUE)
                            .setStartToCloseTimeout(
                                    Duration.ofSeconds(10)
                            )
                            .build());


    @Override
    public String start(String input) {

        return processMessage.process(input);
    }

}
