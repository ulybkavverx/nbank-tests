package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement userUsername = $(Selectors.byClassName("user-username"));
    private SelenideElement editProfile = $(Selectors.byText("✏️ Edit Profile"));
    public static final String DEFAULT_USER_NAME = "noname";
    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount() {
        createNewAccount.click();
        return this;
    }

    public UserDashboard checkWelcomeName(String name) {
        welcomeText.shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, " + name + "!"));
        return this;
    }

    public UserDashboard openEditProfile() {
        userUsername.shouldBe(Condition.visible).click();
        editProfile.shouldBe(Condition.visible);
        return this;
    }
}
