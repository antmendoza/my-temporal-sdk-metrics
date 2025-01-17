package com.antmendoza.temporal.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TemporalProperties {


    private boolean temporalServerLocalhost;
    private String temporalKeyLocation;
    private String temporalCertLocation;
    private String temporalNamespace = "default";
    private String temporalTargetEndpoint= "localhost:7233";

    public TemporalProperties() {

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("temporal.properties")) {

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);


            this.temporalServerLocalhost = Boolean.parseBoolean(prop.getProperty("temporal_server_localhost"));


            System.out.println(">>>>> temporalServerLocalhost: " + prop.getProperty("temporal_server_localhost"));
            System.out.println(">>>>> temporalServerLocalhost: " + this.temporalServerLocalhost);

            if(!this.temporalServerLocalhost){

                this.temporalKeyLocation = prop.getProperty("temporal_key_location");
                this.temporalCertLocation = prop.getProperty("temporal_cert_location");
                this.temporalNamespace = prop.getProperty("temporal_namespace");
                this.temporalTargetEndpoint = prop.getProperty("temporal_target_endpoint");
            }

        } catch (IOException ex) {

            new RuntimeException(ex);
        }

    }


    public String getTemporalKeyLocation() {
        return temporalKeyLocation;
    }

    public String getTemporalCertLocation() {
        return temporalCertLocation;
    }

    public String getTemporalNamespace() {
        return temporalNamespace;
    }

    public String getTemporalTargetEndpoint() {
        return temporalTargetEndpoint;
    }

    public boolean isTemporalServerLocalhost() {
        return temporalServerLocalhost;
    }
}
