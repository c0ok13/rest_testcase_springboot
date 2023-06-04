package com.example.rest_testcase_springboot.service.model;

import com.example.rest_testcase_springboot.service.util.PaymentLimit;
import com.example.rest_testcase_springboot.service.util.PaymentStatus;
import com.example.rest_testcase_springboot.service.util.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Payment {
    BigDecimal amountInCart;
    BigDecimal invoice;
    PaymentStatus paymentStatus;
    PaymentLimit paymentLimit;
    PaymentType paymentType;
    int percentOfPaymentToLoyalty;

    public Payment(BigDecimal amountInCart, PaymentLimit paymentLimit, PaymentType paymentType) {
        this.amountInCart = amountInCart;
        this.invoice = amountInCart;
        this.paymentStatus = PaymentStatus.PROCESSING_PAYMENT;
        this.paymentLimit = paymentLimit;
        this.paymentType = paymentType;
        this.percentOfPaymentToLoyalty = paymentType.getLoyaltyPercent();
    }
}
