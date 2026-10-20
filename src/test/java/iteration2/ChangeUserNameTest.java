package iteration2;

import iteration1.BaseTest;
import models.ChangeUserNameRequest;
import models.ChangeUserNameResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.ChangeUserNameRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

// наследование от BaseTest, чтобы использовать мягкие проверки softly
public class ChangeUserNameTest extends BaseTest {

    // данные юзера для авторизации
    private final String username = "kate0000";
    private final String password = "KateKate12!!";

    // позитивный тест: проверка успешное изменение имени
    @Test
    public void userCanChangeNameWithCorrectData() {

        // создание тела запроса с новым именем
        ChangeUserNameRequest request = ChangeUserNameRequest.builder()
                .name("Bob Joy")
                .build();

        // отправка запроса и преобразование JSON -> Java-объект
        ChangeUserNameResponse response = new ChangeUserNameRequester(
                // авторизация юзера
                RequestSpecs.authAsUser(username, password),
                // ожидание статус код 200
                ResponseSpecs.requestReturnsOK()
        ).post(request)
                .extract()
                .as(ChangeUserNameResponse.class);

        // проверка, что сервер вернул новое имя
        softly.assertThat(response.getCustomer().getName()).isEqualTo(request.getName());
    }

    // источник тестовых данных для негативных проверок
    public static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.of("Bob", 400),
                Arguments.of("Bob Joy Bob", 400),
                Arguments.of("Bob1 Joy", 400),
                Arguments.of("Bob! Joy", 400),
                Arguments.of("BobJoy", 400),
                Arguments.of("", 400)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    public void userCannotChangeNameWithInvalidData(String name, int expectedStatusCode) {

        // создание запрос с именем из набора тестовых данных
        ChangeUserNameRequest request = ChangeUserNameRequest.builder()
                .name(name)
                .build();

        // отправка запроса и проверка ожид результата
        new ChangeUserNameRequester(
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsStatusCode(expectedStatusCode)
        ).post(request);
    }
}
