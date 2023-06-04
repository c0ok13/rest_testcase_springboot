package com.example.rest_testcase_springboot.service.util;

//представление видов оплаты в зависимости от того на сколько большой или маленький счёт пришел к оплате
public enum PaymentLimit {
    PAYMENT_WITH_HIGH_LOYALTY_LIMIT,
    PAYMENT_WITH_LOW_LOYALTY_LIMIT,
    PAYMENT_WITH_DEFAULT_LOYALTY_LIMIT,
}
