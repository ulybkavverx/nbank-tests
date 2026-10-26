package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.ChangeUserNameRequest;
import models.ChangeUserNameResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

// наследование от BaseTest, чтобы использовать мягкие проверки softly
public class ChangeUserNameTest extends BaseTest {

    // данные существующего юзера, от имени которого выполняются запросы
    private static final String USERNAME = "kate0000";
    private static final String PASSWORD = "KateKate12!!";

    // позитивный тест: проверка успешное изменение имени
    @Test
    public void userCanChangeNameWithCorrectData() {

        // генерация имени
        String newName = RandomData.randomValidFullName();

        // создание тела put-запроса с новым именем
        ChangeUserNameRequest request = ChangeUserNameRequest.builder()
                .name(newName)
                .build();

        // отправка запроса с авторизацией и проверкой статуса
        // ValidatedCrudRequester -> преобразование тело ответа в ChangeUserNameResponse
        ChangeUserNameResponse response =
                new ValidatedCrudRequester<ChangeUserNameResponse>(
                        RequestSpecs.authAsUser(USERNAME, PASSWORD),
                        Endpoint.CHANGE_USER_NAME,
                        ResponseSpecs.requestReturnsOK()
                ).put(request);

        // проверка, что в ответе присутствует инфо о юзере
        // жeсткая проверка остановит тест, если объект отсутствует
        assertThat(response.getCustomer()).isNotNull();

        // проверка, что сервер вернул новое имя
        softly.assertThat(response.getCustomer().getName()).isEqualTo(request.getName());
    }

    // источник тестовых данных для негативных проверок
    public static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.of("Bob", HttpStatus.SC_BAD_REQUEST),
                Arguments.of("Bob Joy Bob", HttpStatus.SC_BAD_REQUEST),
                Arguments.of("Bob1 Joy", HttpStatus.SC_BAD_REQUEST),
                Arguments.of("Bob! Joy", HttpStatus.SC_BAD_REQUEST),
                Arguments.of("BobJoy", HttpStatus.SC_BAD_REQUEST),
                Arguments.of("", HttpStatus.SC_BAD_REQUEST)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    public void userCannotChangeNameWithInvalidData(String name, int expectedStatusCode) {

        // создание запрос с именем из набора тестовых данных
        ChangeUserNameRequest request = ChangeUserNameRequest.builder()
                .name(name)
                .build();

        // отправка put-запроса и проверка ожид результата
        new CrudRequester(
                RequestSpecs.authAsUser(USERNAME, PASSWORD),
                Endpoint.CHANGE_USER_NAME,
                ResponseSpecs.requestReturnsStatusCode(expectedStatusCode)
        ).put(request);
    }
}
