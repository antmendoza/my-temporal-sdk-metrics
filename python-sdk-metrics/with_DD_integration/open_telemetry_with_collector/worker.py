import asyncio
import logging
import os
import random
from datetime import timedelta

from opentelemetry._logs import get_logger_provider, set_logger_provider
from opentelemetry.exporter.otlp.proto.grpc._log_exporter import OTLPLogExporter
from opentelemetry.sdk._logs import LoggerProvider, LoggingHandler
from opentelemetry.sdk._logs._internal.export import BatchLogRecordProcessor
from opentelemetry.sdk.resources import Resource
from temporalio import activity, workflow
from temporalio.client import Client
from temporalio.contrib.opentelemetry import TracingInterceptor
from temporalio.runtime import OpenTelemetryConfig, Runtime, TelemetryConfig, PrometheusConfig, \
    OpenTelemetryMetricTemporality, LoggingConfig, TelemetryFilter, LogForwardingConfig
from temporalio.worker import Worker


@workflow.defn
class GreetingWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:
        seconds_ = await workflow.execute_activity(compose_greeting, name,
                                                   start_to_close_timeout=timedelta(seconds=60), )

        workflow.logger.warning("Workflow input parameter: %s" % name)

        # Emit test logs at various levels
        await workflow.execute_activity(emit_test_logs, name, start_to_close_timeout=timedelta(seconds=30))

        return seconds_


@activity.defn
async def compose_greeting(name: str) -> str:
    ## calculate random number smaller than 10
    random_sleep_seconds = random.randint(1, 10)
    await asyncio.sleep(random_sleep_seconds)
    return f"Hello, {name}!"


@activity.defn
async def emit_test_logs(name: str) -> str:
    """Emit log lines at multiple levels for validation in Datadog."""
    logger = logging.getLogger("app")
    logger.debug("debug: processing request for %s", name)
    logger.info("info: starting work for %s", name)
    logger.warning("warning: sample warning for %s", name)
    logger.error("error: sample error for %s", name)
    return f"emitted logs for {name}"


interrupt_event = asyncio.Event()


def init_runtime_with_telemetry() -> Runtime:
    endpoint = os.getenv("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4317")
    service_name = os.getenv("OTEL_SERVICE_NAME", "temporal-worker")
    service_version = os.getenv("OTEL_SERVICE_VERSION", "0.1.0")
    deployment_env = os.getenv("DEPLOYMENT_ENV", "dev")

    resource = Resource.create(
        {
            "service.name": service_name,
            "service.version": service_version,
            "deployment.environment": deployment_env,
        }
    )

    existing_lp = get_logger_provider()
    if isinstance(existing_lp, LoggerProvider):
        logger_provider = existing_lp
    else:
        logger_provider = LoggerProvider(resource=resource)
        set_logger_provider(logger_provider)


    logger_provider.add_log_record_processor(
        BatchLogRecordProcessor(
            OTLPLogExporter(
                endpoint=endpoint,
                insecure=True
            )
        )
    )

    # Bridge standard logging to OTel logs
    handler = LoggingHandler(level=logging.INFO, logger_provider=logger_provider)
    root_logger = logging.getLogger()
    root_logger.addHandler(handler)


    ## if env variable prometheus-port is not null setup prometheus
    prometheus_port = os.environ.get("PROMETHEUS_PORT")
    print(f"PROMETHEUS_PORT={prometheus_port}")

    if prometheus_port is not None:
        print("Using Prometheus")
        return Runtime(
            telemetry=TelemetryConfig(
                logging=LoggingConfig(
                    filter=TelemetryFilter(core_level="DEBUG", other_level="DEBUG"),
                    forwarding=LogForwardingConfig(logger=logger)
                ),
                metrics=PrometheusConfig(
                    bind_address="127.0.0.1:" + prometheus_port,
                    histogram_bucket_overrides={
                        #             "temporal_activity_schedule_to_start_latency": [1, 10, 30, 60, 120, 300, 600, 1800, 3600]
                    }
                ),
                global_tags={"anything": "worker_" + prometheus_port},
            )
        )
    else:
        print("No Prometheus, using OTel")
        # Setup SDK metrics to OTel endpoint
        WORKER_ID = "worker"

        return Runtime(
            telemetry=TelemetryConfig(
                metrics=OpenTelemetryConfig(
                    url="http://localhost:4317",
                    metric_periodicity=timedelta(seconds=1),
                    metric_temporality=OpenTelemetryMetricTemporality.DELTA,
                    # headers={"x-honeycomb-dataset": "temporal-metrics",
                    # durations_as_seconds=True
                ),
                global_tags={"anything": "worker_" + WORKER_ID,
                             "env": "worker_" + WORKER_ID,
                             "env33": "worker_" + WORKER_ID
                             },

            )
        )


async def main():
    runtime = init_runtime_with_telemetry()

    # Connect client
    client = await Client.connect(
        "localhost:7233",
        # Use OpenTelemetry interceptor
        interceptors=[TracingInterceptor()],
        runtime=runtime,
    )

    # Run a worker for the workflow
    async with Worker(
            client,
            task_queue="open_telemetry-task-queue",
            workflows=[GreetingWorkflow],
            activities=[compose_greeting, emit_test_logs],
            max_concurrent_workflow_tasks=10,
            max_concurrent_activities=10,
            #            max_concurrent_activities=100,
    ):
        # Wait until interrupted
        print("Worker started, ctrl+c to exit")
        await interrupt_event.wait()
        print("Shutting down")


if __name__ == "__main__":
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    interrupt_event = asyncio.Event()  # Now it's bound to this loop
    try:
        loop.run_until_complete(main())
    except KeyboardInterrupt:
        interrupt_event.set()
        loop.run_until_complete(loop.shutdown_asyncgens())
