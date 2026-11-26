package iteration1.ui;

import api.models.CreateAccountResponse;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import common.utils.RetryUtils;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanCreateAccountTest() {
        UserDashboard dashboard = new UserDashboard().open();
        dashboard.createNewAccount();
        dashboard.waitForAlert();

        List<CreateAccountResponse> createdAccounts = RetryUtils.retry(
                () -> SessionStorage.getSteps().getAllAccounts(),
                accounts -> accounts.size() == 1,
                5,
                300
        );

        dashboard.checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage()
                + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();
    }
}
