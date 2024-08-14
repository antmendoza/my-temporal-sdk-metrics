import {proxyActivities, proxyLocalActivities, sleep, WorkflowInterceptorsFactory} from '@temporalio/workflow';
import {
    OpenTelemetryInboundInterceptor,
    OpenTelemetryOutboundInterceptor,
} from '@temporalio/interceptors-opentelemetry/lib/workflow';
import type * as activities from './activities';

const {greet, greet_failed} = proxyActivities<typeof activities>({
    startToCloseTimeout: '20 second',
    retry: {
        maximumAttempts: 2
    }
});


const {greetLocal} = proxyLocalActivities<typeof activities>({
    startToCloseTimeout: '1 minute',
});

// A workflow that simply calls an activity
export async function example(name: string): Promise<string> {
    await greet(name);
    await greetLocal(name);
    await sleep("10s")
    return "done";
}

// Export the interceptors
export const interceptors: WorkflowInterceptorsFactory = () => ({
    inbound: [new OpenTelemetryInboundInterceptor()],
    outbound: [new OpenTelemetryOutboundInterceptor()],
});
