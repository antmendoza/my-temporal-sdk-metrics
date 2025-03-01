package main

import (
	"context"
	"log"
	"sample_5888"
	"strconv"
	"time"

	"go.temporal.io/sdk/client"
)

func main() {

	c, err := client.Dial(client.Options{
		MetricsHandler: sample_5888.GetMetricsHandler("9096"),
	})
	if err != nil {
		log.Fatalln("Unable to create client.", err)
	}
	defer c.Close()

	for i := 0; i < 1000; i++ {
		workflowOptions := client.StartWorkflowOptions{
			ID:        strconv.Itoa(i),
			TaskQueue: "metrics",
		}

		we, err := c.ExecuteWorkflow(context.Background(), workflowOptions, sample_5888.Workflow)
		if err != nil {
			log.Fatalln("Unable to execute workflow.", err)
		}

		log.Println("Started workflow.", "WorkflowID", we.GetID(), "RunID", we.GetRunID())

		time.Sleep(100 * time.Millisecond)

		// Synchronously wait for the workflow completion.
		//err = we.Get(context.Background(), nil)
		//if err != nil {
		//	log.Fatalln("Unable to wait for workflow completion.", err)
		//}

	}

}
