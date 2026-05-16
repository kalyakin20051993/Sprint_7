import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class LoginCourierTests {

    private String login;
    private String password;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @BeforeEach
    public void createTestCourier() {
        login = "kalyakin_" + System.currentTimeMillis();
        password = "1234567890";
        String name = "Andrey";

        createCourier(login, password, name)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Авторизация с позитивным логином и паролем")
    public void loginCourierPositive() {
        Response response = loginCourier(login, password);
        bodyPositive(response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"login", "password"})
    @DisplayName("Попытка логина курьера без одного поля")
    public void courierLoginWithoutOneField(String missingField) {
        String requestLogin = missingField.equals("login") ? null : login;
        String requestPassword = missingField.equals("password") ? null : password;
        Response response = loginCourier(requestLogin, requestPassword);
        bodyNegativeWithoutField(response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"login", "password"})
    @DisplayName("Попытка логина курьера с неверным логином или паролем")
    public void courierLoginWrongOneField(String wrongField) {
        String requestLogin = wrongField.equals("login")
                ? login + RandomStringUtils.randomAlphanumeric(10)
                : login;
        String requestPassword = wrongField.equals("password")
                ? password + RandomStringUtils.randomAlphanumeric(10)
                : password;
        Response response = loginCourier(requestLogin, requestPassword);

        bodyNegativeWrongData(response);
    }

    @Step("Создание курьера: login={login}, password={password}, name={name}")
    public Response createCourier(String login, String password, String name) {
        String json = "{"
                + "\"login\": " + toJsonValue(login) + ","
                + "\"password\": " + toJsonValue(password) + ","
                + "\"firstName\": " + toJsonValue(name)
                + "}";

        return given()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .post("/api/v1/courier");
    }

    @Step("Логин курьера: login={login}, password={password}")
    public Response loginCourier(String login, String password) {
        String json = "{"
                + "\"login\": " + toJsonValue(login) + ","
                + "\"password\": " + toJsonValue(password)
                + "}";

        return given()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .post("/api/v1/courier/login");
    }

    @Step("Удаление курьера с id={courierId}")
    public Response deleteCourier(int courierId) {
        return given()
                .when()
                .delete("/api/v1/courier/" + courierId);
    }

    @Step("Проверка успешного логина курьера")
    public void bodyPositive(Response response){
        response.then().assertThat().statusCode(200)
                .and()
                .body("id", notNullValue());
    }

    @Step("Проверка ошибки при логине с отсутствующим полем")
    public void bodyNegativeWithoutField(Response response){
        response.then().assertThat().statusCode(400)
                .and()
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Step("Проверка ошибки при логине с неверным логином или паролем")
    public void bodyNegativeWrongData(Response response){
        response.then().assertThat().statusCode(404)
                .and()
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @AfterEach
    public void deleteTestCourier() {
        Response loginResponse = loginCourier(login, password);
        if (loginResponse.statusCode() == 200) {
            int courierId = loginResponse.then().extract().path("id");
            deleteCourier(courierId);
        }
    }

    private String toJsonValue(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }
}
