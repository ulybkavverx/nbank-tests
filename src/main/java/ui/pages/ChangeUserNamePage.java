package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.UIAssertionError;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;

public class ChangeUserNamePage extends BasePage<ChangeUserNamePage> {
    private final SelenideElement nameInput =
            $(Selectors.byAttribute("placeholder", "Enter new name"));
    private final SelenideElement saveChangesButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));
    private final SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home"));

    private String enteredName;

    @Override
    public String url() {
        return "edit-profile";
    }

    public ChangeUserNamePage enterName(String name) {
        this.enteredName = name;
        nameInput.shouldBe(Condition.visible, Condition.enabled);

        nameInput.setValue(name);
        // проверка, не очистилось ли поле ввода в теч 2 сек, если очистилось -> повторный ввод
        if (isCleared()) {
            nameInput.setValue(name);
            // если очистилось снова -> еще одна попытка ввода
            if (isCleared()) {
                nameInput.setValue(name);
            }
        }

        nameInput.shouldHave(Condition.value(name));
        return this;
    }

    public ChangeUserNamePage saveChanges() {
        nameInput.shouldHave(Condition.value(enteredName));
        saveChangesButton.shouldBe(Condition.visible, Condition.enabled)
                .click();
        return this;
    }

    public UserDashboard goHome() {
        homeButton.shouldBe(Condition.visible).click();
        return new UserDashboard();
    }

    // метод только для проверки очистилось ли поле ввода
    private boolean isCleared() {
        try {
            // если поле стало пустым в теч 2 сек - return true
            nameInput.shouldBe(Condition.empty, Duration.ofSeconds(2));
            return true;
            // если за 2 сек оно не стало пустым -> exception
        } catch (UIAssertionError ignored) {
            return false;
        }
    }
}
