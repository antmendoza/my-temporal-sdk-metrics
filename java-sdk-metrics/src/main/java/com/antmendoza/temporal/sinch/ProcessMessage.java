package com.antmendoza.temporal.sinch;


import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface ProcessMessage {


    public String process(String message);
}
