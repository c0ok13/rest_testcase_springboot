package com.example.rest_testcase_springboot.service.util;

//перречесление всех возможных состояний платежа
public enum PaymentStatus {
    PROCESSING_PAYMENT,
    NOT_ENOUGH_MONEY_ON_THE_ACCOUNT,
    NEGATIVE_VALUE,
    PAYMENT_PROCESSED,
    ADDING_FEE,
    TRANSACTION_PROCESSING,
    ADDING_BONUSES_TO_LOYALTY_SYSTEM,
    END_PROCESSING_PAYMENT,
    VALIDATION_CHECK
}
