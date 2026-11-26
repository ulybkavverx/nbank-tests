package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

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
        String id = String.valueOf(accountId);

        // обновление страницы для загрузки актуального списка счетов
        Selenide.refresh();
        checkPageIsOpened();

        // ожидание загрузки селектора
        accountSelector.shouldBe(Condition.visible);

        // ожидание появления нужного счета
        accountSelector.$$("option[value='%s']".formatted(id))
                .first()
                .should(Condition.exist, Duration.ofSeconds(10));

        // выбор счета
        accountSelector.selectOptionByValue(id);
        accountSelector.shouldHave(Condition.value(id));
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
