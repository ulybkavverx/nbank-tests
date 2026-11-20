package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class TransferPage extends BasePage<TransferPage> {
    private final SelenideElement accountSelector = $(".account-selector");
    private final SelenideElement recipientNameInput =
            $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private final SelenideElement recipientAccountNumberInput =
            $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private final SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private final SelenideElement confirmCheckbox = $(Selectors.byClassName("form-check-input"));
    private final SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
    private final SelenideElement transferPageTitle = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));

    @Override
    public String url() {
        return "/transfer";
    }

    public TransferPage checkPageIsOpened() {
        transferPageTitle.shouldBe(Condition.visible);
        return this;
    }

    public TransferPage selectAccount(long accountId) {
        accountSelector.shouldBe(Condition.visible);
        accountSelector.shouldHave(Condition.text("-- Choose an account --"));
        accountSelector.selectOptionByValue(String.valueOf(accountId));
        accountSelector.shouldHave(Condition.value(String.valueOf(accountId)));
        return this;
    }

    public TransferPage enterRecipientName(String recipientName) {
        recipientNameInput.setValue(recipientName);
        recipientNameInput.shouldHave(Condition.value(recipientName));
        return this;
    }

    public TransferPage enterRecipientAccountNumber(String accountNumber) {
        recipientAccountNumberInput.setValue(accountNumber);
        recipientAccountNumberInput.shouldHave(Condition.value(accountNumber));
        return this;
    }

    public TransferPage enterAmount(double amount) {
        amountInput.setValue(String.valueOf(amount));
        amountInput.shouldHave(Condition.value(String.valueOf(amount)));
        return this;
    }

    public TransferPage confirmTransfer() {
        confirmCheckbox.shouldBe(Condition.visible).click();
        confirmCheckbox.shouldBe(Condition.checked);
        return this;
    }

    public TransferPage sendTransfer() {
        sendTransferButton.click();
        return this;
    }
}
