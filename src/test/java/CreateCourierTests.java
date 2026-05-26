import io.qameta.allure.Step;
import io.qameta.allure.Description;
import org.junit.jupiter.api.*;
import io.restassured.response.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;


public class CreateCourierTests extends BaseTest {
    private String login;
    private String password;
    private String name;

    Client client = new Client();
    private final List<String> createdLogins = new ArrayList<>();

    @BeforeEach
    public void prepareTestData() {
        login = "kalyakin_" + System.currentTimeMillis();
        password = "1234567890";
        name = "Andrey";
    }

    @AfterEach
    public void deleteCreatedCouriers() {
        for (String createdLogin : createdLogins) {
            Response loginResponse = client.loginCourier(createdLogin, password);
            if (loginResponse.statusCode() == 200) {
                int courierId = loginResponse.path("id");
                client.deleteCourier(courierId);
            }
        }
        createdLogins.clear();
    }

    @Test
    @DisplayName("Создание курьера позитивный")
    @Description("Создаётся курьер с позитивно заполненными полями")
    public void createCourierPositive() {
        Response response = client.createCourier(login, password, name);
        createdLogins.add(login);
        bodyIsOk(response);
    }

    @Test
    @DisplayName("Невозможность повторного создания курьера")
    @Description("Попытка создания курьера два раза подряд")
    public void createCourierDuplicate() {
        client.createCourier(login, password, name);
        createdLogins.add(login);
        Response response = client.createCourier(login, password, name);
        bodyIsDuplicate(response);
    }

    @ParameterizedTest
    @MethodSource("courierWithoutOneFieldData")
    @DisplayName("Попытка создать курьера без одного поля")
    public void courierWithoutOneField(String login, String password, String name) {
        Response response = client.createCourier(login, password, name);
        bodyIsNotOk(response);
    }

    @Step("Проверка тела и кода ответа ok: true")
    public void bodyIsOk(Response response){
        response.then().assertThat().statusCode(201)
                .and()
                .body("ok", equalTo(true));
    }

    @Step("Проверка тела и кода ответа, что логин уже существует")
    public void bodyIsDuplicate(Response response){
        response.then().assertThat().statusCode(409)
                .and()
                .body("message", equalTo("Этот логин уже используется."));
    }

    @Step("Проверка тела и кода ответа, что недостаточно поля")
    public void bodyIsNotOk(Response response){
        response.then().assertThat().statusCode(400)
                .and()
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    static Stream<Arguments> courierWithoutOneFieldData() {
        String login = "kalyakin_" + System.currentTimeMillis();
        String password = "1234567890";
        String name = "Andrey";

        return Stream.of(
                Arguments.of(null, password, name),
                Arguments.of(login + "_1", null, name),
                Arguments.of(login + "_2", password, null)
        );
    }
}
