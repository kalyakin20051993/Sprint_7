import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetOrdersListTests extends BaseTest {
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
