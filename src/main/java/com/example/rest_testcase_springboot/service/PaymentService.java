package com.example.rest_testcase_springboot.service;

import com.example.rest_testcase_springboot.service.util.PaymentStatus;
import com.example.rest_testcase_springboot.service.util.PaymentType;

import java.math.BigDecimal;

//по одному из основных принципов SOLID - Interface responsibility реализуем интерфейс нашего сервиса для дальнейшей работы
public interface PaymentService {
    BigDecimal getAmountOfMoneyInAccount();
    BigDecimal getEarnedPointsInLoyaltySystem();
    PaymentStatus purchasePayment(PaymentType paymentType, BigDecimal amountInCart);
}
