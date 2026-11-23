package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;
import lombok.Getter;
import ui.elements.UserBage;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class AdminPanel extends BasePage<AdminPanel> {
    private SelenideElement adminPanelText = $(Selectors.byText("Admin Panel"));
    private SelenideElement addUserButton = $(Selectors.byText("Add User"));

    @Override
    public String url() {
        return "/admin";
    }


    public AdminPanel createUser(String username, String password) {
        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        addUserButton.click();
        return this;
    }

    public List<UserBage> getAllUsers() {
        ElementsCollection elementsCollection = $(Selectors.byText("All Users")).parent().findAll("li");
        elementsCollection.first().shouldBe(Condition.visible);
        return generatePageElements(elementsCollection, UserBage::new);
    }

    public UserBage findUserByUsername(String username) {
        return RetryUtils.retry(
                () -> getAllUsers().stream().filter(it -> it.getUsername().equals(username)).findAny().orElse(null),
                result -> result != null,
                3,
                1000
        );
    }
}
