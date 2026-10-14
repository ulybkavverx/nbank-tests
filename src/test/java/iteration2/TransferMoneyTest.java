package iteration2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferMoneyTest {

    private static final String BASE_URL = "http://localhost:4111";

    // данные пользователя-отправителя
    private final String username = "kate0000";
    private final String password = "KateKate12!!";

    // данные пользователя-получателя
    private final String usernameB = "kate19981";
    private final String passwordB = "KateTest19981!";

    // хранение Authorization после логина
    private String auth;
    private String authB;

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.baseURI = BASE_URL;
    }

    @BeforeEach
    public void setup() {
        auth = login(username, password);
        authB = login(usernameB, passwordB);
    }

    // авторизация пользователя
    private String login(String username, String password) {

        // данные пользователя
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                        "username": "%s",
                        "password": "%s"
                        }
                        """.formatted(username, password))
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .header("Authorization");
    }

    // получение id первого аккаунта пользователя
    private int getFirstAccountId(String auth) {
        return given()
                .header("Authorization", auth)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].id");
    }

    // получение id второго аккаунта пользователя
    private int getSecondAccountId(String authB) {
        return given()
                .header("Authorization", authB)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("[1].id");
    }

    // получение баланса
    private Float getBalance(String auth) {
        return given()
                .header("Authorization", auth)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].balance");
    }

    // выполнение перевода
    private void transfer(int senderAccountId, int receiverAccountId, double amount) {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", auth)
                .body("""
                        {
                        "senderAccountId": %d,
                        "receiverAccountId": %d,
                        "amount": %s
                        }
                        """.formatted(senderAccountId, receiverAccountId, amount))
                .post("/api/v1/accounts/transfer")
                .then()
                .log().all()
                .statusCode(200);
    }

    // невалидный перевод
    private void invalidTransfer(int senderAccountId, int receiverAccountId, double amount) {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", auth)
                .body("""
                        {
                        "senderAccountId": %d,
                        "receiverAccountId": %d,
                        "amount": %s
                        }
                        """.formatted(senderAccountId, receiverAccountId, amount))
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(400);
    }

    // ТС - 12-14 - Положительные тесты, положительная сумма
    @ParameterizedTest
    @ValueSource(doubles = {100, 1, 0.01})
    public void successfulTransferPositiveAmountTest(double amount) {
        int senderAccountId = getFirstAccountId(auth);
        int receiverAccountId = getSecondAccountId(authB);

        double senderBalanceBefore = getBalance(auth);
        double receiverBalanceBefore = getBalance(authB);

        transfer(senderAccountId, receiverAccountId, amount);

        double senderBalanceAfter = getBalance(auth);
        double receiverBalanceAfter = getBalance(authB);

        assertEquals(senderBalanceBefore - amount, senderBalanceAfter, 0.01);
        assertEquals(receiverBalanceBefore + amount, receiverBalanceAfter, 0.01);
    }

    // ТС - 15 - Перевод максимально допустимой положительной суммы
    @Test
    public void positiveTransferMaxPositiveAmount() {
        int senderAccountId = getFirstAccountId(auth);
        int receiverAccountId = getSecondAccountId(authB);

        double senderBalanceBefore = getBalance(auth);
        double receiverBalanceBefore = getBalance(authB);

        double amount = 10000;

        System.out.println("Баланс отправителя: " + senderBalanceBefore);
        System.out.println("Сумма перевода: " + amount);

        transfer(senderAccountId, receiverAccountId, amount);

        double senderBalanceAfter = getBalance(auth);
        double receiverBalanceAfter = getBalance(authB);

        assertEquals(senderBalanceBefore - amount, senderBalanceAfter, 0.01);
        assertEquals(receiverBalanceBefore + amount, receiverBalanceAfter, 0.01);
    }

    // ТС - 16 - Перевод суммы чуть меньше максимальной
    @Test
    public void positiveTransferSlightlyLessThanMaxPositiveAmount() {
        int senderAccountId = getFirstAccountId(auth);
        int receiverAccountId = getSecondAccountId(authB);

        double senderBalanceBefore = getBalance(auth);
        double receiverBalanceBefore = getBalance(authB);

        double amount = 9999.99;

        System.out.println("Баланс отправителя: " + senderBalanceBefore);
        System.out.println("Сумма перевода: " + amount);

        transfer(senderAccountId, receiverAccountId, amount);

        double senderBalanceAfter = getBalance(auth);
        double receiverBalanceAfter = getBalance(authB);

        assertEquals(senderBalanceBefore - amount, senderBalanceAfter, 0.01);
        assertEquals(receiverBalanceBefore + amount, receiverBalanceAfter, 0.01);
    }

    // ТС - 17-20 - Негативные тесты, невалидная сумма
    @ParameterizedTest
    @ValueSource(doubles = {10001, 10000.01, 0, -1})
    public void invalidTransferTest(double amount) {
        int senderAccountId = getFirstAccountId(auth);
        int receiverAccountId = getSecondAccountId(authB);

        double senderBalanceBefore = getBalance(auth);
        double receiverBalanceBefore = getBalance(authB);

        invalidTransfer(senderAccountId, receiverAccountId, amount);

        double senderBalanceAfter = getBalance(auth);
        double receiverBalanceAfter = getBalance(authB);

        assertEquals(senderBalanceBefore, senderBalanceAfter, 0.01);
        assertEquals(receiverBalanceBefore, receiverBalanceAfter, 0.01);
    }

    // ТС - 21 - Перевод суммы, равной балансу отправителя
    @Test
    public void transferAmountEqualSendersBalance() {
        int senderAccountId = getFirstAccountId(auth);
        int receiverAccountId = getSecondAccountId(authB);

        double senderBalanceBefore = getBalance(auth);
        double receiverBalanceBefore = getBalance(authB);

        double amount = 5000;

        transfer(senderAccountId, receiverAccountId, amount);

        double senderBalanceAfter = getBalance(auth);
        double receiverBalanceAfter = getBalance(authB);

        assertEquals(senderBalanceBefore - amount, senderBalanceAfter, 0.01);
        assertEquals(receiverBalanceBefore + amount, receiverBalanceAfter, 0.01);
    }

    // ТС - 22 - Перевод суммы больше баланса отправителя
    @Test
    public void negativeTransferMoreThanBalanceTest() {
        int senderAccountId = getFirstAccountId(auth);
        int receiverAccountId = getSecondAccountId(authB);

        double senderBalanceBefore = getBalance(auth);
        double receiverBalanceBefore = getBalance(authB);

        double amount = senderBalanceBefore + 1;

        invalidTransfer(senderAccountId, receiverAccountId, amount);

        double senderBalanceAfter = getBalance(auth);
        double receiverBalanceAfter = getBalance(authB);

        assertEquals(senderBalanceBefore, senderBalanceAfter, 0.01);
        assertEquals(receiverBalanceBefore, receiverBalanceAfter, 0.01);
    }

    // ТС - 23 - Перевод между своими аккаунтами
    @Test
    public void transferBetweenOwnAccountsTest() {
        int senderAccountId = getSecondAccountId(auth);
        int receiverAccountId = getFirstAccountId(auth);

        Float senderBalanceBefore = given()
                .header("Authorization", auth)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("[1].balance");

        Float receiverBalanceBefore = given()
                .header("Authorization", auth)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .log().body()
                .extract()
                .path("[0].balance");

        double amount = 100;

        System.out.println("Отправитель ID: " + senderAccountId);
        System.out.println("Получатель ID: " + receiverAccountId);

        transfer(senderAccountId, receiverAccountId, amount);

        Float senderBalanceAfter = given()
                .header("Authorization", auth)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("[1].balance");

        Float receiverBalanceAfter = given()
                .header("Authorization", auth)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].balance");

        assertEquals(senderBalanceBefore.doubleValue() - amount, senderBalanceAfter.doubleValue(), 0.01);
        assertEquals(receiverBalanceBefore.doubleValue() + amount, receiverBalanceAfter.doubleValue(), 0.01);
    }

    // ТС - 24 - Перевод на чужой аккаунт
    @Test
    public void transferToAnotherUserTest() {
        int senderAccountId = getSecondAccountId(auth);
        int receiverAccountId = getFirstAccountId(authB);

        Float senderBalanceBefore = given()
                .header("Authorization", auth)
                .when()
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("[1].balance");

        Float receiverBalanceBefore = given()
                .header("Authorization", authB)
                .when()
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].balance");

        double amount = 100;

        System.out.println("ID отправителя: " + senderAccountId);
        System.out.println("ID получателя: " + receiverAccountId);
        System.out.println("Баланс отправителя: " + senderBalanceBefore);
        System.out.println("Баланс получателя: " + receiverBalanceBefore);

        transfer(senderAccountId, receiverAccountId, amount);

        Float senderBalanceAfter = given()
                .header("Authorization", auth)
                .when()
                .get("/api/v1/customer/accounts")
                .then().statusCode(200)
                .extract()
                .path("[1].balance");

        Float receiverBalanceAfter = given()
                .header("Authorization", authB)
                .when()
                .get("/api/v1/customer/accounts")
                .then().statusCode(200)
                .extract()
                .path("[0].balance");

        System.out.println("Баланс отправителя до: " + senderBalanceBefore);
        System.out.println("Баланс отправителя после: " + senderBalanceAfter);

        System.out.println("Баланс получателя до: " + receiverBalanceBefore);
        System.out.println("Баланс получателя после: " + receiverBalanceAfter);

        assertEquals(senderBalanceBefore.doubleValue() - amount, senderBalanceAfter.doubleValue(), 0.01);
        assertEquals(receiverBalanceBefore.doubleValue() + amount, receiverBalanceAfter.doubleValue(), 0.01);
    }
}
