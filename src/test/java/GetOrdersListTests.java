import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetOrdersListTests {
    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Получение списка заказов")
    public void getOrdersList() {
        Response response = getOrderList();
        bodyHaveOrdersList(response);
    }

    @Step("Запросить список заказов")
    public Response getOrderList() {
        return given()
                .header("Content-type", "application/json")
                .get("/api/v1/orders");
    }

    @Step("Проверить что тело возвращает список заказов")
    public void bodyHaveOrdersList(Response response) {
        response.then().assertThat()
                .statusCode(200)
                .body("orders", not(empty()));
    }
}
