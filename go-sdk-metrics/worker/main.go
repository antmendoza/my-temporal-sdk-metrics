package main

import (
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/worker"
	"log"
	"sample_5888"
)

func main() {

	c, err := client.Dial(client.Options{
		MetricsHandler: sample_5888.GetMetricsHandler("9095"),
	})
	defer c.Close()
	worker.SetStickyWorkflowCacheSize(2)

	w := worker.New(c, "metrics", worker.Options{})

	w.RegisterWorkflow(sample_5888.Workflow)
	w.RegisterActivity(sample_5888.Activity)

	err = w.Run(worker.InterruptCh())
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}

}
