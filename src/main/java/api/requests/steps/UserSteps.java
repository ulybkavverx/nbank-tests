package api.requests.steps;

import api.models.*;
import api.requests.AccountRequester;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

import java.util.List;

public class UserSteps {
    private final RequestSpecification requestSpecification;

    public UserSteps(String username, String password) {
        this.requestSpecification =
                RequestSpecs.authAsUser(username, password); // мой вариант
    }

    // получение всех аккаунтов тек юзера
    public List<CreateAccountResponse> getAllAccounts() {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                requestSpecification,
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK()
        ).getAll(CreateAccountResponse[].class);
    }

    // список акков текущего юзера
    public List<Account> getAccounts() {
        return new AccountRequester(
                requestSpecification,
                ResponseSpecs.requestReturnsOK()
        ).getAccounts();
    }

    // пополнение указанного акка на заданную сумму, проверка статуса
    public void depositMoney(long accountId, double amount, int expectedStatusCode) {
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        new CrudRequester(
                requestSpecification,
                Endpoint.DEPOSIT_MONEY,
                ResponseSpecs.requestReturnsStatusCode(expectedStatusCode)
        ).post(request);
    }

    // перевод между аккаунтами с проверкой статус-кода
    public void transferMoney(int senderAccountId, int receiverAccountId, double amount, int expectedStatusCode) {
        // сборка тела запроса с данными перевода
        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        // отправка запроса через общий requester и проверка статус-кода
        new CrudRequester(
                requestSpecification,
                Endpoint.TRANSFER_MONEY,
                ResponseSpecs.requestReturnsStatusCode(expectedStatusCode)
        ).post(request);
    }

    // создание нового счета для тек пользователя
    public CreateAccountResponse createAccount() {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                requestSpecification,
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated()
        ).post(null);
    }

    private double balanceOf(int accountId) {
        return getAccounts().stream()
                .filter(account -> account.getId() == accountId)
                .findFirst()
                .orElseThrow()
                .getBalance();
    }

    // случайные допустим депозиты, пока баланс не станет >= target
    public void fundAtLeast(int accountId, double target) {
        long targetCents = Math.round(target * 100);
        while (Math.round(balanceOf(accountId) * 100) < targetCents) {
            double need = target - balanceOf(accountId);
            double deposit = Math.min(5000, Math.floor(need * 100) / 100.0);
            if (deposit < 0.01) {
                deposit = 0.01;
            }
            depositMoney(accountId, deposit, HttpStatus.SC_OK);
        }
    }

    // изменение имени тек юзера через api
    public ChangeUserNameResponse changeUserName(String name) {
        ChangeUserNameRequest request = ChangeUserNameRequest.builder()
                .name(name)
                .build();

        // отправка put запроса и получение ответа
        return new ValidatedCrudRequester<ChangeUserNameResponse>(
                requestSpecification,
                Endpoint.CHANGE_USER_NAME,
                ResponseSpecs.requestReturnsOK()
        ).put(request);
    }

    // получение профиля текущего юзера
    public CreateUserResponse getProfile() {
        return new ValidatedCrudRequester<CreateUserResponse>(
                requestSpecification,
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK()
        ).get();
    }

    // получение баланса
    public double getAccountBalance(long accountId) {
        return getAccounts().stream()
                .filter(account -> account.getId() == accountId)
                .findFirst()
                .orElseThrow()
                .getBalance();
    }
}
