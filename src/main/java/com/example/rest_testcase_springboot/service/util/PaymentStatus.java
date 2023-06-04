package com.example.rest_testcase_springboot.service.util;

//перречесление всех возможных состояний платежа
public enum PaymentStatus {
    PROCESSING_PAYMENT(500),
    NOT_ENOUGH_MONEY_ON_THE_ACCOUNT(400),
    NEGATIVE_VALUE(400),
    PAYMENT_PROCESSED(200),
    ADDING_FEE(400),
    TRANSACTION_PROCESSING(400),
    ADDING_BONUSES_TO_LOYALTY_SYSTEM(400),
    END_PROCESSING_PAYMENT(400),
    VALIDATION_CHECK(400);

    private final int statusCode;
    PaymentStatus(int statusCode) {
        this.statusCode = statusCode;
    }
    public int getStatusCode() {
        return statusCode;
    }
}
