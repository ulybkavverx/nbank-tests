package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.TransferPage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferMoneyTest extends BaseUiTest {
    private static final double INVALID_TRANSFER_AMOUNT = RandomData.MAX_TRANSFER + 0.01;

    @Test
    public void senderCanTransferMoneyToRecipientsAccountTest() {
        // генерация случайного трансфера без учета границ
        double amount = RandomData.randomNonBoundaryTransferAmount();

        // ШАГ 1: создание юзера-отправителя через api
        CreateUserRequest sender = AdminSteps.createUser();

        // создание объекта для выполнения запросов от имени отправителя
        UserSteps senderSteps = new UserSteps(
                sender.getUsername(),
                sender.getPassword()
        );

        // создание акка отправителя
        CreateAccountResponse senderAccount = senderSteps.createAccount();
        // пополнение счета отправителя, чтобы хватило денег на перевод
        senderSteps.fundAtLeast((int) senderAccount.getId(), amount);

        // ШАГ 2: создание юзера-получателя через api
        CreateUserRequest recipient = AdminSteps.createUser();

        // создание объекта для выполнения запросов от имени получателя
        UserSteps recipientSteps = new UserSteps(
                recipient.getUsername(),
                recipient.getPassword()
        );

        // создание акка получателя
        CreateAccountResponse receiverAccount = recipientSteps.createAccount();

        // генерация случайного фи юзера получателя
        String recipientName = RandomData.randomValidFullName();

        // назначение имени получателя через api
        recipientSteps.changeUserName(recipientName);

        // ШАГ 3: получение баланса акка отправителя ДО трансфера через api
        double senderBalanceBefore = senderSteps.getAccountBalance(senderAccount.getId());

        // ШАГ 4: получение баланса акка получателя ДО трансфера через api
        double recipientBalanceBefore = recipientSteps.getAccountBalance(receiverAccount.getId());

        // ШАГ 5: авторизация отправителя
        authAsUser(sender);

        // ШАГ 6: переход на страницу трансфера
        TransferPage transferPage = new TransferPage().open().checkPageIsOpened();

        // ШАГ 7: выбор акка отправителя для трансфера
        transferPage.selectAccount(senderAccount.getId());

        // ШАГ 8: ввод имени получателя
        transferPage.enterRecipientName(recipientName);

        // ШАГ 9: ввод номера акка юзера-получателя
        transferPage.enterRecipientAccountNumber(String.valueOf(receiverAccount.getAccountNumber()));

        // ШАГ 10: вводим случайную сумму трансфера через ui
        transferPage.enterAmount(amount);

        // ШАГ 11: нажимаем на галочку, подтверждающая корректность веденных данных
        transferPage.confirmTransfer();

        // ШАГ 12: нажать кнопку 🚀 Send Transfer
        transferPage.sendTransfer().checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY.getMessage());

        // ШАГ 13: получение баланса отправителя ПОСЛЕ трансфера
        double senderBalanceAfter = senderSteps.getAccountBalance(senderAccount.getId());

        // ШАГ 14: получение баланса получателя ПОСЛЕ трансфера
        double recipientBalanceAfter = recipientSteps.getAccountBalance(receiverAccount.getId());

        // ШАГ 15: проверка, что баланс отправителя уменьшился на сумму перевода
        assertEquals(senderBalanceBefore - amount, senderBalanceAfter, 0.001);

        // ШАГ 16: проверка, что баланс получателя увеличился на сумму перевода
        assertEquals(recipientBalanceBefore + amount, recipientBalanceAfter, 0.001);
    }

    @Test
    public void senderCanTransferMaxAmountToRecipientsAccountTest() {
        // макс разрешенный трансфер
        double amount = RandomData.MAX_TRANSFER;

        // ШАГ 1: создание юзера-отправителя через api
        CreateUserRequest sender = AdminSteps.createUser();

        // создание объекта для выполнения запросов от имени отправителя
        UserSteps senderSteps = new UserSteps(
                sender.getUsername(),
                sender.getPassword()
        );

        // создание акка отправителя
        CreateAccountResponse senderAccount = senderSteps.createAccount();
        // пополнение счета отправителя, чтобы хватило денег на перевод
        senderSteps.fundAtLeast((int) senderAccount.getId(), amount);

        // ШАГ 2: создание юзера-получателя через api
        CreateUserRequest recipient = AdminSteps.createUser();

        // создание объекта для выполнения запросов от имени получателя
        UserSteps recipientSteps = new UserSteps(
                recipient.getUsername(),
                recipient.getPassword()
        );

        // создание акка получателя
        CreateAccountResponse receiverAccount = recipientSteps.createAccount();

        // генерация случайного фи юзера получателя
        String recipientName = RandomData.randomValidFullName();

        // назначение имени получателя через api
        recipientSteps.changeUserName(recipientName);

        // ШАГ 3: получение баланса акка отправителя ДО трансфера через api
        double senderBalanceBefore = senderSteps.getAccountBalance(senderAccount.getId());

        // ШАГ 4: получение баланса акка получателя ДО трансфера через api
        double recipientBalanceBefore = recipientSteps.getAccountBalance(receiverAccount.getId());

        // ШАГ 5: авторизация отправителя
        authAsUser(sender);

        // ШАГ 6: переход на страницу трансфера
        TransferPage transferPage = new TransferPage().open().checkPageIsOpened();

        // ШАГ 7: выбор акка отправителя для трансфера
        transferPage.selectAccount(senderAccount.getId());

        // ШАГ 8: ввод имени получателя
        transferPage.enterRecipientName(recipientName);

        // ШАГ 9: ввод номера акка юзера-получателя
        transferPage.enterRecipientAccountNumber(String.valueOf(receiverAccount.getAccountNumber()));

        // ШАГ 10: вводим макс сумму трансфера через ui
        transferPage.enterAmount(amount);

        // ШАГ 11: нажимаем на галочку, подтверждающая корректность веденных данных
        transferPage.confirmTransfer();

        // ШАГ 12: нажать кнопку 🚀 Send Transfer
        transferPage.sendTransfer().checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY.getMessage());

        // ШАГ 13: получение баланса отправителя ПОСЛЕ трансфера
        double senderBalanceAfter = senderSteps.getAccountBalance(senderAccount.getId());

        // ШАГ 14: получение баланса получателя ПОСЛЕ трансфера
        double recipientBalanceAfter = recipientSteps.getAccountBalance(receiverAccount.getId());

        // ШАГ 15: проверка, что баланс отправителя уменьшился ровно на сумму перевода
        assertEquals(senderBalanceBefore - amount, senderBalanceAfter, 0.001);

        // ШАГ 16: проверка, что баланс получателя увеличился ровно на сумму перевода
        assertEquals(recipientBalanceBefore + amount, recipientBalanceAfter, 0.001);
    }

    @Test
    public void senderCannotTransferMoreThanValidAmountTest() {
        // невалидная сумма трансфера
        double amount = INVALID_TRANSFER_AMOUNT;

        // ШАГ 1: создание юзера-отправителя через api
        CreateUserRequest sender = AdminSteps.createUser();

        // создание объекта для выполнения запросов от имени отправителя
        UserSteps senderSteps = new UserSteps(
                sender.getUsername(),
                sender.getPassword()
        );

        // создание акка отправителя
        CreateAccountResponse senderAccount = senderSteps.createAccount();
        // пополнение счета отправителя, чтобы хватило денег на перевод
        senderSteps.fundAtLeast((int) senderAccount.getId(), amount);

        // ШАГ 2: создание юзера-получателя через api
        CreateUserRequest recipient = AdminSteps.createUser();

        // создание объекта для выполнения запросов от имени получателя
        UserSteps recipientSteps = new UserSteps(
                recipient.getUsername(),
                recipient.getPassword()
        );

        // создание акка получателя
        CreateAccountResponse receiverAccount = recipientSteps.createAccount();

        // генерация случайного фи юзера получателя
        String recipientName = RandomData.randomValidFullName();

        // назначение имени получателя через api
        recipientSteps.changeUserName(recipientName);

        // ШАГ 3: получение баланса акка отправителя ДО трансфера через api
        double senderBalanceBefore = senderSteps.getAccountBalance(senderAccount.getId());

        // ШАГ 4: получение баланса акка получателя ДО трансфера через api
        double recipientBalanceBefore = recipientSteps.getAccountBalance(receiverAccount.getId());

        // ШАГ 5: авторизация отправителя
        authAsUser(sender);

        // ШАГ 6: переход на страницу трансфера
        TransferPage transferPage = new TransferPage().open().checkPageIsOpened();

        // ШАГ 7: выбор акка отправителя для трансфера
        transferPage.selectAccount(senderAccount.getId());

        // ШАГ 8: ввод имени получателя
        transferPage.enterRecipientName(recipientName);

        // ШАГ 9: ввод номера акка юзера-получателя
        transferPage.enterRecipientAccountNumber(String.valueOf(receiverAccount.getAccountNumber()));

        // ШАГ 10: вводим сумму больше макс допустимого значения через ui
        transferPage.enterAmount(amount);

        // ШАГ 11: нажимаем на галочку, подтверждающая корректность веденных данных
        transferPage.confirmTransfer();

        // ШАГ 12: нажать кнопку 🚀 Send Transfer
        transferPage.sendTransfer().checkAlertMessageAndAccept(BankAlert.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage());

        // ШАГ 13: получение баланса отправителя ПОСЛЕ трансфера
        double senderBalanceAfter = senderSteps.getAccountBalance(senderAccount.getId());

        // ШАГ 14: получение баланса получателя ПОСЛЕ трансфера
        double recipientBalanceAfter = recipientSteps.getAccountBalance(receiverAccount.getId());

        // ШАГ 15: проверка, что баланс отправителя не изменился
        assertEquals(senderBalanceBefore, senderBalanceAfter, 0.001);

        // ШАГ 16: проверка, что баланс получателя не изменился
        assertEquals(recipientBalanceBefore, recipientBalanceAfter, 0.001);
    }
}
