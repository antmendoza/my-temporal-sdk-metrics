package com.antmendoza.temporal;

import com.antmendoza.temporal.codec.CryptCodec;
import com.antmendoza.temporal.temporal.rde.httpserver.RDEHttpServer;

import java.io.IOException;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {
        try {
            new RDEHttpServer(Collections.singletonList(new CryptCodec()), 8888).start();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
