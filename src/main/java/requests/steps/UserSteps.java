package requests.steps;

import generators.RandomData;
import io.restassured.specification.RequestSpecification;
import models.Account;
import models.CreateAccountResponse;
import models.DepositMoneyRequest;
import models.TransferMoneyRequest;
import org.apache.http.HttpStatus;
import requests.AccountRequester;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

public class UserSteps {
    private final RequestSpecification requestSpecification;

    public UserSteps(String username, String password) {
        this.requestSpecification =
                RequestSpecs.authAsUser(username, password);
    }

    // список акков текущего юзера
    public List<Account> getAccounts() {
        return new AccountRequester(
                requestSpecification,
                ResponseSpecs.requestReturnsOK()
        ).getAccounts();
    }

    // пополнение указанного акка на заданную сумму, проверка статуса
    public void depositMoney(int accountId, double amount, int expectedStatusCode) {
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
        while (balanceOf(accountId) + 0.001 < target) {
            double need = target - balanceOf(accountId);
            depositMoney(accountId, RandomData.randomDepositUpTo(need), HttpStatus.SC_OK);
        }
    }
}
