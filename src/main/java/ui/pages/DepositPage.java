package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class DepositPage extends BasePage<DepositPage> {
    private final SelenideElement accountSelector = $(".account-selector");
    private final SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private final SelenideElement depositButton =  $(Selectors.byText("\uD83D\uDCB5 Deposit"));
    private final SelenideElement depositPageTitle = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositPage checkPageIsOpened() {
        depositPageTitle.shouldBe(Condition.visible);
        return this;
    }

    public DepositPage selectAccount(long accountId) {
        accountSelector.shouldBe(Condition.visible);
        accountSelector.shouldHave(Condition.text("-- Choose an account --"));
        accountSelector.selectOptionByValue(String.valueOf(accountId));
        accountSelector.shouldHave(Condition.value(String.valueOf(accountId)));
        return this;
    }

    public DepositPage enterAmount(double amount) {
        amountInput.setValue(String.valueOf(amount));
        amountInput.shouldHave(Condition.value(String.valueOf(amount)));
        return this;
    }

    public DepositPage deposit() {
        depositButton.click();
        return this;
    }
}
