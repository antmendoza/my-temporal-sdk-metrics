package com.temporal.grpc;

import io.grpc.*;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Client-side gRPC interceptor to inject retryable failures for testing.
 * <p>
 * Controlled by environment variables (all optional):
 * - INJECT_GRPC_FAILURES: enable/disable (default: false)
 * - INJECT_GRPC_METHOD_SUBSTR: comma-separated substrings to match method names (default: "*")
 * - INJECT_GRPC_FAIL_PERCENT: 0-100 percent of matching calls to fail (default: 0)
 * - INJECT_GRPC_FAIL_FIRST_N: fail the first N matching calls per-method (default: 0)
 * <p>
 * A call is failed if either the random percentage triggers, or the per-method
 * first-N budget has remaining failures.
 * <p>
 * Failures are returned with Status.UNAVAILABLE which the Temporal SDK treats as retryable.
 */
public class FailureInjectionInterceptor implements ClientInterceptor {

    private static final boolean ENABLED = Boolean.parseBoolean(System.getenv().getOrDefault("INJECT_GRPC_FAILURES", "false"));
    private static final String METHOD_PATTERN = System.getenv().getOrDefault("INJECT_GRPC_METHOD_SUBSTR", "*");
    private static final int FAIL_PERCENT = parseIntBounded(System.getenv("INJECT_GRPC_FAIL_PERCENT"), 0, 100, 0);
    private static final int FAIL_FIRST_N = parseIntBounded(System.getenv("INJECT_GRPC_FAIL_FIRST_N"), 0, Integer.MAX_VALUE, 0);

    private static final Set<String> METHOD_TOKENS = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<String, Integer> REMAINING_FIRST_N = new ConcurrentHashMap<>();

    static {
        if (!METHOD_PATTERN.isBlank()) {
            Arrays.stream(METHOD_PATTERN.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(METHOD_TOKENS::add);
        }
    }

    private static int parseIntBounded(String raw, int min, int max, int def) {
        if (raw == null || raw.isBlank()) return def;
        try {
            int v = Integer.parseInt(raw.trim());
            if (v < min) return min;
            if (v > max) return max;
            return v;
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static boolean methodMatches(String fullMethodName) {
        if (METHOD_TOKENS.isEmpty() || METHOD_TOKENS.contains("*")) return true;
        for (String token : METHOD_TOKENS) {
            if (fullMethodName.contains(token)) return true;
        }
        return false;
    }

    private static boolean shouldFail(String method) {
        // Percent-based
        if (FAIL_PERCENT > 0) {
            int r = ThreadLocalRandom.current().nextInt(100);
            if (r < FAIL_PERCENT) return true;
        }

        // First-N per-method
        if (FAIL_FIRST_N > 0) {
            final boolean[] fire = new boolean[1];
            REMAINING_FIRST_N.compute(method, (k, v) -> {
                int remaining = (v == null) ? FAIL_FIRST_N : v;
                if (remaining > 0) {
                    fire[0] = true;
                    return remaining - 1;
                }
                return remaining;
            });
            return fire[0];
        }

        return false;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions,
                                                               Channel next) {
        if (!ENABLED) {
            return next.newCall(method, callOptions);
        }

        final String fullMethodName = method.getFullMethodName();
        final boolean matches = methodMatches(fullMethodName);
        if (!matches) {
            return next.newCall(method, callOptions);
        }

        ClientCall<ReqT, RespT> delegate = next.newCall(method, callOptions);

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(delegate) {
            private boolean failed = false;

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                if (shouldFail(fullMethodName)) {
                    failed = true;
                    System.out.println(new Date() + " [FailureInjection] Injecting UNAVAILABLE for " + fullMethodName +
                            " (percent=" + FAIL_PERCENT + ", firstN=" + FAIL_FIRST_N + ")");
                    // Immediately fail the call without touching the delegate.
                    responseListener.onClose(Status.UNAVAILABLE.withDescription("Injected failure"), new Metadata());
                    return;
                }
                super.start(responseListener, headers);
            }

            @Override
            public void sendMessage(ReqT message) {
                if (failed) {
                    // Swallow messages for the failed call.
                    return;
                }
                super.sendMessage(message);
            }
        };
    }
}
