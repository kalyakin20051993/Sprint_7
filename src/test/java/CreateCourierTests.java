import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.qameta.allure.Description;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.given;


public class CreateCourierTests {
    String login = "kalyakin_" + System.currentTimeMillis();
    String password = "1234567890";
    String name = "Andrey";

    private final List<String> createdLogins = new ArrayList<>();

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @AfterEach
    public void deleteCreatedCouriers() {
        for (String createdLogin : createdLogins) {
            Response loginResponse = loginCourier(createdLogin, password);
            if (loginResponse.statusCode() == 200) {
                int courierId = loginResponse.path("id");
                deleteCourier(courierId);
            }
        }
        createdLogins.clear();
    }

    @Test
    @DisplayName("Создание курьера позитивный")
    @Description("Создаётся курьер с позитивно заполненными полями")
    public void createCourierPositive() {
        Response response = createCourier(login, password, name);
        createdLogins.add(login);
        bodyIsOk(response);
    }

    @Test
    @DisplayName("Невозможность повторного создания курьера")
    @Description("Попытка создания курьера два раза подряд")
    public void createCourierDuplicate() {
        createCourier(login, password, name);
        createdLogins.add(login);
        Response response = createCourier(login, password, name);
        bodyIsDuplicate(response);
    }

    @ParameterizedTest
    @MethodSource("courierWithoutOneFieldData")
    @DisplayName("Попытка создать курьера без одного поля")
    public void courierWithoutOneField(String login, String password, String name) {
        Response response = createCourier(login, password, name);
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

    @Step("Создание курьера: login={login}, password={password}, name={name}")
    public Response createCourier(String login, String password, String name) {
        Courier courier = new Courier(login, password, name);
        String json = new Gson().toJson(courier);

        return given()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .post("/api/v1/courier");
    }

    @Step("Проверка тела и кода ответа, что недостаточно поля")
    public void bodyIsNotOk(Response response){
        response.then().assertThat().statusCode(400)
                .and()
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Step("Логин курьера для получения id: login={login}")
    public Response loginCourier(String login, String password) {
        Courier courier = new Courier(login, password);
        String json = new Gson().toJson(courier);

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
