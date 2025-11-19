package com.poc.bcp;

import io.grpc.*;

import java.util.concurrent.atomic.AtomicInteger;

import static com.poc.bcp.Starter.workflowTaskTimeout;

public class BCPFailureInjectionInterceptor implements ClientInterceptor {


    final AtomicInteger counter = new AtomicInteger();


    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions,
                                                               Channel next) {

        // if the method name matches the pattern and failure injection is enabled
        if (method.getFullMethodName().contains("RespondWorkflowTaskCompleted")
        ) {
            if (counter.incrementAndGet() == 3) {
                try {
                    //This is to simulate delay in RespondWorkflowTaskCompleted, workflowTaskTimeout
                    Thread.sleep(workflowTaskTimeout.toMillis());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return next.newCall(method, callOptions);
    }

}
