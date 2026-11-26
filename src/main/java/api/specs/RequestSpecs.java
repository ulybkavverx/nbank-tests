package api.specs;

import api.configs.Config;
import api.models.LoginUserRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestSpecs {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String ADMIN_AUTH_HEADER = "Basic YWRtaW46YWRtaW4=";
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin";

    private static Map<String, String> authHeaders = new ConcurrentHashMap<>(Map.of(ADMIN_USERNAME, ADMIN_AUTH_HEADER));

    private RequestSpecs() {
    }

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri(Config.getProperty("apiBaseUrl") + Config.getProperty("apiVersion"));
    }

    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification adminSpec() {
        return defaultRequestBuilder()
                .addHeader(AUTHORIZATION_HEADER, authHeaders.get(ADMIN_USERNAME))
                .build();
    }

    public static RequestSpecification authAsUser(String username, String password) {
        return defaultRequestBuilder()
                .addHeader(AUTHORIZATION_HEADER, getUserAuthHeader(username, password))
                .build();
    }

    public static String getUserAuthHeader(String username, String password) {
        return authHeaders.computeIfAbsent(username, key ->
                new CrudRequester(
                        RequestSpecs.unauthSpec(),
                        Endpoint.LOGIN,
                        ResponseSpecs.requestReturnsOK())
                        .post(LoginUserRequest.builder().username(username).password(password).build())
                        .extract()
                        .header(AUTHORIZATION_HEADER)
        );
    }
}