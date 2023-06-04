package com.example.rest_testcase_springboot.controller;


import com.example.rest_testcase_springboot.service.PaymentService;
import com.example.rest_testcase_springboot.service.util.PaymentStatus;
import com.example.rest_testcase_springboot.service.util.PaymentType;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

//создаем RestAPI контроллер для обработки входных запросов
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class RestApiController {
    private final PaymentService paymentService;

    // маппинг на получение денег на счету у клиента
    @GetMapping("/money")
    public BigDecimal money() {
        return paymentService.getAmountOfMoneyInAccount();
    }

    // маппинг на получение баллов находящихся на счету у клиента
    @GetMapping("/bankAccountOfEMoney")
    public BigDecimal bankAccountOfEMoney() {
        return paymentService.getEarnedPointsInLoyaltySystem();
    }

    // маппинг на обработку платежа
    @GetMapping("/payment/{type}/{amount}")
    public PaymentStatus payment(@PathVariable("type") PaymentType paymentType, @PathVariable("amount") BigDecimal amount) {
        return paymentService.purchasePayment(paymentType, amount);
    }
}
