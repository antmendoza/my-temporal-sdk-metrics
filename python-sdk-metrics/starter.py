import asyncio
import random

from temporalio.client import Client

from worker import GreetingWorkflow, init_runtime_with_prometheus

interrupt_event = asyncio.Event()


async def main():
    runtime = init_runtime_with_prometheus(8085)

    # Connect client
    client = await Client.connect(
        "localhost:7233",
        runtime=runtime,
    )

    print(
        "Prometheus client metrics available at http://127.0.0.1:8085/metrics, ctrl+c to exit"
    )

    while True:
        await asyncio.sleep(0.1)
        try:
            await asyncio.sleep(0.1)
            await client.get_workflow_handle("prometheus-workflow-id").signal("test","test")
        except Exception:
            pass

        try:
            # Run workflow
            result = await client.start_workflow(
                GreetingWorkflow.run,
                "Temporal",
                id="prometheus-workflow-id" + str(random.randint(0, 1000)),
                task_queue="prometheus-task-queue",
            )
            print(f"Workflow result: {result}")
        except Exception:
            pass



    await interrupt_event.wait()


if __name__ == "__main__":
    loop = asyncio.new_event_loop()
    try:
        loop.run_until_complete(main())
    except KeyboardInterrupt:
        interrupt_event.set()
        loop.run_until_complete(loop.shutdown_asyncgens())
