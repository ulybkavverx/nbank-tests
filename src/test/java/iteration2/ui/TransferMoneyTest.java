package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.requests.steps.UserSteps;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.TransferPage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferMoneyTest extends BaseUiTest {
    private static final double INVALID_TRANSFER_AMOUNT = RandomData.MAX_TRANSFER + 0.01;
    private static final double TRANSFER_BALANCE_RESERVE = 1.00;

    @Test
    @UserSession(value = 2)
    public void senderCanTransferMoneyToRecipientsAccountTest() {
        // генерация случайного трансфера без учета границ с округлением до копеек
        double amount = RandomData.randomNonBoundaryTransferAmountRounded();

        // получение UserSteps отправителя и получателя
        UserSteps senderSteps = SessionStorage.getSteps(1);
        UserSteps recipientSteps = SessionStorage.getSteps(2);

        // создание счета отправителя и пополнение с запасом
        CreateAccountResponse senderAccount = senderSteps.createAccount();
        senderSteps.fundAtLeast((int) senderAccount.getId(), amount + TRANSFER_BALANCE_RESERVE);

        // создание счета получателя через api
        CreateAccountResponse receiverAccount = recipientSteps.createAccount();

        // генерация и назначение случайного имени получателя через api
        String recipientName = RandomData.randomValidFullName();
        recipientSteps.changeUserName(recipientName);

        // получение балансов обоих счетов ДО трансфера через api
        double senderBalanceBefore = senderSteps.getAccountBalance(senderAccount.getId());
        double recipientBalanceBefore = recipientSteps.getAccountBalance(receiverAccount.getId());

        // выполнение трансфера через ui
        new TransferPage().open()
                .checkPageIsOpened()
                .selectAccount(senderAccount.getId())
                .enterRecipientName(recipientName)
                .enterRecipientAccountNumber(String.valueOf(receiverAccount.getAccountNumber()))
                .enterAmount(amount)
                .confirmTransfer()
                .sendTransfer()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY.getMessage());

        // получение балансов обоих счетов ПОСЛЕ трансфера
        double senderBalanceAfter = senderSteps.getAccountBalance(senderAccount.getId());
        double recipientBalanceAfter = recipientSteps.getAccountBalance(receiverAccount.getId());

        // проверка изменения балансов ПОСЛЕ трансфера
        assertEquals(senderBalanceBefore - amount, senderBalanceAfter, 0.001);
        assertEquals(recipientBalanceBefore + amount, recipientBalanceAfter, 0.001);
    }

    @Test
    @UserSession(value = 2)
    public void senderCanTransferMaxAmountToRecipientsAccountTest() {
        // макс разрешенный трансфер
        double amount = RandomData.MAX_TRANSFER;

        // получение UserSteps отправителя и получателя
        UserSteps senderSteps = SessionStorage.getSteps(1);
        UserSteps recipientSteps = SessionStorage.getSteps(2);

        // создание счета отправителя через api и пополнение
        CreateAccountResponse senderAccount = senderSteps.createAccount();
        senderSteps.fundAtLeast((int) senderAccount.getId(), amount + TRANSFER_BALANCE_RESERVE);

        // создание счета получателя через api
        CreateAccountResponse receiverAccount = recipientSteps.createAccount();

        // генерация и назначение случайного имени получателя через api
        String recipientName = RandomData.randomValidFullName();
        recipientSteps.changeUserName(recipientName);

        // получение баланса обоих счетов ДО трансфера через api
        double senderBalanceBefore = senderSteps.getAccountBalance(senderAccount.getId());
        double recipientBalanceBefore = recipientSteps.getAccountBalance(receiverAccount.getId());

        // выполнение трансфера через ui
        new TransferPage().open()
                .checkPageIsOpened()
                .selectAccount(senderAccount.getId())
                .enterRecipientName(recipientName)
                .enterRecipientAccountNumber(String.valueOf(receiverAccount.getAccountNumber()))
                .enterAmount(amount)
                .confirmTransfer()
                .sendTransfer()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY.getMessage());

        // получение баланса обоих счетов ПОСЛЕ трансфера
        double senderBalanceAfter = senderSteps.getAccountBalance(senderAccount.getId());
        double recipientBalanceAfter = recipientSteps.getAccountBalance(receiverAccount.getId());

        // проверка изменения балансов ПОСЛЕ трансфера
        assertEquals(senderBalanceBefore - amount, senderBalanceAfter, 0.001);
        assertEquals(recipientBalanceBefore + amount, recipientBalanceAfter, 0.001);
    }

    @Test
    @UserSession(value = 2)
    public void senderCannotTransferMoreThanValidAmountTest() {
        // невалидная сумма трансфера
        double amount = INVALID_TRANSFER_AMOUNT;

        // получение UserSteps отправителя и получателя
        UserSteps senderSteps = SessionStorage.getSteps(1);
        UserSteps recipientSteps = SessionStorage.getSteps(2);

        // создание счета отправителя и пополнение
        CreateAccountResponse senderAccount = senderSteps.createAccount();
        senderSteps.fundAtLeast((int) senderAccount.getId(), amount);

        // создание счета получателя через api
        CreateAccountResponse receiverAccount = recipientSteps.createAccount();

        // генерация и назначение случайного имени получателя через api
        String recipientName = RandomData.randomValidFullName();
        recipientSteps.changeUserName(recipientName);

        // получение баланса обоих счетов ДО трансфера через api
        double senderBalanceBefore = senderSteps.getAccountBalance(senderAccount.getId());
        double recipientBalanceBefore = recipientSteps.getAccountBalance(receiverAccount.getId());

        // выполнение трансфера через ui
        new TransferPage().open()
                .checkPageIsOpened()
                .selectAccount(senderAccount.getId())
                .enterRecipientName(recipientName)
                .enterRecipientAccountNumber(String.valueOf(receiverAccount.getAccountNumber()))
                .enterAmount(amount)
                .confirmTransfer()
                .sendTransfer()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage());

        // получение баланса обоих счетов ПОСЛЕ трансфера
        double senderBalanceAfter = senderSteps.getAccountBalance(senderAccount.getId());
        double recipientBalanceAfter = recipientSteps.getAccountBalance(receiverAccount.getId());

        // проверка изменения балансов ПОСЛЕ трансфера
        assertEquals(senderBalanceBefore, senderBalanceAfter, 0.001);
        assertEquals(recipientBalanceBefore, recipientBalanceAfter, 0.001);
    }
}
