import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class CreateOrderTests extends BaseTest {
    private Integer track;

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
        Order order = new Order(colors);
        String json = new Gson().toJson(order);

        return given()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .post(Endpoints.ORDERS);
    }

    @Step("Отмена заказа с track={track}")
    public void cancelOrder(int track) {
        Order order = new Order(track);
        String json = new Gson().toJson(order);

        given()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .put(Endpoints.ORDERS_CANCEL);
    }

    @Step("Тело ответа содержит track")
    public void bodyHaveTrack(Response response) {
        response.then().assertThat()
                .statusCode(201)
                .body("track", notNullValue());
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
