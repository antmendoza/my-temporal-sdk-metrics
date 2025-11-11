package com.temporal.grpc;

import io.grpc.*;

import java.util.Date;

public class HeaderLoggingInterceptor implements ClientInterceptor {

    private static final Metadata.Key<String> TEMPORAL_NAMESPACE_KEY =
            Metadata.Key.of("temporal-namespace", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {


        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {

                // If temporal-namespace appears more than once, warn and deduplicate to the first value
                Iterable<String> nsValues = headers.getAll(TEMPORAL_NAMESPACE_KEY);
                if (nsValues != null) {
                    String first = null;
                    int count = 0;
                    for (String v : nsValues) {
                        if (first == null) first = v;
                        count++;
                    }
                    if (count > 1) {
                        System.out.println(new Date() + " WARNING: duplicate 'temporal-namespace' headers detected (" + count + ") for method " + method.getFullMethodName() + ". Deduplicating to first value: '" + first + "'.");
                        headers.removeAll(TEMPORAL_NAMESPACE_KEY);
                        headers.put(TEMPORAL_NAMESPACE_KEY, first);
                    }
                }

                // Log request headers
                System.out.println(new Date() + " Request Headers: " + " " + method.getFullMethodName() + " " + headers);
                super.start(
                        new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                                responseListener) {
                            @Override
                            public void onHeaders(Metadata headers) {
                                // Log response headers
                                System.out.println(new Date() + " Response Headers: " + " " + method.getFullMethodName() + " - " + headers);
                                super.onHeaders(headers);
                            }

                            @Override
                            public void onMessage(RespT message) {
                                // Log response body/message
                               // System.out.println(new Date() + " Response Message: " + " " + method.getFullMethodName() + " " + safeToString(message));
                                super.onMessage(message);
                            }
                        },
                        headers);
            }

            @Override
            public void sendMessage(ReqT message) {
                // Log request body/message
                System.out.println(new Date() + " Request Message: " + " " + method.getFullMethodName() + " " + safeToString(message));
                super.sendMessage(message);
            }
        };
    }

    private static String safeToString(Object obj) {
        try {
            if (obj == null) return "<null>";
            return obj.toString();
        } catch (Throwable t) {
            return "<unable to stringify: " + t.getClass().getSimpleName() + ">";
        }
    }
}
