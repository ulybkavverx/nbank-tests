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

public class DepositMoneyTest {
    private static final double MAX_DEPOSIT_AMOUNT = 5000.00;
    private static final double INVALID_DEPOSIT_AMOUNT = MAX_DEPOSIT_AMOUNT + 0.01;

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

        // ШАГ 3: открытие страницы авторизации
        Selenide.open("/login");

        // авторизация юзера через ui
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        // проверка успешной авторизации
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГ 4: переход на страницу депозита и клик
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        // проверка, что открылась страница депозита
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);

        // ШАГ 5: выбор акка для пополнения
        $(".account-selector").shouldBe(Condition.visible);
        // проверка наличия подсказки по умолчанию
        $(".account-selector").shouldHave(Condition.text("-- Choose an account --"));
        // выбираем созданный акк
        $(".account-selector").selectOptionByValue(String.valueOf(account.getId()));
        // проверка, что выбран именно созданный акк
        $(".account-selector").shouldHave(Condition.value(String.valueOf(account.getId())));

        // ШАГ 6: получение баланса акка ДО пополнения через api
        double balanceBefore = userSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == account.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 7: вводим случайную сумму депозита через ui
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amount));
        // проверка, что сумма корректна введена
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .shouldHave(Condition.value(String.valueOf(amount)));

        // ШАГ 8: нажимаем кнопку 💵 Deposit
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        // проверка алерта об успешном пополнении
        Alert alert = switchTo().alert();
        assertTrue(alert.getText().contains("✅ Successfully deposited"));
        // закрываем алерт
        alert.accept();

        // ШАГ 9: получение баланса ПОСЛЕ пополнения
        double balanceAfter = userSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == account.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 10: проверка, что баланс увеличился ровно на сумму депозита
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

        // ШАГ 3: открытие страницы авторизации
        Selenide.open("/login");

        // авторизация юзера через ui
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        // проверка успешной авторизации
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГ 4: переход на страницу депозита и клик
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        // проверка, что открылась страница депозита
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);

        // ШАГ 5: выбор акка для пополнения
        $(".account-selector").shouldBe(Condition.visible);
        // проверка наличия подсказки по умолчанию
        $(".account-selector").shouldHave(Condition.text("-- Choose an account --"));
        // выбираем созданный акк
        $(".account-selector").selectOptionByValue(String.valueOf(account.getId()));
        // проверка, что выбран именно созданный акк
        $(".account-selector").shouldHave(Condition.value(String.valueOf(account.getId())));

        // ШАГ 6: получение баланса акка ДО пополнения через api
        double balanceBefore = userSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == account.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 7: вводим макс суммы депозита через ui
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amount));
        // проверка, что сумма корректна введена
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .shouldHave(Condition.value(String.valueOf(amount)));

        // ШАГ 8: нажимаем кнопку 💵 Deposit
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        // проверка алерта об успешном пополнении
        Alert alert = switchTo().alert();
        assertTrue(alert.getText().contains("✅ Successfully deposited"));
        // закрываем алерт кнопкой ОК
        alert.accept();

        // ШАГ 9: получение баланса ПОСЛЕ пополнения
        double balanceAfter = userSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == account.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 10: проверка, что баланс увеличился ровно на сумму депозита
        assertEquals(balanceBefore + amount, balanceAfter, 0.001);
    }

    @Test
    public void userCannotDepositAmountAboveMaxTest() {
        // сумма, превышающая мак разрешенный депозит на 0.01
        double amount = INVALID_DEPOSIT_AMOUNT;

        // ШАГ 1: создание нового юзера через api
        CreateUserRequest user = AdminSteps.createUser();

        // создание объекта для выполнения запросов от имени нового юзера
        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword()
        );

        // ШАГ 2: создание акка для нового юзера через api
        CreateAccountResponse account = userSteps.createAccount();

        // ШАГ 3: открытие страницы авторизации
        Selenide.open("/login");

        // авторизация юзера через ui
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        // проверка успешной авторизации
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГ 4: переход на страницу депозита и клик
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
        // проверка, что открылась страница депозита
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);

        // ШАГ 5: выбор акка для пополнения
        $(".account-selector").shouldBe(Condition.visible);
        // проверка наличия подсказки по умолчанию
        $(".account-selector").shouldHave(Condition.text("-- Choose an account --"));
        // выбираем созданный акк
        $(".account-selector").selectOptionByValue(String.valueOf(account.getId()));
        // проверка, что выбран именно созданный акк
        $(".account-selector").shouldHave(Condition.value(String.valueOf(account.getId())));

        // ШАГ 6: получение баланса акка ДО пополнения через api
        double balanceBefore = userSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == account.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 7: ввод суммы, превыщающ макс разрешенный депозит
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .setValue(String.valueOf(amount));
        // проверка, что сумма корректна введена
        $(Selectors.byAttribute("placeholder", "Enter amount"))
                .shouldHave(Condition.value(String.valueOf(amount)));

        // ШАГ 8: нажимаем кнопку 💵 Deposit
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        // проверка алерта с ошибкой ❌ Please deposit less or equal to 5000$.
        Alert alert = switchTo().alert();
        assertTrue(alert.getText().contains("❌ Please deposit less or equal to 5000$."));
        // закрываем алерт кнопкой ОК
        alert.accept();

        // ШАГ 9: получение баланса ПОСЛЕ НЕудачной попытки пополнения
        double balanceAfter = userSteps.getAccounts().stream()
                .filter(acc -> acc.getId() == account.getId())
                .findFirst()
                .orElseThrow()
                .getBalance();

        // ШАГ 10: проверка, что баланс не изменился
        assertEquals(balanceBefore, balanceAfter, 0.001);
    }
}
