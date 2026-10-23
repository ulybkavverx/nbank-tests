package iteration2;

import io.restassured.specification.RequestSpecification;
import iteration1.BaseTest;
import models.Account;
import models.TransferMoneyRequest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.AccountRequester;
import requests.TransferMoneyRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class TransferMoneyTest extends BaseTest {

    // данные пользователя-отправителя
    private final String username = "kate0000";
    private final String password = "KateKate12!!";

    // данные пользователя-получателя
    private final String usernameB = "kate19981";
    private final String passwordB = "KateTest19981!";

    // настройки запросов обоих пользователей
    private RequestSpecification senderRequestSpecification;
    private RequestSpecification receiverRequestSpecification;

    // авторизация обоих юзеров перед каждым тестом
    @BeforeEach
    public void setup() {
        senderRequestSpecification = RequestSpecs.authAsUser(username, password);
        receiverRequestSpecification = RequestSpecs.authAsUser(usernameB, passwordB);
    }

    // получение аккаунта пользователя по индексу
    private Account getAccount(RequestSpecification requestSpecification, int accountIdx) {
        return new AccountRequester(
                requestSpecification,
                ResponseSpecs.requestReturnsOK()
        ).getAccounts().get(accountIdx);
    }

    // выполнение перевода и проверка ожид результата
    private void transfer(int senderAccountId, int receiverAccountId, double amount, int expectedStatusCode) {
        new TransferMoneyRequester(
                senderRequestSpecification,
                ResponseSpecs.requestReturnsStatusCode(expectedStatusCode)
        ).post(
                TransferMoneyRequest.builder()
                        .senderAccountId(senderAccountId)
                        .receiverAccountId(receiverAccountId)
                        .amount(amount)
                        .build()
        );
    }

    // проверка, что баланс изменился на ожид сумму
    private void assertBalanceEquals(double actualBalance, double expectedBalance, String description) {
        softly.assertThat(Math.abs(actualBalance - expectedBalance))
                .as(description)
                .isLessThanOrEqualTo(0.01);
    }

    // ТС 12-16: Успешный перевод граничных сумм
    @ParameterizedTest
    @ValueSource(doubles = {100, 1, 0.01, 9999.99, 10_000})
    public void successfulTransferBoundaryAmountTest(double amount) {

        // Первый аккаунт отправителя и второй аккаунт получателя
        Account senderBefore = getAccount(senderRequestSpecification, 0);
        Account receiverBefore = getAccount(receiverRequestSpecification, 1);

        double senderBalanceBefore = senderBefore.getBalance();
        double receiverBalanceBefore = receiverBefore.getBalance();

        // трансфер
        transfer(senderBefore.getId(), receiverBefore.getId(), amount, HttpStatus.SC_OK);

        // получение балансов обоих юзеров после трансфера
        double senderBalanceAfter = getAccount(senderRequestSpecification, 0).getBalance();
        double receiverBalanceAfter = getAccount(receiverRequestSpecification, 1).getBalance();

        // проверка, что у отправителя списалась ожид сумма, у получателя зачислилась ожид сумма
        assertBalanceEquals(senderBalanceAfter, senderBalanceBefore - amount, "Баланс отправителя после перевода");
        assertBalanceEquals(receiverBalanceAfter, receiverBalanceBefore + amount, "Баланс получателя после перевода");
    }

    // ТС 17-20: Негативные тесты, невалидные граничные суммы
    @ParameterizedTest
    @ValueSource(doubles = {10001, 10000.01, 0, -1})
    public void invalidTransferTest(double amount) {
        Account senderBefore = getAccount(senderRequestSpecification, 0);
        Account receiverBefore = getAccount(receiverRequestSpecification, 1);

        double senderBalanceBefore = senderBefore.getBalance();
        double receiverBalanceBefore = receiverBefore.getBalance();

        transfer(senderBefore.getId(), receiverBefore.getId(), amount, HttpStatus.SC_BAD_REQUEST);

        double senderBalanceAfter =
                getAccount(senderRequestSpecification, 0).getBalance();

        double receiverBalanceAfter =
                getAccount(receiverRequestSpecification, 1).getBalance();

        // при отклонненом переводе балансы не должны измениться
        assertBalanceEquals(senderBalanceAfter, senderBalanceBefore, "Баланс отправителя после перевода");
        assertBalanceEquals(receiverBalanceAfter, receiverBalanceBefore, "Баланс получателя после перевода");

    }

    // ТС 21: Перевод суммы, равной балансу отправителя
    @Test
    public void transferAmountEqualSendersBalance() {
        Account senderBefore = getAccount(senderRequestSpecification, 0);
        Account receiverBefore = getAccount(receiverRequestSpecification, 1);

        double senderBalanceBefore = senderBefore.getBalance();
        double receiverBalanceBefore = receiverBefore.getBalance();

        double amount = senderBalanceBefore;

        transfer(senderBefore.getId(), receiverBefore.getId(), amount, HttpStatus.SC_OK);

        double senderBalanceAfter =
                getAccount(senderRequestSpecification, 0).getBalance();

        double receiverBalanceAfter =
                getAccount(receiverRequestSpecification, 1).getBalance();

        assertBalanceEquals(senderBalanceAfter, senderBalanceBefore - amount, "Баланс отправителя после перевода");
        assertBalanceEquals(receiverBalanceAfter, receiverBalanceBefore + amount, "Баланс получателя после перевода");
    }

    // ТС 22: Перевод суммы больше баланса отправителя
    @Test
    public void negativeTransferMoreThanBalanceTest() {
        Account senderBefore = getAccount(senderRequestSpecification, 0);
        Account receiverBefore = getAccount(receiverRequestSpecification, 1);

        double senderBalanceBefore = senderBefore.getBalance();
        double receiverBalanceBefore = receiverBefore.getBalance();

        double amount = senderBalanceBefore + 1;

        transfer(senderBefore.getId(), receiverBefore.getId(), amount, HttpStatus.SC_BAD_REQUEST);

        double senderBalanceAfter =
                getAccount(senderRequestSpecification, 0).getBalance();

        double receiverBalanceAfter =
                getAccount(receiverRequestSpecification, 1).getBalance();

        assertBalanceEquals(senderBalanceAfter, senderBalanceBefore, "Баланс отправителя после перевода");
        assertBalanceEquals(receiverBalanceAfter, receiverBalanceBefore, "Баланс получателя после перевода");
    }

    // ТС 23: Перевод между своими аккаунтами
    @Test
    public void transferBetweenOwnAccountsTest() {
        // перевод со второго акка на первый
        Account senderBefore = getAccount(senderRequestSpecification, 1);
        Account receiverBefore = getAccount(senderRequestSpecification, 0);

        double senderBalanceBefore = senderBefore.getBalance();
        double receiverBalanceBefore = receiverBefore.getBalance();

        double amount = 100;

        transfer(senderBefore.getId(), receiverBefore.getId(), amount, HttpStatus.SC_OK);

        double senderBalanceAfter =
                getAccount(senderRequestSpecification, 1).getBalance();

        double receiverBalanceAfter =
                getAccount(senderRequestSpecification, 0).getBalance();

        assertBalanceEquals(senderBalanceAfter, senderBalanceBefore - amount, "Баланс отправителя после перевода");
        assertBalanceEquals(receiverBalanceAfter, receiverBalanceBefore + amount, "Баланс получателя после перевода");
    }

    // ТС 24: Перевод на чужой аккаунт
    @Test
    public void transferToAnotherUserTest() {
        // второй акк отправителя, первый акк получателя
        Account senderBefore = getAccount(senderRequestSpecification, 1);
        Account receiverBefore = getAccount(receiverRequestSpecification, 0);

        double senderBalanceBefore = senderBefore.getBalance();
        double receiverBalanceBefore = receiverBefore.getBalance();

        double amount = 100;

        transfer(senderBefore.getId(), receiverBefore.getId(), amount, HttpStatus.SC_OK);

        double senderBalanceAfter =
                getAccount(senderRequestSpecification, 1).getBalance();

        double receiverBalanceAfter =
                getAccount(receiverRequestSpecification, 0).getBalance();

        assertBalanceEquals(senderBalanceAfter, senderBalanceBefore - amount, "Баланс отправителя после перевода");
        assertBalanceEquals(receiverBalanceAfter, receiverBalanceBefore + amount, "Баланс получателя после перевода");
    }
}
