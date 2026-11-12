package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import models.CreateUserRequest;
import models.CreateUserResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;

import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.junit.jupiter.api.Assertions.*;

public class ChangeUserNameTest {

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
    public void userCanChangeNameTest() {
        // ШАГ 1: создание нового юзера через api
        CreateUserRequest user = AdminSteps.createUser();

        // создание объекта для выполнения запросов от имени нового юзера
        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword()
        );

        // ШАГ 2: получение тек профиля через api
        CreateUserResponse profileBefore = userSteps.getProfile();

        // сохранение тек имени юзера
        String oldName = profileBefore.getName();
        // проверка, что у нового юзера имя еще не задано
        assertNull(oldName);

        // ШАГ 3: генерация нового валидного имени юзера
        String newName = RandomData.randomValidFullName();

        // ШАГ 4: открытие страницы авторизации
        Selenide.open("/login");

        // авторизация юзера через ui
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        // проверка успешной авторизации
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГ 5: переход на страницу ✏️ Edit Profile (смена имени)
        $(Selectors.byClassName("user-username")).click();
        // проверить, что открылась страница ✏️ Edit Profile
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);


        // ШАГ 6: ввод имени в строчку Enter new name
        $(Selectors.byAttribute("placeholder", "Enter new name")).setValue(newName);
        // проверка, что имя написано корректно
        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldHave(Condition.value(newName));

        // ШАГ 7: нажатие кнопки 💾 Save Changes
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        // проверка алерта об успешной операции
        Alert alert = switchTo().alert();
        assertTrue(alert.getText().contains("✅ Name updated successfully!"));
        // закрытие алерта
        alert.accept();

        // ШАГ 8: нажать на кнопку Home
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();

        // ШАГ 9: проверка, что на главной странице имя изменилось на новое
        $(Selectors.byClassName("welcome-text"))
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + newName + "!"));

        // ШАГ 10: получение обновленного профиля юзера через api
        CreateUserResponse profileAfter = userSteps.getProfile();
        // проверка, что новое имя сохранилось на сервере
        assertEquals(newName, profileAfter.getName());
    }

    @Test
    public void userCannotChangeNameWithSingleWordTest() {
        // ШАГ 1: создание нового юзера через api
        CreateUserRequest user = AdminSteps.createUser();

        // создание объекта для выполнения запросов от имени нового юзера
        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword()
        );

        // ШАГ 2: получение тек профиля через api
        CreateUserResponse profileBefore = userSteps.getProfile();

        // сохранение тек имени юзера
        String oldName = profileBefore.getName();
        // проверка, что у нового юзера имя еще не задано
        assertNull(oldName);

        // ШАГ 3: генерация нового НЕвалидного имени юзера с одним словом
        String newInvalidName = RandomData.randomSingleWordName();

        // ШАГ 4: открытие страницы авторизации
        Selenide.open("/login");

        // авторизация юзера через ui
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        // проверка успешной авторизации
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГ 5: переход на страницу ✏️ Edit Profile (смена имени)
        $(Selectors.byClassName("user-username")).click();
        // проверить, что открылась страница ✏️ Edit Profile
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);

        // ШАГ 6: ввод НЕвалидного имени в строчку Enter new name
        $(Selectors.byAttribute("placeholder", "Enter new name")).setValue(newInvalidName);
        // проверка, что имя написано корректно
        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldHave(Condition.value(newInvalidName));

        // ШАГ 7: нажатие кнопки 💾 Save Changes
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        // проверка алерта об НЕуспешной операции
        Alert alert = switchTo().alert();
        assertTrue(alert.getText().contains("Name must contain two words with letters only"));
        // закрытие алерта
        alert.accept();

        // ШАГ 8: нажать на кнопку Home
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();

        // ШАГ 9: проверка, что на главной странице имя НЕ изменилось
        $(Selectors.byClassName("welcome-text"))
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, noname!"));

        // ШАГ 10: получение обновленного профиля юзера через api
        CreateUserResponse profileAfter = userSteps.getProfile();
        // проверка, что имя НЕ изменилось на сервере
        assertEquals(oldName, profileAfter.getName());
    }

    @Test
    public void userCannotChangeNameWithDigitTest() {
        // ШАГ 1: создание нового юзера через api
        CreateUserRequest user = AdminSteps.createUser();

        // создание объекта для выполнения запросов от имени нового юзера
        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword()
        );

        // ШАГ 2: получение тек профиля через api
        CreateUserResponse profileBefore = userSteps.getProfile();

        // сохранение тек имени юзера
        String oldName = profileBefore.getName();
        // проверка, что у нового юзера имя еще не задано
        assertNull(oldName);

        // ШАГ 3: генерация нового НЕвалидного имени юзера с цифрой
        String newInvalidName = RandomData.randomFullNameWithDigit();

        // ШАГ 4: открытие страницы авторизации
        Selenide.open("/login");

        // авторизация юзера через ui
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        // проверка успешной авторизации
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // ШАГ 5: переход на страницу ✏️ Edit Profile (смена имени)
        $(Selectors.byClassName("user-username")).click();
        // проверить, что открылась страница ✏️ Edit Profile
        $(Selectors.byText("✏️ Edit Profile")).shouldBe(Condition.visible);

        // ШАГ 6: ввод НЕвалидного имени в строчку Enter new name
        $(Selectors.byAttribute("placeholder", "Enter new name")).setValue(newInvalidName);
        // проверка, что имя написано корректно
        $(Selectors.byAttribute("placeholder", "Enter new name"))
                .shouldHave(Condition.value(newInvalidName));

        // ШАГ 7: нажатие кнопки 💾 Save Changes
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        // проверка алерта об НЕуспешной операции
        Alert alert = switchTo().alert();
        assertTrue(alert.getText().contains("Name must contain two words with letters only"));
        // закрытие алерта
        alert.accept();

        // ШАГ 8: нажать на кнопку Home
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();

        // ШАГ 9: проверка, что на главной странице имя НЕ изменилось
        $(Selectors.byClassName("welcome-text"))
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, noname!"));

        // ШАГ 10: получение обновленного профиля юзера через api
        CreateUserResponse profileAfter = userSteps.getProfile();
        // проверка, что имя НЕ изменилось на сервере
        assertEquals(oldName, profileAfter.getName());
    }
}
