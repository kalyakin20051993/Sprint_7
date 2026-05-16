import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class CreateOrderTests {
    private Integer track;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @AfterEach
    public void cancelOrderAfterTest() {
        if (track != null) {
            cancelOrder(track);
            track = null;
        }
    }

    @ParameterizedTest
    @MethodSource("orderColorData")
    @DisplayName("Создание заказа с разными вариантами цвета")
    public void createOrderWithDifferentColors(String[] colors) {
        Response response = createOrder(colors);
        bodyHaveTrack(response);
        track = response.path("track");
    }

    @Step("Создание заказа: color={colors}")
    public Response createOrder(String[] colors) {
        String json = "{"
                + "\"color\": " + colorsToJsonArray(colors)
                + "}";

        return given()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .post("/api/v1/orders");
    }

    @Step("Отмена заказа с track={track}")
    public void cancelOrder(int track) {
        String json = "{\"track\": " + track + "}";

        given()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .put("/api/v1/orders/cancel");
    }

    @Step("Тело ответа содержит track")
    public void bodyHaveTrack(Response response) {
        response.then().assertThat()
                .statusCode(201)
                .body("track", notNullValue());
    }


    private String colorsToJsonArray(String[] colors) {
        return Arrays.stream(colors)
                .map(color -> "\"" + color + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    static Stream<Arguments> orderColorData() {
        return Stream.of(
                Arguments.of((Object) new String[]{"BLACK"}),
                Arguments.of((Object) new String[]{"GREY"}),
                Arguments.of((Object) new String[]{"BLACK", "GREY"}),
                Arguments.of((Object) new String[]{})
        );
    }
}
