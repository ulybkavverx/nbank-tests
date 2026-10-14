package iteration2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.restassured.RestAssured.given;

public class ChangeUserNameTest {
    private static final String BASE_URL = "http://localhost:4111";

    // данные пользователя
    private final String username = "kate0000";
    private final String password = "KateKate12!!";

    // Токен авторизации
    private String auth;

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.baseURI = BASE_URL;
    }

    @BeforeEach
    public void setup() {
        auth = login(username, password);
    }

    // авторизация пользователя
    private String login(String username, String password) {
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

    // проверка изменения имени
    @ParameterizedTest
    @CsvSource({
            "Bob Joy, 200",
            "Bob, 400",
            "Bob Joy Bob, 400",
            "Bob1 Joy, 400",
            "Bob! Joy, 400",
            "BobJoy, 400",
            "'', 400"
    })

    public void changeNameTest(String name, int statusCode) {
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("""
                        {
                        "name": "%s"
                        }
                        """.formatted(name))
                .put("/api/v1/customer/profile")
                .then()
                .log().all()
                .statusCode(statusCode);
    }
}
