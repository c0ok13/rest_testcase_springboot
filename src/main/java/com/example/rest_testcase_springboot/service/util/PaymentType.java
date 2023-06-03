package com.example.rest_testcase_springboot.service.util;

//т.к. мы имеем 2 типа оплаты, то удобнее всего создать енам в рамках
// которого представим оба типа и сразу привяжем к ним процент который должен получать клиент
// за оплату, покупок

public enum PaymentType {
    Shop(10),
    Online(17);

    private final int loyaltyPercent;
    PaymentType(int loyaltyPercent) {
        this.loyaltyPercent = loyaltyPercent;
    }
    public int getLoyaltyPercent() {
        return loyaltyPercent;
    }
}
