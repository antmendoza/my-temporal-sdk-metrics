package sample_5888

import (
	"context"
	prom "github.com/prometheus/client_golang/prometheus"
	"github.com/uber-go/tally/v4"
	"github.com/uber-go/tally/v4/prometheus"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetrichttp"
	"go.opentelemetry.io/otel/exporters/stdout/stdouttrace"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/sdk/instrumentation"
	"go.opentelemetry.io/otel/sdk/metric"
	"go.opentelemetry.io/otel/sdk/resource"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
	semconv "go.opentelemetry.io/otel/semconv/v1.17.0"
	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/contrib/opentelemetry"
	sdktally "go.temporal.io/sdk/contrib/tally"
	"log"
	"time"
)

func InitializeGlobalTracerProvider() (*sdktrace.TracerProvider, error) {
	// Initialize tracer
	exp, err := stdouttrace.New(stdouttrace.WithPrettyPrint())
	if err != nil {
		return nil, err
	}
	tp := sdktrace.NewTracerProvider(
		sdktrace.WithBatcher(exp),
		sdktrace.WithResource(resource.NewWithAttributes(
			semconv.SchemaURL,
			semconv.ServiceName("temporal-example"),
			semconv.ServiceVersion("0.0.1"),
		)),
	)
	otel.SetTracerProvider(tp)

	otel.SetTextMapPropagator(
		propagation.NewCompositeTextMapPropagator(
			propagation.TraceContext{},
			propagation.Baggage{},
		),
	)

	return tp, nil
}

func NewPrometheusScope(c prometheus.Configuration) tally.Scope {
	reporter, err := c.NewReporter(
		prometheus.ConfigurationOptions{
			Registry: prom.NewRegistry(),
			OnError: func(err error) {
				log.Println("error in prometheus reporter", err)
			},
		},
	)
	if err != nil {
		log.Fatalln("error creating prometheus reporter", err)
	}
	scopeOpts := tally.ScopeOptions{
		CachedReporter:  reporter,
		Separator:       prometheus.DefaultSeparator,
		SanitizeOptions: &sdktally.PrometheusSanitizeOptions,
		Prefix:          "",
	}
	scope, _ := tally.NewRootScope(scopeOpts, time.Second)
	scope = sdktally.NewPrometheusNamingScope(scope)

	log.Println("prometheus metrics scope created")
	return scope
}

func GetMetricsHandler(port string) client.MetricsHandler {
	var metricsHandler client.MetricsHandler = nil

	if false {
		//os.Getenv("ENABLE_TELEMETRY") == "true"

		ctx := context.Background()
		exp, err := otlpmetrichttp.New(ctx, otlpmetrichttp.WithEndpointURL("http://localhost:4318"))
		if err != nil {
			panic(err)
		}

		// Create a MeterProvider with explicit bucket histogram configuration
		view := metric.NewView(
			metric.Instrument{
				//Name:  "temporal*latency*",
				Name: "*",

				Scope: instrumentation.Scope{Name: "temporal-sdk-go"},
			},
			metric.Stream{
				Aggregation: metric.AggregationExplicitBucketHistogram{
					Boundaries: []float64{0, 0.2, 0.5, 1, 2, 3, 4, 5, 10, 25, 50, 75, 100,
						200, 300, 400, 500, 1000, 2000, 5000, 10000},
				},
			},
		)
		meterProvider := metric.NewMeterProvider(
			metric.WithReader(metric.NewPeriodicReader(exp, metric.WithInterval(1*time.Second))),
			metric.WithView(view),
		)

		metricsHandler = opentelemetry.NewMetricsHandler(
			opentelemetry.MetricsHandlerOptions{
				Meter: meterProvider.Meter("temporal-sdk-go"),
			},
		)

	} else {

		metricsHandler = sdktally.NewMetricsHandler(NewPrometheusScope(prometheus.Configuration{
			ListenAddress: "0.0.0.0:" + port,
			TimerType:     "histogram",
			DefaultHistogramBuckets: []prometheus.HistogramObjective{{Upper: 0.1}, {Upper: 0.2}, {Upper: 0.5}, {Upper: 1},
				{Upper: 2}, {Upper: 5}, {Upper: 10}},
		}))

	}

	return metricsHandler
}
