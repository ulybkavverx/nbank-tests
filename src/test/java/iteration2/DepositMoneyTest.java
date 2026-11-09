package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.Account;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.steps.UserSteps;

import static org.assertj.core.api.Assertions.within;

public class DepositMoneyTest extends BaseTest {

    // данные пользователя, от имени которого выполняются сценарии
    private static final String USERNAME = "kate0000";
    private static final String PASSWORD = "KateKate12!!";

    private UserSteps userSteps;

    @BeforeEach
    public void setUp() {
        userSteps = new UserSteps(USERNAME, PASSWORD);
    }

    // получение акк текущего юзера
    private Account getCurrentAccount() {
        return userSteps.getAccounts().getFirst();
    }

    // ТС 02-05: успешное пополнение обычной суммы
    @Test
    public void positiveDepositMoneyRandomAmountTest() {
        // акк до пополнения
        Account accountBefore = getCurrentAccount();
        double balanceBefore = accountBefore.getBalance();

        // генерация новой допустимой суммы
        double amount = RandomData.randomNonBoundaryDepositAmount();

        // пополнение акка
        userSteps.depositMoney(
                accountBefore.getId(),
                amount,
                HttpStatus.SC_OK
        );

        // получение баланса после пополнения
        double balanceAfter = getCurrentAccount().getBalance();

        // проверка, что баланс увеличился на сумму пополнения
        softly.assertThat(balanceAfter).isCloseTo(balanceBefore + amount, within(0.001));
    }

    // ТС 02-05: успешное пополнение допустимыми суммами
    @ParameterizedTest
    @ValueSource(doubles = {1, 0.01, 4999.99, 5000})
    public void positiveDepositMoneyBoundaryTest(double amount) {

        // акк до пополнения
        Account accountBefore = getCurrentAccount();
        double balanceBefore = accountBefore.getBalance();

        // пополнение акка с ожид статусом 200
        userSteps.depositMoney(
                accountBefore.getId(),
                amount,
                HttpStatus.SC_OK
        );

        // получение баланса после пополнения
        double balanceAfter = getCurrentAccount().getBalance();

        // проверка, что баланс увеличился на сумму пополнения
        softly.assertThat(balanceAfter).isCloseTo(balanceBefore + amount, within(0.001));
    }

    // TC 06–09: пополнение недопустимыми суммами
    @ParameterizedTest
    @ValueSource(doubles = {5000.01, 5001, 0, -1})
    public void depositMoneyWithInvalidAmountTest(double amount) {
        Account accountBefore = getCurrentAccount();
        double balanceBefore = accountBefore.getBalance();

        // ожид сервер отклонит запрос, ожид статус 400
        userSteps.depositMoney(
                accountBefore.getId(),
                amount,
                HttpStatus.SC_BAD_REQUEST
        );

        // проверка, что баланс не изменился
        double balanceAfter = getCurrentAccount().getBalance();

        softly.assertThat(balanceAfter).isCloseTo(balanceBefore, within(0.001));
    }

    // ТС 10: попытка депозита в чужой аккаунт
    @Test
    public void depositToAnotherUserAccountTest() {
        // авторизация второго пользователя petya345
        UserSteps anotherUserSteps = new UserSteps("petya345", "PetyaPetya123!");

        // получение аккаунта второго пользователя petya345
        Account anotherUserAccount = anotherUserSteps.getAccounts().getFirst();
        double balanceBefore = anotherUserAccount.getBalance();

        // генерация допустимой суммы депозита
        double amount = RandomData.randomDepositAmount();

        // попытка пополнить чужой баланс от имени первого юзера
        userSteps.depositMoney(
                anotherUserAccount.getId(),
                amount,
                HttpStatus.SC_FORBIDDEN
        );

        // баланс пользователя petya345 ПОСЛЕ попытки депозита
        double balanceAfter =
                anotherUserSteps.getAccounts()
                        .getFirst()
                        .getBalance();

        // проверка, что баланс пользователя petya345 не изменился
        softly.assertThat(balanceAfter).isCloseTo(balanceBefore, within(0.001));
    }

    // ТС 11: попытка депозита в несуществующий аккаунт
    @Test
    public void depositToNonExistingAccountTest() {
        Account accountBefore = getCurrentAccount();
        double balanceBefore = accountBefore.getBalance();

        // генерация несуществующ акка
        int nonExistAccountId = RandomData.getNonExistingAccountID();

        // генерация случайной допуст суммы
        double amount = RandomData.randomDepositAmount();

        // пользователь kate0000 пытается сделать депозит в несуществующий аккаунт
        userSteps.depositMoney(
                nonExistAccountId,
                amount,
                HttpStatus.SC_FORBIDDEN
        );

        // баланс пользователя kate0000 после депозита
        double balanceAfter = getCurrentAccount().getBalance();

        // проверка, что баланс не изменился
        softly.assertThat(balanceAfter).isCloseTo(balanceBefore, within(0.001));
    }
}
