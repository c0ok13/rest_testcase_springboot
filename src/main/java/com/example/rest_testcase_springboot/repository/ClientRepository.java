package com.example.rest_testcase_springboot.repository;

import com.example.rest_testcase_springboot.service.model.ClientBank;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

// создаем репозиторий в рамках которого будем и делать манипуляцию с клиентом банка
@Repository
@AllArgsConstructor
public class ClientRepository {

    //добавляем нашего клиента в репо
    private final ClientBank clientBank;

    //геттеры, сеттеры по клиенту
    public BigDecimal getWallet() {
        return clientBank.getWallet();
    }

    public void setWallet(BigDecimal value) {
        clientBank.setWallet(value);
    }

    public BigDecimal getBonus() {
        return clientBank.getBonus();
    }

    public void setBonus(BigDecimal value) {
        clientBank.setBonus(value);
    }
}
