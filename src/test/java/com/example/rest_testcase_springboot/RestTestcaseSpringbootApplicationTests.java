package com.example.rest_testcase_springboot;

import com.example.rest_testcase_springboot.service.model.ClientBank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestTestcaseSpringbootApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientBank clientBank;

    @BeforeEach
    public void initTest() {
        clientBank.setWallet(BigDecimal.valueOf(5000));
        clientBank.setBonus(BigDecimal.ZERO);
    }

    //проверяем что базовый функционал получения данных о счете на кошельке работает
    @Test
    public void StartingWallet() throws Exception {
        mockMvc.perform(get("/api/money")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("5000")));
    }

    //проверяем что базовый функционал получения данных о счете на балловой системе работает
    @Test
    public void StartingLoyaltyPoints() throws Exception {
        mockMvc.perform(get("/api/bankAccountOfEMoney")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("0")));
    }

    //проверяем оплату по нижней границе, ключевое тут проверить что со счёта клиента было снято с комиссией, а баллы добавленные на счет не учитывали комиссию
    @Test
    public void LowerEdgeOfPaymentByOnlinePayment() throws Exception {
        mockMvc.perform(get("/api/payment/ONLINE/20")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("PAYMENT_PROCESSED")));

        mockMvc.perform(get("/api/money")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("4978.00")));

        mockMvc.perform(get("/api/bankAccountOfEMoney")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("3.40")));
    }

    // проверяем оплату по верхней границе через магазин, ключевое тут проверить что количество баллов получаемых пользователем за оплату повышено
    @Test
    public void HigherEdgeOfPaymentByShopPayment() throws Exception {
        mockMvc.perform(get("/api/payment/SHOP/300")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("PAYMENT_PROCESSED")));

        mockMvc.perform(get("/api/money")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("4700")));

        mockMvc.perform(get("/api/bankAccountOfEMoney")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("90.00")));
    }


    //проверяем оплату находящуюся в рамках оплаты не имеющих каких-либо модификаций
    @Test
    public void PaymentWithDefaultLimitByShop() throws Exception {
        mockMvc.perform(get("/api/payment/SHOP/100")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("PAYMENT_PROCESSED")));

        mockMvc.perform(get("/api/money")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("4900")));

        mockMvc.perform(get("/api/bankAccountOfEMoney")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("10.00")));
    }

    //проверяем оплату превышающую баланс клиента
    @Test
    public void PaymentExceedsBalance() throws Exception {
        mockMvc.perform(get("/api/payment/SHOP/6000")).andDo(print()).andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("NOT_ENOUGH_MONEY_ON_THE_ACCOUNT")));

        mockMvc.perform(get("/api/money")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("5000")));

        mockMvc.perform(get("/api/bankAccountOfEMoney")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("0")));
    }

    //проверяем на отрицательное число выставленное к оплате
    @Test
    public void PaymentWithNegativeValue() throws Exception {
        mockMvc.perform(get("/api/payment/SHOP/-100")).andDo(print()).andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("NEGATIVE_VALUE")));

        mockMvc.perform(get("/api/money")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("5000")));

        mockMvc.perform(get("/api/bankAccountOfEMoney")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("0")));
    }


    //проверяем случай когда у клиента не хватит денег из-за добавления комиссии
    @Test
    public void PaymentWithFeeGreaterThanBalance() throws Exception {
        mockMvc.perform(get("/api/payment/SHOP/4990")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("PAYMENT_PROCESSED")));

        mockMvc.perform(get("/api/payment/SHOP/10")).andDo(print()).andExpect(status().is(400))
                .andExpect(content().string(containsString("NOT_ENOUGH_MONEY_ON_THE_ACCOUNT")));

        mockMvc.perform(get("/api/money")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("10")));

        mockMvc.perform(get("/api/bankAccountOfEMoney")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("1497.00")));
    }

}
