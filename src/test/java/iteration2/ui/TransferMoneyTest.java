package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;

import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransferMoneyTest {
    private static final double INVALID_TRANSFER_AMOUNT = RandomData.MAX_TRANSFER + 0.01;

    @BeforeAll
    public static void setupSelenoid() {
        // адрес удалённого браузера в Selenoid
        Configuration.remote = "http://localhost:4444/wd/hub";
        // адрес frontend-приложения, доступный из Docker
        Configuration.baseUrl = "http://192.168.3.179:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

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


        // ШАГ 3: открытие страницы авторизации
        Selenide.open("/login");

        // авторизация юзера-отправителя через ui
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(sender.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(sender.getPassword());
        $("button").click();

        // проверка успешной авторизации
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГ 4: переход на страницу трансфера и клик
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        // проверка, что открылась страница трансфера
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        // ШАГ 5: выбор акка для трансфера
        $(".account-selector").shouldBe(Condition.visible);
        // проверка наличия подсказки по умолчанию
        $(".account-selector").shouldHave(Condition.text("-- Choose an account --"));
        // выбираем созданный акк
        $(".account-selector").selectOptionByValue(String.valueOf(senderAccount.getId()));
        // проверка, что выбран именно созданный акк
        $(".account-selector").shouldHave(Condition.value(String.valueOf(senderAccount.getId())));

        // ШАГ 6: ввод имени получателя
        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue(recipientName);
        // проверка, что имя получателя введено корректно
        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .shouldHave(Condition.value(recipientName));

        // ШАГ 7: ввод номера акка юзера-получателя
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .setValue(String.valueOf(receiverAccount.getAccountNumber()));
        // проверка, что номер акка введен корректно
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .shouldHave(Condition.value(String.valueOf(receiverAccount.getAccountNumber())));


        // ШАГ 8: получение баланса акка отправителя ДО трансфера через api
        double senderBalanceBefore = senderSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == senderAccount.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 9: получение баланса акка получателя ДО трансфера через api
        double recipientBalanceBefore = recipientSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == receiverAccount.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 10: вводим случайную сумму трансфера через ui
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amount));
        // проверка, что сумма корректна введена
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .shouldHave(Condition.value(String.valueOf(amount)));

        // ШАГ 11: нажимаем на галочку, подтверждающая корректность веденных данных
        $(Selectors.byClassName("form-check-input")).click();
        // проверка, что галочка установлена
        $(Selectors.byClassName("form-check-input")).shouldBe(Condition.checked);

        // ШАГ 12: нажать кнопку 🚀 Send Transfer
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // проверка алерта об успешной операции
        Alert alert = switchTo().alert();
        assertTrue(alert.getText().contains("✅ Successfully transferred"));
        // закрываем алерт
        alert.accept();

        // ШАГ 13: получение баланса отправителя ПОСЛЕ трансфера
        double senderBalanceAfter = senderSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == senderAccount.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 14: получение баланса получателя ПОСЛЕ трансфера
        double recipientBalanceAfter = recipientSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == receiverAccount.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 15: проверка, что баланс отправителя уменьшился на сумму перевода
        assertEquals(senderBalanceBefore - amount, senderBalanceAfter, 0.001);

        // ШАГ 16: проверка, что баланс получателя увеличился на сумму перевода
        assertEquals(recipientBalanceBefore + amount, recipientBalanceAfter, 0.001);

        // ШАГ 17: нажать на кнопку Home
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
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

        // ШАГ 3: открытие страницы авторизации
        Selenide.open("/login");

        // авторизация юзера-отправителя через ui
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(sender.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(sender.getPassword());
        $("button").click();

        // проверка успешной авторизации
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГ 4: переход на страницу трансфера и клик
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        // проверка, что открылась страница трансфера
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        // ШАГ 5: выбор акка для трансфера
        $(".account-selector").shouldBe(Condition.visible);
        // проверка наличия подсказки по умолчанию
        $(".account-selector").shouldHave(Condition.text("-- Choose an account --"));
        // выбираем созданный акк
        $(".account-selector").selectOptionByValue(String.valueOf(senderAccount.getId()));
        // проверка, что выбран именно созданный акк
        $(".account-selector").shouldHave(Condition.value(String.valueOf(senderAccount.getId())));

        // ШАГ 6: ввод имени получателя
        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue(recipientName);
        // проверка, что имя получателя введено корректно
        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .shouldHave(Condition.value(recipientName));

        // ШАГ 7: ввод номера акка юзера-получателя
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .setValue(String.valueOf(receiverAccount.getAccountNumber()));
        // проверка, что номер акка введен корректно
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .shouldHave(Condition.value(String.valueOf(receiverAccount.getAccountNumber())));


        // ШАГ 8: получение баланса акка отправителя ДО трансфера через api
        double senderBalanceBefore = senderSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == senderAccount.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 9: получение баланса акка получателя ДО трансфера через api
        double recipientBalanceBefore = recipientSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == receiverAccount.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 10: вводим макс сумму трансфера через ui
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amount));
        // проверка, что сумма корректна введена
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .shouldHave(Condition.value(String.valueOf(amount)));

        // ШАГ 11: нажимаем на галочку, подтверждающая корректность веденных данных
        $(Selectors.byClassName("form-check-input")).click();
        // проверка, что галочка установлена
        $(Selectors.byClassName("form-check-input")).shouldBe(Condition.checked);

        // ШАГ 12: нажать кнопку 🚀 Send Transfer
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // проверка алерта об успешной операции
        Alert alert = switchTo().alert();
        assertTrue(alert.getText().contains("✅ Successfully transferred"));
        // закрываем алерт
        alert.accept();

        // ШАГ 13: получение баланса отправителя ПОСЛЕ трансфера
        double senderBalanceAfter = senderSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == senderAccount.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 14: получение баланса получателя ПОСЛЕ трансфера
        double recipientBalanceAfter = recipientSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == receiverAccount.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 15: проверка, что баланс отправителя уменьшился ровно на сумму перевода
        assertEquals(senderBalanceBefore - amount, senderBalanceAfter, 0.001);

        // ШАГ 16: проверка, что баланс получателя увеличился ровно на сумму перевода
        assertEquals(recipientBalanceBefore + amount, recipientBalanceAfter, 0.001);

        // ШАГ 17: нажать на кнопку Home
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
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

        // ШАГ 3: открытие страницы авторизации
        Selenide.open("/login");

        // авторизация юзера-отправителя через ui
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(sender.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(sender.getPassword());
        $("button").click();

        // ШАГ 4: переход на страницу трансфера и клик
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        // проверка, что открылась страница трансфера
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        // ШАГ 5: выбор акка для трансфера
        $(".account-selector").shouldBe(Condition.visible);
        // проверка наличия подсказки по умолчанию
        $(".account-selector").shouldHave(Condition.text("-- Choose an account --"));
        // выбираем созданный акк
        $(".account-selector").selectOptionByValue(String.valueOf(senderAccount.getId()));
        // проверка, что выбран именно созданный акк
        $(".account-selector").shouldHave(Condition.value(String.valueOf(senderAccount.getId())));

        // ШАГ 6: ввод имени получателя
        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .setValue(recipientName);
        // проверка, что имя получателя введено корректно
        $(Selectors.byAttribute("placeholder", "Enter recipient name"))
                .shouldHave(Condition.value(recipientName));

        // ШАГ 7: ввод номера акка юзера-получателя
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .setValue(String.valueOf(receiverAccount.getAccountNumber()));
        // проверка, что номер акка введен корректно
        $(Selectors.byAttribute("placeholder", "Enter recipient account number"))
                .shouldHave(Condition.value(String.valueOf(receiverAccount.getAccountNumber())));

        // ШАГ 8: получение баланса акка отправителя ДО трансфера через api
        double senderBalanceBefore = senderSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == senderAccount.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 9: получение баланса акка получателя ДО трансфера через api
        double recipientBalanceBefore = recipientSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == receiverAccount.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 10: вводим сумму больше макс допустимого значения через ui
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amount));
        // проверка, что сумма корректна введена
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .shouldHave(Condition.value(String.valueOf(amount)));

        // ШАГ 11: нажимаем на галочку, подтверждающая корректность веденных данных
        $(Selectors.byClassName("form-check-input")).click();
        // проверка, что галочка установлена
        $(Selectors.byClassName("form-check-input")).shouldBe(Condition.checked);


        // ШАГ 12: нажать кнопку 🚀 Send Transfer
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        // проверка алерта об отклоненной операции
        Alert alert = switchTo().alert();
        assertTrue(alert.getText().contains("❌ Error: Transfer amount cannot exceed 10000"));
        // закрываем алерт
        alert.accept();

        // ШАГ 13: получение баланса отправителя ПОСЛЕ трансфера
        double senderBalanceAfter = senderSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == senderAccount.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 14: получение баланса получателя ПОСЛЕ трансфера
        double recipientBalanceAfter = recipientSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == receiverAccount.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 15: проверка, что баланс отправителя не изменился
        assertEquals(senderBalanceBefore, senderBalanceAfter, 0.001);

        // ШАГ 16: проверка, что баланс получателя не изменился
        assertEquals(recipientBalanceBefore, recipientBalanceAfter, 0.001);

        // ШАГ 17: нажать на кнопку Home
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
    }
}
