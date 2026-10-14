package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositMoneyTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    // данные пользователя kate0000
    private final String username = "kate0000";
    private final String password = "KateKate12!!";

    // хранение Authorization после логина
    private String auth;

    @BeforeEach
    public void setup() {
        auth = login();
    }

    // авторизация пользователя kate0000
    private String login() {
        // данные пользователя
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                        "username": "%s",
                        "password": "%s"
                        }
                        """.formatted(username, password))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .header("Authorization");
    }

    // получение id аккаунта пользователя
    private int getUserId() {
        return given()
                .header("Authorization", auth)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].id");
    }

    // получение текущего баланса пользователя
    private Float getBalance() {
        return given()
                .header("Authorization", auth)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].balance");
    }

    // выполнение депозита
    private void makeDeposit(int userId, double amount) {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", auth)
                .body("""
                        {
                        "id": %d,
                        "balance": %s
                        }
                        """.formatted(userId, amount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(200);
    }

    // ТС - 01, 02, 03, 04, 05 - положительные тесты
    @ParameterizedTest
    @ValueSource(doubles = {100, 1, 0.01, 4999.99, 5000})
    public void positiveDepositMoneyTest(double amount) {
        int userId = getUserId();

        Float balanceBefore = getBalance();

        makeDeposit(userId, amount);

        Float balanceAfter = getBalance();

        assertEquals(balanceBefore + amount, balanceAfter, 0.001);
    }

    // ТС - 06, 07, 08, 09 - негативные тесты
    @ParameterizedTest
    @ValueSource(doubles = {5000.01, 5001, 0, -1})
    public void negativeDepositMoneyTest(double amount) {
        int userId = getUserId();

        Float balanceBefore = getBalance();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", auth)
                .body("""
                        {
                        "id": %d,
                        "balance": %s
                        }
                        """.formatted(userId, amount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(400);

        Float balanceAfter = getBalance();

        assertEquals(balanceBefore, balanceAfter, 0.001);
    }

    // ТС - 10 - Депозит в чужой аккаунт
    @Test
    public void depositToAnotherUserAccountTest() {
        // авторизация второго пользователя petya345
        String authB = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                        "username": "petya345",
                        "password": "PetyaPetya123!"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .header("Authorization");

        // получение id аккаунта пользователя petya345
        int accountBId = given()
                .header("Authorization", authB)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].id");


        // баланс пользователя petya345 ДО депозита
        Float balanceBefore = given()
                .header("Authorization", authB)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].balance");


        // попытка пользователя kate0000 сделать депозит на аккаунт пользователя petya345
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", auth)
                .body("""
                        {
                        "id": %d
                        "balance": 100
                        }
                        """.formatted(accountBId))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(400);

        // баланс пользователя petya345 ПОСЛЕ попытки депозита
        Float balanceAfter = given()
                .header("Authorization", authB)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].balance");

        // проверка, что баланс пользователя petya345 не изменился
        assertEquals(balanceBefore, balanceAfter);
    }

    // ТС - 11 - Депозит в несуществующий аккаунт
    @Test
    public void depositToNonExistingAccountTest() {
        // баланс пользователя kate0000 до попытки депозита в несуществующий аккаунт
        Float balanceBefore = getBalance();

        // несуществующий аккаунт
        int nonExistAccountId = 999999;

        // пользователь kate0000 пытается сделать депозит в несуществующий аккаунт
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", auth)
                .body("""
                        {
                        "id": %d
                        "balance": 100
                        }
                        """.formatted(nonExistAccountId))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(400);

        // баланс пользователя kate0000 после депозита
        Float balanceAfter = getBalance();

        // проверка, что баланс не изменился
        assertEquals(balanceBefore, balanceAfter);
    }
}
