package com.example.rest_testcase_springboot.service;

import com.example.rest_testcase_springboot.repository.ClientRepository;
import com.example.rest_testcase_springboot.service.model.ClientBank;
import com.example.rest_testcase_springboot.service.util.PaymentLimit;
import com.example.rest_testcase_springboot.service.util.PaymentStatus;
import com.example.rest_testcase_springboot.service.util.PaymentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PaymentServiceImp implements PaymentService{

    //правильнее всего будет работать не напрямую с моделью клиента банка, а создать репозиторий в рамках которого будут происходить взаимодействия с клиентом
    private final ClientRepository clientRepository;

    //внутренняя константа, необходимая для корректного извлечения процентной части от значения
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    //указываем верхнюю границу для суммы к оплате, при равной или выше которой мы начинаем начислять максимальное количество бонусов по программе лояльности
    @Value("${discount.amount.limit.upper}")
    private BigDecimal upperDiscountLimit;

    //указываем нижнюю границу для суммы к оплате, при равной или ниже которой мы начинаем добавлять комиссию к итоговой сумме к оплате клиента
    @Value("${discount.amount.limit.lower}")
    private BigDecimal lowerDiscountLimit;

    //комиссия в процентном представлении
    @Value("${payment.percent.fee}")
    private Integer paymentFee;

    //наивысший процент для начисления бонусов
    @Value("${loyalty.percent.high}")
    private Integer highLoyaltyPercent;

    //через него будем контролировать на каком этапе обработке платежа мы находимся
    private PaymentStatus paymentStatus;
    private PaymentLimit paymentLimit = PaymentLimit.Idle;
    //добавляем наш репозиторий "виртуальной бд" к сервису
    @Autowired
    public PaymentServiceImp(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    //получение денежного баланса клиента
    @Override
    public BigDecimal getAmountOfMoneyInAccount() {
        return clientRepository.getWallet();
    }

    //получаение бонусного баланса
    @Override
    public BigDecimal getEarnedPointsInLoyaltySystem() {
        return clientRepository.getBonus();
    }

    //логика по проведению платежа
    @Override
    public PaymentStatus purchasePayment(PaymentType paymentType, BigDecimal amountInCart) {
        // инициализируем стандартные значения переменных и устанавливаем статус платежа ProcessingPayment
        paymentStatus = PaymentStatus.ProcessingPayment;
        paymentLimit = PaymentLimit.PaymentWithDefaultLoyaltyLimit;
        int percentOfPaymentToLoyalty = paymentType.getLoyaltyPercent();
        BigDecimal invoice = amountInCart;

        //проверяем на входящие отрицательные числа
        if(!checkOnPositiveDecimal(invoice)){
            return PaymentStatus.NegativeValue;
        }

        //проверяем хватет ли денег на счету клиента для оплаты
        if(notEnoughMoneyInAccount(invoice)){
            return PaymentStatus.NotEnoughMoneyOnTheAccount;
        }

        //проверка на верхнее и нижнее пограничное состояние
        edgePaymentCaseCheck(invoice);

        //если у нас paymentLimit изменился, на верхнее пограничное состояние, то изменяем процент начислений бонусов, если нижнее, то добавляем комиссию к итоговому выставленному счету
        switch (paymentLimit) {
            case PaymentWithHighLoyaltyLimit -> percentOfPaymentToLoyalty = highLoyaltyPercent;
            case PaymentWithLowLoyaltyLimit -> {
                invoice = invoice.add(getFee(invoice));
                //проверяем после добавления комиссии не случилось ли такое, что клиент не сможет оплатить покупку
                if(notEnoughMoneyInAccount(invoice)){
                    return PaymentStatus.NotEnoughMoneyOnTheAccount;
                }
            }
        }


        //добавление балллов на аккаунт
        addPointsToAccount(amountInCart, percentOfPaymentToLoyalty);

        //проведение оплаты и вычитание денег со счёта клиента
        debitingFunds(invoice);

        //устанавливаем окончательное состояние обработки платежа
        paymentStatus = PaymentStatus.EndProcessingPayment;
        paymentLimit = PaymentLimit.Idle;

        return PaymentStatus.PaymentProcessed;
    }

    //проверяем что у нас достаточно денег на счету, т.е. больше или равно сумме которая находится в корзине
    private boolean notEnoughMoneyInAccount(BigDecimal amountInCart) {
        return getAmountOfMoneyInAccount().compareTo(amountInCart) < 0;
    }

    //проверка на пограничные значения выставленного счета к оплате и установленных нами пределов
    private void edgePaymentCaseCheck(BigDecimal amountInCart) {
        if (amountInCart.compareTo(upperDiscountLimit) >= 0) {
            paymentLimit = PaymentLimit.PaymentWithHighLoyaltyLimit;
        } else if (amountInCart.compareTo(lowerDiscountLimit) <= 0) {
            paymentLimit = PaymentLimit.PaymentWithLowLoyaltyLimit;
        }
    }

    //получение комисии
    private BigDecimal getFee(BigDecimal amountInCart) {
        paymentStatus = PaymentStatus.AddingFee;
        return getPercentOfValue(amountInCart, paymentFee);
    }


    //добавление баллов на аккаунт пользователя
    private void addPointsToAccount(BigDecimal amountInCart, Integer percentage) {
        paymentStatus = PaymentStatus.AddingBonusesToLoyaltySystem;
        BigDecimal points = getPercentOfValue(amountInCart, percentage);
        clientRepository.setBonus(getEarnedPointsInLoyaltySystem().add(points));
    }

    //вычитание со счёта клиента представленной ему к оплате суммы
    private void debitingFunds(BigDecimal amountInCart) {
        paymentStatus = PaymentStatus.FinalCalculation;
        clientRepository.setWallet(getAmountOfMoneyInAccount().subtract(amountInCart));
    }

    //служебная функция для получения процентных долей числа с двойной точностью после запятой и "грубым" округлением, т.е. 0.207 у нас округлиться до 0.20
    private BigDecimal getPercentOfValue(BigDecimal value, Integer percent) {
        return value.multiply(BigDecimal.valueOf(percent)).divide(ONE_HUNDRED, 2, RoundingMode.DOWN);
    }

    //проверка числа на положительное
    private boolean checkOnPositiveDecimal(BigDecimal val){
        return val.compareTo(BigDecimal.ZERO) > 0;
    }
}
