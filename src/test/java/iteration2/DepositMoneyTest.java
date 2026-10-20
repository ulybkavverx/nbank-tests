package iteration2;

import io.restassured.specification.RequestSpecification;
import iteration1.BaseTest;
import models.Account;
import models.DepositMoneyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.AccountRequester;
import requests.DepositMoneyRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.assertj.core.api.Assertions.within;

public class DepositMoneyTest extends BaseTest {

    // данные пользователя kate0000
    private final String username = "kate0000";
    private final String password = "KateKate12!!";

    // настройки запросов авторизованного юзера
    private RequestSpecification userRequestSpecification;

    // перед каждым тестом авторизация юзера
    @BeforeEach
    public void setup() {
        userRequestSpecification = RequestSpecs.authAsUser(username, password);
    }

    // получение акк текущего юзера
    private Account getCurrentAccount() {
        return new AccountRequester(
                userRequestSpecification,
                ResponseSpecs.requestReturnsOK()
        ).getAccounts().getFirst();
    }

    // пополнение акка
    private void makeDeposit(int accountID, double amount, int expectedStatusCode) {
        new DepositMoneyRequester(
                userRequestSpecification,
                ResponseSpecs.requestReturnsStatusCode(expectedStatusCode)
        ).post(
                DepositMoneyRequest.builder()
                        .id(accountID)
                        .balance(amount)
                        .build()
        );
    }

    // ТС 01-05: успешное пополнение допустимыми суммами
    @ParameterizedTest
    @ValueSource(doubles = {100, 1, 0.01, 4999.99, 5000})
    public void positiveDepositMoneyTest(double amount) {

        // акк до пополнения
        Account accountBefore = getCurrentAccount();
        double balanceBefore = accountBefore.getBalance();

        // пополнение акка с ожид статусом 200
        makeDeposit(accountBefore.getId(), amount, 200);

        // получение баланса после пополнения
        double balanceAfter = getCurrentAccount().getBalance();

        // проверка, что баланс увеличился на сумму пополнения
        softly.assertThat(balanceAfter).isCloseTo(balanceBefore + amount, within(0.001));
    }

    // TC 06–09: пополнение недопустимыми суммами
    @ParameterizedTest
    @ValueSource(doubles = {5000.01, 5001, 0, -1})
    public void negativeDepositMoneyTest(double amount) {
        Account accountBefore = getCurrentAccount();
        double balanceBefore = accountBefore.getBalance();

        // ожид сервер отклонит запрос, ожид статус 400
        makeDeposit(accountBefore.getId(), amount, 400);

        // проверка, что баланс не изменился
        double balanceAfter = getCurrentAccount().getBalance();

        softly.assertThat(balanceAfter).isCloseTo(balanceBefore, within(0.001));
    }

    // ТС 10: попытка депозита в чужой аккаунт
    @Test
    public void depositToAnotherUserAccountTest() {
        // авторизация второго пользователя petya345
        RequestSpecification anotherUserRequestSpec =
                RequestSpecs.authAsUser(
                        "petya345",
                        "PetyaPetya123!"
                );

        AccountRequester anotherUserAccountRequester =
                new AccountRequester(
                        anotherUserRequestSpec,
                        ResponseSpecs.requestReturnsOK()
                );

        // получение аккаунта второго пользователя petya345
        Account anotherUserAccount =
                anotherUserAccountRequester.getAccounts().getFirst();

        double balanceBefore = anotherUserAccount.getBalance();

        // попытка пополнить чужой баланс от имени первого юзера
        makeDeposit(anotherUserAccount.getId(), 100, 400); // тест падает, тк в ответе нужен 403 код

        // баланс пользователя petya345 ПОСЛЕ попытки депозита
        double balanceAfter =
                anotherUserAccountRequester.getAccounts()
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

        // несуществующий аккаунт
        int nonExistAccountId = 999999;

        // пользователь kate0000 пытается сделать депозит в несуществующий аккаунт
        makeDeposit(nonExistAccountId, 100, 400); // тест падает, тк в ответе нужен 403 код

        // баланс пользователя kate0000 после депозита
        double balanceAfter = getCurrentAccount().getBalance();

        // проверка, что баланс не изменился
        softly.assertThat(balanceAfter).isCloseTo(balanceBefore, within(0.001));
    }
}
