import asyncio
from datetime import timedelta

from temporalio import activity, workflow
from temporalio.client import Client
from temporalio.common import RetryPolicy
from temporalio.runtime import PrometheusConfig, Runtime, TelemetryConfig
from temporalio.worker import Worker


@workflow.defn
class GreetingWorkflow:
    @workflow.run
    async def run(self, name: str) -> str:

        result = await workflow.execute_local_activity(
            compose_greeting,
            name,
            start_to_close_timeout=timedelta(seconds=3),
            retry_policy=RetryPolicy(
                maximum_attempts=1,
            )
        )

        await workflow.execute_local_activity(
            compose_greeting,
            name,
            start_to_close_timeout=timedelta(seconds=3),
            retry_policy=RetryPolicy(
                maximum_attempts=1,
            )
        )


        result = await workflow.execute_activity(
            compose_greeting,
            name,
            start_to_close_timeout=timedelta(seconds=3),
            retry_policy=RetryPolicy(
                maximum_attempts=1,
            )
        )


        result = await workflow.execute_activity(
            compose_greeting,
            name,
            start_to_close_timeout=timedelta(seconds=3),
            retry_policy=RetryPolicy(
                maximum_attempts=1,
            )
        )

        await asyncio.sleep(1)

        return result


@activity.defn
async def compose_greeting(name: str) -> str:
    await asyncio.sleep(1.2)
    return f"Hello, {name}!"


interrupt_event = asyncio.Event()


def init_runtime_with_prometheus(port: int) -> Runtime:
    # Create runtime for use with Prometheus metrics
    return Runtime(
        telemetry=TelemetryConfig(
            metrics=PrometheusConfig(
                bind_address=f"127.0.0.1:{port}",
                durations_as_seconds=True
            )
        )
    )


async def main():
    runtime = init_runtime_with_prometheus(8086)

    # Connect client
    client = await Client.connect(
        "localhost:7233",
        runtime=runtime,
    )

    # Run a worker for the workflow
    async with Worker(
            client,
            task_queue="prometheus-task-queue",
            workflows=[GreetingWorkflow],
            activities=[compose_greeting],
    ):
        # Wait until interrupted
        print("Worker started")
        print(
            "Prometheus metrics available at http://127.0.0.1:8086/metrics, ctrl+c to exit"
        )
        await interrupt_event.wait()
        print("Shutting down")


if __name__ == "__main__":
    loop = asyncio.new_event_loop()
    try:
        loop.run_until_complete(main())
    except KeyboardInterrupt:
        interrupt_event.set()
        loop.run_until_complete(loop.shutdown_asyncgens())