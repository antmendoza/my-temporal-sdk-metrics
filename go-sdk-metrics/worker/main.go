package main

import (
	"github.com/uber-go/tally/v4/prometheus"
	"go.temporal.io/sdk/client"
	sdktally "go.temporal.io/sdk/contrib/tally"
	"go.temporal.io/sdk/worker"
	"log"
	"sample_5888"
)

func main() {

	//	ctx := context.Background()
	//exp, err := otlpmetrichttp.New(ctx, otlpmetrichttp.WithEndpointURL("http://localhost:4318"))
	//if err != nil {
	//	panic(err)
	//}
	//meterProvider := metric.NewMeterProvider(metric.WithReader(
	//	metric.NewPeriodicReader(exp, metric.WithInterval(1*time.Second)),
	//))
	//handler := opentelemetry.NewMetricsHandler(
	//	opentelemetry.MetricsHandlerOptions{
	//		Meter: meterProvider.Meter("temporal-sdk-go"),
	//	},
	//)

	tallyHandler := sdktally.NewMetricsHandler(sample_5888.NewPrometheusScope(prometheus.Configuration{
		ListenAddress: "0.0.0.0:9095",
		TimerType:     "histogram",
	}))

	c, err := client.Dial(client.Options{
		MetricsHandler: tallyHandler,
	})

	defer c.Close()

	w := worker.New(c, "metrics", worker.Options{})

	w.RegisterWorkflow(sample_5888.Workflow)
	w.RegisterActivity(sample_5888.Activity)

	err = w.Run(worker.InterruptCh())
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}
}
