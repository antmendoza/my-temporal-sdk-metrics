package com.antmendoza.temporal;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;


public class OpenTelemetryConfig {

    static final String url = "http://host.docker.internal:4317";

    public static Tracer initTracer() {
        // Create an OpenTelemetry instance
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(url) // OTLP Collector endpoint
                .build();


//        OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.builder()
//                .setEndpoint(url) // OTLP endpoint for metrics
//                .build();


//        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
//                .registerMetricReader(
//                        PeriodicMetricReader.builder(metricExporter)
//                                .setInterval(Duration.ofSeconds(1))
//                                .build()
//                )
//                .build();


        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                .build();


        // Build OpenTelemetry instance
        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
//                .setMeterProvider(meterProvider)
                .build();


        // Create an OpenTracing Tracer shim
        Tracer tracer = OpenTracingShim.createTracerShim(openTelemetry);

        // Register as global tracer
        GlobalTracer.registerIfAbsent(tracer);

        return tracer;
    }


}