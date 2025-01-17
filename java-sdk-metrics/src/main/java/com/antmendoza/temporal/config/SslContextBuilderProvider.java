package com.antmendoza.temporal.config;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.temporal.serviceclient.SimpleSslContextBuilder;

import java.io.InputStream;

public class SslContextBuilderProvider {


    private final TemporalProperties properties;

    public SslContextBuilderProvider() {
        this.properties = new TemporalProperties();
    }

    public SslContext getSslContext() {


        if(properties.isTemporalServerLocalhost()){
            return null;
        }


        try {
            InputStream clientCert = getClass().getResourceAsStream(properties.getTemporalCertLocation());
            InputStream clientKey = getClass().getResourceAsStream(properties.getTemporalKeyLocation());
            return SimpleSslContextBuilder.forPKCS8(clientCert, clientKey).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    public String getTargetEndpoint() {
        String targetEndpoint = properties.getTemporalTargetEndpoint();
        return targetEndpoint;
    }

    public String getNamespace() {
        // Your registered namespace.
        // String namespace = System.getenv("TEMPORAL_NAMESPACE");
        String namespace = properties.getTemporalNamespace();

        return namespace;
    }


}
