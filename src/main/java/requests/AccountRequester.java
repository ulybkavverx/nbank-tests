package requests;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.Account;

import java.util.List;

import static io.restassured.RestAssured.given;

// Класс отвечает за получение аккаунтов пользователя
public class AccountRequester {
    private final RequestSpecification requestSpecification;
    private final ResponseSpecification responseSpecification;

    public AccountRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }

    // получение списка аккаунтов текущего юзера
    public List<Account> getAccounts() {
        return given()
                .spec(requestSpecification)
                .get("/api/v1/customer/accounts")
                .then()
                .assertThat()
                .spec(responseSpecification)
                .extract()
                .jsonPath()
                .getList("", Account.class);
    }
}
