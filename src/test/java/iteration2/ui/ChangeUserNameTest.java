package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateUserResponse;
import api.requests.steps.UserSteps;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.ChangeUserNamePage;
import ui.pages.UserDashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ChangeUserNameTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanSuccessfullyChangeNameTest() {
        // получение UserSteps тек юзера
        UserSteps userSteps = SessionStorage.getSteps();

        // получение тек профиля через api
        CreateUserResponse profileBefore = userSteps.getProfile();

        // проверка, что у нового юзера имя еще не задано
        assertNull(profileBefore.getName());

        // генерация нового валидного имени юзера
        String newName = RandomData.randomValidFullName();

        // переход на главную страницу и открытие редактирования профиля
        new UserDashboard()
                .open()
                .checkWelcomeName("noname")
                .openEditProfile();

        // изменение имени
        new ChangeUserNamePage()
                .enterName(newName)
                .saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATE_SUCCESSFULLY.getMessage())
                .goHome();

        // проверка, что на главной странице имя изменилось на новое
        new UserDashboard().checkWelcomeName(newName);

        // получение обновленного профиля юзера через api
        CreateUserResponse profileAfter = userSteps.getProfile();
        // проверка, что новое имя сохранилось на сервере
        assertEquals(newName, profileAfter.getName());
    }

    @Test
    @UserSession
    public void userCannotChangeNameWithSingleWordTest() {
        // получение UserSteps тек юзера
        UserSteps userSteps = SessionStorage.getSteps();

        // получение тек профиля через api
        CreateUserResponse profileBefore = userSteps.getProfile();
        // проверка, что у нового юзера имя еще не задано
        assertNull(profileBefore.getName());

        // генерация нового НЕвалидного имени юзера с одним словом
        String newInvalidName = RandomData.randomSingleWordName();

        // переход на главную страницу и открытие редактирования профиля
        new UserDashboard()
                .open()
                .checkWelcomeName("noname")
                .openEditProfile();

        // изменение имени
        new ChangeUserNamePage()
                .enterName(newInvalidName)
                .saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAINS_2_WORDS_WITH_LETTERS_ONLY.getMessage());

        // проверка, что на главной странице имя НЕ изменилось
        new ChangeUserNamePage()
                .goHome()
                .checkWelcomeName("noname");

        // получение обновленного профиля юзера через api
        CreateUserResponse profileAfter = userSteps.getProfile();
        // проверка, что имя НЕ изменилось на сервере
        assertNull(profileAfter.getName());
    }

    @Test
    @UserSession
    public void userCannotChangeNameWithDigitTest() {
        // получение UserSteps тек юзера
        UserSteps userSteps = SessionStorage.getSteps();

        // получение тек профиля через api
        CreateUserResponse profileBefore = userSteps.getProfile();

        // проверка, что у нового юзера имя еще не задано
        assertNull(profileBefore.getName());

        // генерация нового НЕвалидного имени юзера с цифрой
        String newInvalidName = RandomData.randomFullNameWithDigit();

        // переход на главную страницу и открытие редактирования профиля
        new UserDashboard()
                .open()
                .checkWelcomeName("noname")
                .openEditProfile();

        // изменение имени
        new ChangeUserNamePage()
                .enterName(newInvalidName)
                .saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAINS_2_WORDS_WITH_LETTERS_ONLY.getMessage());

        // проверка, что на главной странице имя НЕ изменилось
        new ChangeUserNamePage()
                .goHome()
                .checkWelcomeName("noname");

        // получение обновленного профиля юзера через api
        CreateUserResponse profileAfter = userSteps.getProfile();
        // проверка, что имя НЕ изменилось на сервере
        assertNull(profileAfter.getName());
    }
}
