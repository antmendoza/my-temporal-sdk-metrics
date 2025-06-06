package com.temporal.query_can_workflow;

import io.temporal.api.common.v1.Payload;
import io.temporal.api.common.v1.Payloads;
import io.temporal.common.converter.DataConverterException;
import io.temporal.workflow.unsafe.WorkflowUnsafe;

import java.lang.reflect.Type;
import java.util.Optional;

public class MyDataConverter implements io.temporal.common.converter.DataConverter {


    private final int millis_10 = 10;

    @Override
    public <T> Optional<Payload> toPayload(final T value) throws DataConverterException {

        if (isQuery_CAN_Workflow()) {
            return WorkflowUnsafe.deadlockDetectorOff(
                    () -> {
                        sleep(millis_10);
                        return Optional.empty();
                    });

        }


        return Optional.empty();


    }


    @Override
    public <T> T fromPayload(final Payload payload, final Class<T> valueClass, final Type valueType) throws DataConverterException {

        if (isQuery_CAN_Workflow()) {
            return WorkflowUnsafe.deadlockDetectorOff(
                    () -> {
                        sleep(millis_10);
                        return null;
                    });

        }


        return null;
    }

    @Override
    public Optional<Payloads> toPayloads(final Object... values) throws DataConverterException {

        if (isQuery_CAN_Workflow()) {
            return WorkflowUnsafe.deadlockDetectorOff(
                    () -> {
                        sleep(millis_10);
                        return Optional.empty();
                    });

        }


        return Optional.empty();
    }

    @Override
    public <T> T fromPayloads(final int index, final Optional<Payloads> content, final Class<T> valueType, final Type valueGenericType) throws DataConverterException {


        if (isQuery_CAN_Workflow()) {
            return WorkflowUnsafe.deadlockDetectorOff(
                    () -> {
                        sleep(millis_10);
                        return null;
                    });

        }


        return null;

    }


    private void sleep(long millis_10) {
        try {
            Thread.sleep(millis_10);
        } catch (InterruptedException e) {
            //throw new RuntimeException(e);
        }
    }

    private static boolean isQuery_CAN_Workflow() {

        return true;
        //return Workflow.getInfo().getWorkflowType().equals(MyWorkflowCAN.class.getName())
        //        ||
        //        Workflow.getInfo().getWorkflowType().equals(MyWorkflowRunForever.class.getName());
    }
}