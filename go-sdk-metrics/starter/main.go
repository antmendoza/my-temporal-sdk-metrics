package main

import (
	"context"
	"github.com/uber-go/tally/v4/prometheus"
	sdktally "go.temporal.io/sdk/contrib/tally"
	"log"
	"sample_5888"

	"go.temporal.io/sdk/client"
)

func main() {
	// The client is a heavyweight object that should be created once per process.
	tallyHandler := sdktally.NewMetricsHandler(sample_5888.NewPrometheusScope(prometheus.Configuration{
		ListenAddress: "0.0.0.0:9096",
		TimerType:     "histogram",
	}))

	c, err := client.Dial(client.Options{
		MetricsHandler: tallyHandler,
	})
	if err != nil {
		log.Fatalln("Unable to create client.", err)
	}
	defer c.Close()

	for i := 0; i < 1000; i++ {
		workflowOptions := client.StartWorkflowOptions{
			ID:        "metrics_workflowID",
			TaskQueue: "metrics",
		}

		we, err := c.ExecuteWorkflow(context.Background(), workflowOptions, sample_5888.Workflow)
		if err != nil {
			log.Fatalln("Unable to execute workflow.", err)
		}

		log.Println("Started workflow.", "WorkflowID", we.GetID(), "RunID", we.GetRunID())

		// Synchronously wait for the workflow completion.
		err = we.Get(context.Background(), nil)
		if err != nil {
			log.Fatalln("Unable to wait for workflow completion.", err)
		}

	}

}
