package com.antmendoza.temporal.codec;

public record Customer(String customerId, String customerName) {



    public String wfId() {
        return "wfId "+customerId + '-' +customerName.replaceAll(" ", "_");
    }

}
