package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.ChangeUserNamePage;
import ui.pages.UserDashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ChangeUserNameTest extends BaseUiTest {
    @Test
    public void userCanSuccessfullyChangeNameTest() {
        // ШАГ 1: создание нового юзера через api
        CreateUserRequest user = AdminSteps.createUser();

        // создание объекта для выполнения запросов от имени нового юзера
        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword()
        );

        // получение тек профиля через api
        CreateUserResponse profileBefore = userSteps.getProfile();

        // проверка, что у нового юзера имя еще не задано
        assertNull(profileBefore.getName());

        // генерация нового валидного имени юзера
        String newName = RandomData.randomValidFullName();

        // ШАГ 2: авторизация юзера
        authAsUser(user);

        // переход на главную страницу и открытие редактирования профиля
        new UserDashboard()
                .open()
                .checkWelcomeName("noname")
                .openEditProfile();

        // ШАГ 3: изменение имени
        new ChangeUserNamePage()
                .enterName(newName)
                .saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATE_SUCCESSFULLY.getMessage())
                .goHome();

        // ШАГ 4: проверка, что на главной странице имя изменилось на новое
        new UserDashboard().checkWelcomeName(newName);

        // ШАГ 5: получение обновленного профиля юзера через api
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

        // получение тек профиля через api
        CreateUserResponse profileBefore = userSteps.getProfile();
        // проверка, что у нового юзера имя еще не задано
        assertNull(profileBefore.getName());

        // генерация нового НЕвалидного имени юзера с одним словом
        String newInvalidName = RandomData.randomSingleWordName();

        // ШАГ 2: авторизация
        authAsUser(user);

        // переход на главную страницу и открытие редактирования профиля
        new UserDashboard()
                .open()
                .checkWelcomeName("noname")
                .openEditProfile();

        // ШАГ 3: изменение имени
        new ChangeUserNamePage()
                .enterName(newInvalidName)
                .saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAINS_2_WORDS_WITH_LETTERS_ONLY.getMessage());

        // ШАГ 4: проверка, что на главной странице имя НЕ изменилось
        new ChangeUserNamePage()
                .goHome()
                .checkWelcomeName("noname");

        // ШАГ 5: получение обновленного профиля юзера через api
        CreateUserResponse profileAfter = userSteps.getProfile();
        // проверка, что имя НЕ изменилось на сервере
        assertNull(profileAfter.getName());
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

        // получение тек профиля через api
        CreateUserResponse profileBefore = userSteps.getProfile();

        // проверка, что у нового юзера имя еще не задано
        assertNull(profileBefore.getName());

        // генерация нового НЕвалидного имени юзера с цифрой
        String newInvalidName = RandomData.randomFullNameWithDigit();

        // ШАГ 2: авторизация
        authAsUser(user);

        // переход на главную страницу и открытие редактирования профиля
        new UserDashboard()
                .open()
                .checkWelcomeName("noname")
                .openEditProfile();

        // ШАГ 3: изменение имени
        new ChangeUserNamePage()
                .enterName(newInvalidName)
                .saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAINS_2_WORDS_WITH_LETTERS_ONLY.getMessage());

        // ШАГ 4: проверка, что на главной странице имя НЕ изменилось
        new ChangeUserNamePage()
                .goHome()
                .checkWelcomeName("noname");

        // ШАГ 5: получение обновленного профиля юзера через api
        CreateUserResponse profileAfter = userSteps.getProfile();
        // проверка, что имя НЕ изменилось на сервере
        assertNull(profileAfter.getName());
    }
}
