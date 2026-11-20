package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
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

        // получение тек профиля и проверка, что имя еще не задано
        CreateUserResponse profileBefore = userSteps.getProfile();
        assertNull(profileBefore.getName());

        // генерация нового валидного имени юзера
        String newName = RandomData.randomValidFullName();

        // ШАГ 2: авторизация юзера
        authAsUser(user);

        // ШАГ 3: изменение имени через ui и проверка отображения нового имени на Dashboard
        new UserDashboard()
                .open()
                .checkWelcomeName(UserDashboard.DEFAULT_USER_NAME)
                .openEditProfile()
                .enterName(newName)
                .saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATE_SUCCESSFULLY.getMessage())
                .goHome()
                .checkWelcomeName(newName);

        // ШАГ 4: получение обновленного профиля юзера через api
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

        // получение тек профиля и проверка, что имя еще не задано
        CreateUserResponse profileBefore = userSteps.getProfile();
        assertNull(profileBefore.getName());

        // генерация нового НЕвалидного имени юзера с одним словом
        String newInvalidName = RandomData.randomSingleWordName();

        // ШАГ 2: авторизация
        authAsUser(user);

        // ШАГ 3: изменение имени через ui и проверка отображения имени на Dashboard
        new UserDashboard()
                .open()
                .checkWelcomeName(UserDashboard.DEFAULT_USER_NAME)
                .openEditProfile()
                .enterName(newInvalidName)
                .saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAINS_2_WORDS_WITH_LETTERS_ONLY.getMessage())
                .goHome()
                .checkWelcomeName(UserDashboard.DEFAULT_USER_NAME);

        // ШАГ 4: получение обновленного профиля юзера через api
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

        // получение тек профиля и проверка, что имя еще не задано
        CreateUserResponse profileBefore = userSteps.getProfile();
        assertNull(profileBefore.getName());

        // генерация нового НЕвалидного имени юзера с цифрой
        String newInvalidName = RandomData.randomFullNameWithDigit();

        // ШАГ 2: авторизация
        authAsUser(user);

        // ШАГ 3: изменение имени через ui и проверка отображения имени на Dashboard
        new UserDashboard()
                .open()
                .checkWelcomeName(UserDashboard.DEFAULT_USER_NAME)
                .openEditProfile()
                .enterName(newInvalidName)
                .saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAINS_2_WORDS_WITH_LETTERS_ONLY.getMessage())
                .goHome()
                .checkWelcomeName(UserDashboard.DEFAULT_USER_NAME);

        // ШАГ 4: получение обновленного профиля юзера через api
        CreateUserResponse profileAfter = userSteps.getProfile();
        // проверка, что имя НЕ изменилось на сервере
        assertNull(profileAfter.getName());
    }
}
