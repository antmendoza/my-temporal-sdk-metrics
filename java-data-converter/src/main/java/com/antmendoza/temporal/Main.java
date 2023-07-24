package com.antmendoza.temporal;

import com.antmendoza.temporal.codec.CryptCodec;
import com.antmendoza.temporal.codec.Customer;
import com.antmendoza.temporal.codec.EncryptedPayloadsActivity;
import com.antmendoza.temporal.temporal.rde.httpserver.RDEHttpServer;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) {
        try {

            System.out.println("Start... ");

            CompletableFuture.runAsync(() -> {
                try {

                    new EncryptedPayloadsActivity().createWorkflow(new Customer("3454345", "Temporal.io"));

                } catch (Exception e) {
                    e.printStackTrace();
                }

            });


            //new EncryptedPayloadsActivity().createWorkflow(new Customer("1235", "firstname2 surname2 lastSurname"));

            new RDEHttpServer(Collections.singletonList(new CryptCodec()), 8888).start();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
