import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class Client {
    @Step("Создание курьера: login={login}, password={password}, name={name}")
    public Response createCourier(String login, String password, String name) {
        Courier courier = new Courier(login, password, name);
        String json = new Gson().toJson(courier);

        return given()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .post(Endpoints.COURIER);
    }

    @Step("Логин курьера: login={login}, password={password}")
    public Response loginCourier(String login, String password) {
        Courier courier = new Courier(login, password);
        String json = new Gson().toJson(courier);

        return given()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .post(Endpoints.COURIER_LOGIN);
    }

    @Step("Удаление курьера с id={courierId}")
    public Response deleteCourier(int courierId) {
        return given()
                .when()
                .delete(Endpoints.COURIER + courierId);
    }
}