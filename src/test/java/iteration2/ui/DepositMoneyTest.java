package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositPage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositMoneyTest extends BaseUiTest {
    private static final double MAX_DEPOSIT_AMOUNT = 5000.00;
    private static final double INVALID_DEPOSIT_AMOUNT = MAX_DEPOSIT_AMOUNT + 0.01;

    @Test
    public void userCanSuccessfullyDepositMoneyTest() {
        // генерация депозита, без учета границ
        double amount = RandomData.randomNonBoundaryDepositAmount();

        // ШАГ 1: создание нового юзера через api
        CreateUserRequest user = AdminSteps.createUser();

        // создание объекта для выполнения запросов от имени нового юзера
        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword()
        );

        // ШАГ 2: создание акка для нового юзера через api
        CreateAccountResponse account = userSteps.createAccount();

        // ШАГ 3: получение баланса акка ДО пополнения через api
        double balanceBefore = userSteps.getAccountBalance(account.getId());

        // ШАГ 4: авторизация
        authAsUser(user);

        // ШАГ 5: переход на страницу депозита и проверка, что она открылась
        DepositPage depositPage = new DepositPage().open().checkPageIsOpened();

        // ШАГ 6: выбор акка для пополнения через ui
        depositPage.selectAccount(account.getId());

        // ШАГ 7: ввод случайную сумму депозита через ui
        depositPage.enterAmount(amount).deposit().checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULLY.getMessage());

        // ШАГ 8: получение баланса ПОСЛЕ пополнения
        double balanceAfter = userSteps.getAccountBalance(account.getId());

        // ШАГ 9: проверка, что баланс увеличился ровно на сумму депозита
        assertEquals(balanceBefore + amount, balanceAfter, 0.001);
    }

    @Test
    public void userCanSuccessfullyDepositMaxAmountTest() {
        // макс разрешенная сумма депозита
        double amount = MAX_DEPOSIT_AMOUNT;

        // ШАГ 1: создание нового юзера через api
        CreateUserRequest user = AdminSteps.createUser();

        // создание объекта для выполнения запросов от имени нового юзера
        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword()
        );

        // ШАГ 2: создание акка для нового юзера через api
        CreateAccountResponse account = userSteps.createAccount();

        // ШАГ 3: получение баланса акка ДО пополнения через api
        double balanceBefore = userSteps.getAccountBalance(account.getId());

        // ШАГ 4: авторизация
        authAsUser(user);

        // ШАГ 5: переход на страницу депозита
        DepositPage depositPage = new DepositPage().open().checkPageIsOpened();

        // ШАГ 6: выбор акка для пополнения через ui
        depositPage.selectAccount(account.getId());

        // ШАГ 7: ввод макс суммы депозита через ui
        depositPage.enterAmount(amount).deposit().checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULLY.getMessage());

        // ШАГ 8: получение баланса ПОСЛЕ пополнения
        double balanceAfter = userSteps.getAccountBalance(account.getId());

        // ШАГ 10: проверка, что баланс увеличился ровно на сумму депозита
        assertEquals(balanceBefore + amount, balanceAfter, 0.001);
    }

    @Test
    public void userCannotDepositAmountAboveMaxTest() {
        // ШАГ 1: создание нового юзера через api
        CreateUserRequest user = AdminSteps.createUser();

        // создание объекта для выполнения запросов от имени нового юзера
        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword()
        );

        // ШАГ 2: создание акка для нового юзера через api
        CreateAccountResponse account = userSteps.createAccount();

        // ШАГ 3: получение баланса акка ДО пополнения через api
        double balanceBefore = userSteps.getAccountBalance(account.getId());

        // ШАГ 4: авторизация
        authAsUser(user);

        // ШАГ 5: переход на страницу депозита
        DepositPage depositPage = new DepositPage().open().checkPageIsOpened();

        // ШАГ 6: выбор акка для пополнения через ui
        depositPage.selectAccount(account.getId());

        // ШАГ 7: ввод суммы, превыщающ макс разрешенный депозит
        depositPage.enterAmount(INVALID_DEPOSIT_AMOUNT)
                .deposit().checkAlertMessageAndAccept(BankAlert.DEPOSIT_LESS_OR_EQUALS_TO_5000$.getMessage());

        // ШАГ 8: получение баланса ПОСЛЕ НЕудачной попытки пополнения
        double balanceAfter = userSteps.getAccountBalance(account.getId());

        // ШАГ 9: проверка, что баланс не изменился
        assertEquals(balanceBefore, balanceAfter, 0.001);
    }
}
