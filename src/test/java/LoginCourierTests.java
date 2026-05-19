import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.Matchers.*;

public class LoginCourierTests extends BaseTest {

    private String login;
    private String password;

    Client client = new Client();

    @BeforeEach
    public void createTestCourier() {
        login = "kalyakin_" + System.currentTimeMillis();
        password = "1234567890";
        String name = "Andrey";

        client.createCourier(login, password, name)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Авторизация с позитивным логином и паролем")
    public void loginCourierPositive() {
        Response response = client.loginCourier(login, password);
        bodyPositive(response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"login", "password"})
    @DisplayName("Попытка логина курьера без одного поля")
    public void courierLoginWithoutOneField(String missingField) {
        String requestLogin = missingField.equals("login") ? null : login;
        String requestPassword = missingField.equals("password") ? null : password;
        Response response = client.loginCourier(requestLogin, requestPassword);
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
        Response response = client.loginCourier(requestLogin, requestPassword);

        bodyNegativeWrongData(response);
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
        Response loginResponse = client.loginCourier(login, password);
        if (loginResponse.statusCode() == 200) {
            int courierId = loginResponse.then().extract().path("id");
            client.deleteCourier(courierId);
        }
    }
}
