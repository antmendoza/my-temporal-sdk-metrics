package main

import (
	"context"
	"github.com/uber-go/tally/v4/prometheus"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetrichttp"
	"go.opentelemetry.io/otel/sdk/metric"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/contrib/opentelemetry"
	sdktally "go.temporal.io/sdk/contrib/tally"
	"go.temporal.io/sdk/worker"
	"log"
	"os"
	"sample_5888"
	"time"
)

func main() {

	var metricsHandler client.MetricsHandler = nil

	if os.Getenv("ENABLE_TELEMETRY") == "true" {

		ctx := context.Background()
		exp, err := otlpmetrichttp.New(ctx, otlpmetrichttp.WithEndpointURL("http://localhost:4318"))
		if err != nil {
			panic(err)
		}
		meterProvider := metric.NewMeterProvider(metric.WithReader(
			metric.NewPeriodicReader(exp, metric.WithInterval(1*time.Second)),
		))

		metricsHandler = opentelemetry.NewMetricsHandler(
			opentelemetry.MetricsHandlerOptions{
				Meter: meterProvider.Meter("temporal-sdk-go"),
			},
		)

	} else {

		metricsHandler = sdktally.NewMetricsHandler(sample_5888.NewPrometheusScope(prometheus.Configuration{
			ListenAddress: "0.0.0.0:9095",
			TimerType:     "histogram",
		}))

	}

	c, err := client.Dial(client.Options{
		MetricsHandler: metricsHandler,
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
