package com.example.rest_testcase_springboot.service.model;

import com.example.rest_testcase_springboot.service.util.PaymentLimit;
import com.example.rest_testcase_springboot.service.util.PaymentStatus;
import com.example.rest_testcase_springboot.service.util.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


//обьектное представление нашего платежа
@Getter
@Setter
public class Payment {

    //тут храним сумму платежа которая находится в корзине
    private BigDecimal amountInCart;
    //тут храниться число к оплате
    private BigDecimal invoice;

    //храним статус платежа
    private PaymentStatus paymentStatus;

    //Состояния от которых могут зависеть модификации обработки платежа
    private PaymentLimit paymentLimit;

    //тип платежа который поступил
    private PaymentType paymentType;

    //процент от платежа который поступит в виде баллов в программе лояльности
    private int percentOfPaymentToLoyalty;

    //конструктор по которому создаем платеж
    public Payment(BigDecimal amountInCart, PaymentLimit paymentLimit, PaymentType paymentType) {
        this.amountInCart = amountInCart;
        this.invoice = amountInCart;
        this.paymentStatus = PaymentStatus.PROCESSING_PAYMENT;
        this.paymentLimit = paymentLimit;
        this.paymentType = paymentType;
        this.percentOfPaymentToLoyalty = paymentType.getLoyaltyPercent();
    }
}
