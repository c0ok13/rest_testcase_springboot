package com.example.rest_testcase_springboot.service.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;

//делаем представление нашего условного клиента в виде класса, данные о кошельке и бонусном счете будем брать из проперти файла
@Getter
@Setter
public class ClientBank {
    @Value("${test.client.wallet}")
    private BigDecimal wallet;

    @Value("${test.client.bonus}")
    private BigDecimal bonus;

}
