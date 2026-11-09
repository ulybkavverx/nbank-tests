package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.Account;
import models.CreateUserRequest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;

public class TransferMoneyTest extends BaseTest {

    // шаги для отправителя и получателя перевода
    private UserSteps senderSteps;
    private UserSteps receiverSteps;

    // создание новых пользователей и авторизация перед каждым тестом
    @BeforeEach
    public void setUp() {
        CreateUserRequest sender = AdminSteps.createUser();
        senderSteps = new UserSteps(sender.getUsername(), sender.getPassword());

        CreateUserRequest receiver = AdminSteps.createUser();
        receiverSteps = new UserSteps(receiver.getUsername(), receiver.getPassword());

        // создание счетов(2 шт)
        senderSteps.createAccount();
        senderSteps.createAccount();
        receiverSteps.createAccount();
        receiverSteps.createAccount();

        for (Account account : senderSteps.getAccounts()) {
            senderSteps.fundAtLeast(account.getId(), RandomData.MAX_TRANSFER);
        }
    }

    // получение акка пользователя по его индексу в списке
    private Account getAccount(UserSteps userSteps, int accountIdx) {
        return userSteps.getAccounts().get(accountIdx);
    }

    // получение акка пользователя по его id, чтобы после перевода проверить именно тот же счет
    private Account getAccountById(UserSteps userSteps, int accountId) {
        return userSteps.getAccounts().stream()
                .filter(account -> account.getId() == accountId)
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Аккаунт с ID: " + accountId + " не найден"
                ));
    }

    // выполнение перевода от имени отправителя
    private void transfer(int senderAccountId, int receiverAccountId, double amount, int expectedStatusCode) {
        senderSteps.transferMoney(
                senderAccountId,
                receiverAccountId,
                amount,
                expectedStatusCode
        );
    }

    // проверка, что баланс изменился на ожид сумму
    private void assertBalanceEquals(double actualBalance, double expectedBalance, String description) {
        softly.assertThat(Math.abs(actualBalance - expectedBalance))
                .as(description)
                .isLessThanOrEqualTo(0.01);
    }

    // проверка, что после успешного перевода деньги списались со счета отправителя, добавились на счет получателя
    private void assertSuccessfulTransferBalances(
            UserSteps senderUser,
            Account senderBefore,
            UserSteps receiverUser,
            Account receiverBefore,
            double amount
    ) {
        double senderBalanceAfter = getAccountById(senderUser, senderBefore.getId()).getBalance();
        double receiverBalanceAfter = getAccountById(receiverUser, receiverBefore.getId()).getBalance();

        assertBalanceEquals(
                senderBalanceAfter,
                senderBefore.getBalance() - amount,
                "Баланс отправителя после перевода"
        );

        assertBalanceEquals(
                receiverBalanceAfter,
                receiverBefore.getBalance() + amount,
                "Баланс получателя после перевода"
        );
    }

    // проверка, что отклоненный перевод не изменил балансы счетов
    private void assertBalancesUnchanged(
            UserSteps senderUser,
            Account senderBefore,
            UserSteps receiverUser,
            Account receiverBefore
    ) {
        double senderBalanceAfter = getAccountById(senderUser, senderBefore.getId()).getBalance();
        double receiverBalanceAfter = getAccountById(receiverUser, receiverBefore.getId()).getBalance();

        assertBalanceEquals(
                senderBalanceAfter,
                senderBefore.getBalance(),
                "Баланс отправителя после отклонённого перевода"
        );

        assertBalanceEquals(
                receiverBalanceAfter,
                receiverBefore.getBalance(),
                "Баланс получателя после отклонённого перевода"
        );
    }

    // ТС 12: успешное пополнение обычной суммы
    @Test
    public void successfulTransferRegularAmountTest() {
        Account senderBefore = getAccount(senderSteps, 0);
        Account receiverBefore = getAccount(receiverSteps, 1);

        double amount = RandomData.randomNonBoundaryTransferAmount();

        transfer(senderBefore.getId(), receiverBefore.getId(), amount, HttpStatus.SC_OK);

        assertSuccessfulTransferBalances(
                senderSteps, senderBefore,
                receiverSteps, receiverBefore,
                amount
        );
    }

    // ТС 13-16: Успешный перевод граничных сумм
    @ParameterizedTest
    @ValueSource(doubles = {1, 0.01, 9999.99, 10_000})
    public void successfulTransferBoundaryAmountTest(double amount) {
        Account senderBefore = getAccount(senderSteps, 0);
        Account receiverBefore = getAccount(receiverSteps, 1);

        transfer(senderBefore.getId(), receiverBefore.getId(), amount, HttpStatus.SC_OK);

        assertSuccessfulTransferBalances(
                senderSteps, senderBefore,
                receiverSteps, receiverBefore,
                amount
        );
    }

    // ТС 17-20: Негативные тесты, невалидные граничные суммы
    @ParameterizedTest
    @ValueSource(doubles = {10001, 10000.01, 0, -1})
    public void invalidTransferTest(double amount) {
        Account senderBefore = getAccount(senderSteps, 0);
        Account receiverBefore = getAccount(receiverSteps, 1);

        transfer(senderBefore.getId(), receiverBefore.getId(), amount, HttpStatus.SC_BAD_REQUEST);

        assertBalancesUnchanged(
                senderSteps, senderBefore,
                receiverSteps, receiverBefore
        );
    }

    // ТС 21: Перевод суммы, равной балансу отправителя
    @Test
    public void transferAmountEqualSendersBalance() {
        Account senderBefore = getAccount(senderSteps, 0);
        Account receiverBefore = getAccount(receiverSteps, 1);

        double amount = senderBefore.getBalance();

        transfer(senderBefore.getId(), receiverBefore.getId(), amount, HttpStatus.SC_OK);

        assertSuccessfulTransferBalances(
                senderSteps, senderBefore,
                receiverSteps, receiverBefore,
                amount
        );
    }

    // ТС 22: Перевод суммы больше баланса отправителя
    @Test
    public void negativeTransferMoreThanBalanceTest() {
        Account senderBefore = getAccount(senderSteps, 0);
        Account receiverBefore = getAccount(receiverSteps, 1);

        double amount = senderBefore.getBalance() + 1;

        transfer(senderBefore.getId(), receiverBefore.getId(), amount, HttpStatus.SC_BAD_REQUEST);

        assertBalancesUnchanged(
                senderSteps, senderBefore,
                receiverSteps, receiverBefore
        );
    }

    // ТС 23: Перевод между своими аккаунтами
    @Test
    public void transferBetweenOwnAccountsTest() {
        Account senderBefore = getAccount(senderSteps, 1);
        Account receiverBefore = getAccount(senderSteps, 0);

        double amount = RandomData.randomNonBoundaryTransferAmount();

        transfer(senderBefore.getId(), receiverBefore.getId(), amount, HttpStatus.SC_OK);

        assertSuccessfulTransferBalances(
                senderSteps, senderBefore,
                senderSteps, receiverBefore,
                amount
        );
    }

    // ТС 24: Перевод на чужой аккаунт
    @Test
    public void transferToAnotherUserTest() {
        Account senderBefore = getAccount(senderSteps, 1);
        Account receiverBefore = getAccount(receiverSteps, 0);

        double amount = RandomData.randomNonBoundaryTransferAmount();

        transfer(senderBefore.getId(), receiverBefore.getId(), amount, HttpStatus.SC_OK);

        assertSuccessfulTransferBalances(
                senderSteps, senderBefore,
                receiverSteps, receiverBefore,
                amount
        );
    }
}
