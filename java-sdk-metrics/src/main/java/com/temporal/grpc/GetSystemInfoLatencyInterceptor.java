package com.temporal.grpc;

import io.grpc.*;


public class GetSystemInfoLatencyInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        final String fullMethodName = method.getFullMethodName();
        final boolean isGetSystemInfo = fullMethodName != null &&
                fullMethodName.contains("GetSystemInfo");

        ClientCall<ReqT, RespT> delegate = next.newCall(method, callOptions);
        if (!isGetSystemInfo) {
            return delegate;
        }

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(delegate) {
            private long startNanos;

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                startNanos = System.nanoTime();
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;
                        System.out.println(new java.util.Date() + " GetSystemInfo latency: " + durationMs + " ms, status=" + status);
                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }
}

