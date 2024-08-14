import { DefaultLogger, Worker, Runtime } from '@temporalio/worker';
import { Resource } from '@opentelemetry/resources';
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';
import { ConsoleSpanExporter } from '@opentelemetry/sdk-trace-base';
import { NodeSDK } from '@opentelemetry/sdk-node';
import {
  OpenTelemetryActivityInboundInterceptor,
  makeWorkflowExporter,
} from '@temporalio/interceptors-opentelemetry/lib/worker';
import * as activities from './activities';

async function main() {
  const resource = new Resource({
    [SemanticResourceAttributes.SERVICE_NAME]: 'interceptors-sample-worker',
  });


//  const options = {port: 9464};
//  const exporter = new PrometheusExporter(options);


  // Export spans to console for simplicity
  const exporter = new ConsoleSpanExporter();

  const otel = new NodeSDK({ traceExporter: exporter, resource });
  await otel.start();

  // Silence the Worker logs to better see the span output in this sample
  Runtime.install(
      {
        telemetryOptions: {
          metrics: {
            prometheus: { bindAddress: '0.0.0.0:9464' },
          },
          logging: { forward: { level: 'DEBUG' } },
        },
        logger: new DefaultLogger('WARN')
      });

  const worker = await Worker.create({
    workflowsPath: require.resolve('./workflows'),
    activities,
//    maxCachedWorkflows:10,
//    maxConcurrentActivityTaskPolls:5,
//    maxConcurrentWorkflowTaskPolls:5,
//    maxConcurrentWorkflowTaskExecutions:5,
//    maxConcurrentActivityTaskExecutions:5,
    taskQueue: 'interceptors-opentelemetry-example',
    sinks: {
      exporter: makeWorkflowExporter(exporter, resource),
    },
    // Registers opentelemetry interceptors for Workflow and Activity calls
    interceptors: {
      // example contains both workflow and interceptors
      workflowModules: [require.resolve('./workflows')],
      activityInbound: [(ctx) => new OpenTelemetryActivityInboundInterceptor(ctx)],
    },
  });
  try {
    await worker.run();
  } finally {
    await otel.shutdown();
  }
}

main().then(
  () => void process.exit(0),
  (err) => {
    console.error(err);
    process.exit(1);
  }
);
