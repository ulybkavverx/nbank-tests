package iteration1.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;

import java.util.Map;

import static com.codeborne.selenide.Selenide.$;

public class LoginUserTest {
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
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        Selenide.open("/login");

        // ищем элемент на странице, в поле Username вводим имя админа
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        // ищем элемент на странице, в поле Password вводим пароль админа
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        // ищем кнопку Login и кликаем по ней
        $("button").click();

        // проверка, что работа прошла успешно, и мы попали на страницу с текстом Admin Panel
        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);
    }

    @Test
    public void userCanLoginWithCorrectDataTest() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));
    }
}
