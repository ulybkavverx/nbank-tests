package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositPage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositMoneyTest extends BaseUiTest {
    private static final double MAX_DEPOSIT_AMOUNT = 5000.00;
    private static final double INVALID_DEPOSIT_AMOUNT = MAX_DEPOSIT_AMOUNT + 0.01;

    @Test
    @UserSession
    public void userCanSuccessfullyDepositMoneyTest() {
        // генерация депозита, без учета границ
        double amount = RandomData.randomNonBoundaryDepositAmountRounded();

        // создание счета через api
        CreateAccountResponse account = SessionStorage.getSteps().createAccount();

        // получение баланса ДО пополнения через api
        double balanceBefore = SessionStorage.getSteps().getAccountBalance(account.getId());

        // пополнение счета через ui
        new DepositPage().open()
                .checkPageIsOpened()
                .selectAccount(account.getId())
                .enterAmount(amount)
                .deposit()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULLY.getMessage());

        // получение баланса ПОСЛЕ пополнения
        double balanceAfter = SessionStorage.getSteps().getAccountBalance(account.getId());

        // проверка, что баланс увеличился ровно на сумму депозита
        assertEquals(balanceBefore + amount, balanceAfter, 0.001);
    }

    @Test
    @UserSession
    public void userCanSuccessfullyDepositMaxAmountTest() {
        // макс разрешенная сумма депозита
        double amount = MAX_DEPOSIT_AMOUNT;

        // создание счета через api
        CreateAccountResponse account = SessionStorage.getSteps().createAccount();

        // получение баланса ДО пополнения через api
        double balanceBefore = SessionStorage.getSteps().getAccountBalance(account.getId());

        // пополнение счета через ui
        new DepositPage().open()
                .checkPageIsOpened()
                .selectAccount(account.getId())
                .enterAmount(amount)
                .deposit()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULLY.getMessage());

        // получение баланса ПОСЛЕ пополнения
        double balanceAfter = SessionStorage.getSteps().getAccountBalance(account.getId());

        //проверка, что баланс увеличился ровно на сумму депозита
        assertEquals(balanceBefore + amount, balanceAfter, 0.001);
    }

    @Test
    @UserSession
    public void userCannotDepositAmountAboveMaxTest() {
        // создание счета через api
        CreateAccountResponse account = SessionStorage.getSteps().createAccount();

        // получение баланса акка ДО пополнения через api
        double balanceBefore = SessionStorage.getSteps().getAccountBalance(account.getId());

        // пополнение счета через ui
        new DepositPage().open()
                .selectAccount(account.getId())
                .enterAmount(INVALID_DEPOSIT_AMOUNT)
                .deposit()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_LESS_OR_EQUALS_TO_5000$.getMessage());

        // получение баланса ПОСЛЕ НЕудачной попытки пополнения
        double balanceAfter = SessionStorage.getSteps().getAccountBalance(account.getId());

        // проверка, что баланс не изменился
        assertEquals(balanceBefore, balanceAfter, 0.001);
    }
}
