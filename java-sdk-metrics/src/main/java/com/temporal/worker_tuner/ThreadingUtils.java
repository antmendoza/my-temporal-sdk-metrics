package com.temporal.worker_tuner;

public class ThreadingUtils {
    public static void sleepThrowOnInterrupt(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
