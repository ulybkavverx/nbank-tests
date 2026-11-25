package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;
import java.util.Locale;

import static com.codeborne.selenide.Selenide.$;

public class DepositPage extends BasePage<DepositPage> {
    private final SelenideElement accountSelector = $(".account-selector");
    private final SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private final SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));
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
        String id = String.valueOf(accountId);

        accountSelector.shouldBe(Condition.visible);

        Selenide.Wait()
                .withTimeout(Duration.ofSeconds(10))
                .until(driver -> accountSelector.getOptions().stream()
                        .anyMatch(option -> id.equals(option.getValue())));

        accountSelector.selectOptionByValue(id);
        accountSelector.shouldHave(Condition.value(id));
        return this;
    }

    public DepositPage enterAmount(double amount) {
        String value = String.format(Locale.US, "%.2f", amount);
        amountInput.shouldBe(Condition.visible).setValue(value);
        amountInput.shouldHave(Condition.value(value));
        return this;
    }

    public DepositPage deposit() {
        depositButton.click();
        return this;
    }
}
