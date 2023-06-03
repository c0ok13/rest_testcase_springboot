package com.example.rest_testcase_springboot.service.util;

//представление видов оплаты в зависимости от того на сколько большой или маленький счёт пришел к оплате
public enum PaymentLimit {
    PaymentWithHighLoyaltyLimit,
    PaymentWithLowLoyaltyLimit,
    PaymentWithDefaultLoyaltyLimit,
    Idle
}
