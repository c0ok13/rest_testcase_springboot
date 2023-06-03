package com.example.rest_testcase_springboot.service.util;

//перречесление всех возможных состояний платежа
public enum PaymentStatus {
    ProcessingPayment,
    NotEnoughMoneyOnTheAccount,
    NegativeValue,
    PaymentProcessed,
    AddingFee,
    FinalCalculation,
    AddingBonusesToLoyaltySystem,
    EndProcessingPayment;
}
