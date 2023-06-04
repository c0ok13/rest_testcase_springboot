package com.example.rest_testcase_springboot.service;

import com.example.rest_testcase_springboot.repository.ClientRepository;
import com.example.rest_testcase_springboot.service.model.Payment;
import com.example.rest_testcase_springboot.service.util.PaymentLimit;
import com.example.rest_testcase_springboot.service.util.PaymentStatus;
import com.example.rest_testcase_springboot.service.util.PaymentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PaymentServiceImp implements PaymentService {

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

    //получение бонусного баланса
    @Override
    public BigDecimal getEarnedPointsInLoyaltySystem() {
        return clientRepository.getBonus();
    }

    //логика по проведению платежа
    @Override
    public PaymentStatus purchasePayment(PaymentType paymentType, BigDecimal amountInCart) {
        // инициализируем наш платеж
        Payment payment = new Payment(amountInCart, PaymentLimit.PAYMENT_WITH_DEFAULT_LOYALTY_LIMIT, paymentType);

        //проверяем на входящие отрицательные числа
        if (!checkOnPositiveDecimal(payment)) {
            return PaymentStatus.NEGATIVE_VALUE;
        }

        //проверка на верхнее и нижнее пограничное состояние
        edgePaymentCaseCheck(payment);

        //проверяем хватает ли денег на счету клиента для оплаты
        if (notEnoughMoneyInAccount(payment)) {
            return PaymentStatus.NOT_ENOUGH_MONEY_ON_THE_ACCOUNT;
        }

        //добавление баллов на аккаунт
        addPointsToAccount(payment);

        //проведение оплаты и вычитание денег со счёта клиента
        debitingFunds(payment);

        //устанавливаем окончательное состояние обработки платежа
        payment.setPaymentStatus(PaymentStatus.END_PROCESSING_PAYMENT);

        return PaymentStatus.PAYMENT_PROCESSED;
    }

    //проверяем что у нас достаточно денег на счету, т.е. больше или равно сумме которая находится в корзине
    private boolean notEnoughMoneyInAccount(Payment payment) {
        payment.setPaymentStatus(PaymentStatus.VALIDATION_CHECK);
        return getAmountOfMoneyInAccount().compareTo(payment.getInvoice()) < 0;
    }

    //проверка на пограничные значения выставленного счета к оплате и установленных нами пределов
    private void edgePaymentCaseCheck(Payment payment) {
        if (payment.getAmountInCart().compareTo(upperDiscountLimit) >= 0) {
            payment.setPaymentLimit(PaymentLimit.PAYMENT_WITH_HIGH_LOYALTY_LIMIT);
        } else if (payment.getAmountInCart().compareTo(lowerDiscountLimit) <= 0) {
            payment.setPaymentLimit(PaymentLimit.PAYMENT_WITH_LOW_LOYALTY_LIMIT);
        }

        //если у нас paymentLimit изменился, на верхнее пограничное состояние, то изменяем процент начислений бонусов, если нижнее, то добавляем комиссию к итоговому выставленному счету
        switch (payment.getPaymentLimit()) {
            case PAYMENT_WITH_HIGH_LOYALTY_LIMIT -> payment.setPercentOfPaymentToLoyalty(highLoyaltyPercent);
            case PAYMENT_WITH_LOW_LOYALTY_LIMIT -> addFee(payment);
        }
    }

    //получение комиссии
    private BigDecimal getFeeFromCart(Payment payment) {
        return getPercentOfValue(payment.getAmountInCart(), paymentFee);
    }

    //получение комиссии
    private void addFee(Payment payment) {
        payment.setPaymentStatus(PaymentStatus.ADDING_FEE);
        payment.setInvoice(payment.getInvoice().add(getFeeFromCart(payment)));
    }

    //добавление баллов на аккаунт пользователя
    private void addPointsToAccount(Payment payment) {
        payment.setPaymentStatus(PaymentStatus.ADDING_BONUSES_TO_LOYALTY_SYSTEM);
        BigDecimal points = getPercentOfValue(payment.getAmountInCart(), payment.getPercentOfPaymentToLoyalty());
        clientRepository.setBonus(getEarnedPointsInLoyaltySystem().add(points));
    }

    //вычитание со счёта клиента представленной ему к оплате суммы
    private void debitingFunds(Payment payment) {
        payment.setPaymentStatus(PaymentStatus.TRANSACTION_PROCESSING);
        clientRepository.setWallet(getAmountOfMoneyInAccount().subtract(payment.getInvoice()));
    }

    //служебная функция для получения процентных долей числа с двойной точностью после запятой и "грубым" округлением, т.е. 0.207 у нас округлиться до 0.20
    private BigDecimal getPercentOfValue(BigDecimal value, Integer percent) {
        return value.multiply(BigDecimal.valueOf(percent)).divide(ONE_HUNDRED, 2, RoundingMode.DOWN);
    }

    //проверка числа на положительное
    private boolean checkOnPositiveDecimal(Payment payment) {
        payment.setPaymentStatus(PaymentStatus.VALIDATION_CHECK);
        return payment.getAmountInCart().compareTo(BigDecimal.ZERO) > 0;
    }
}
