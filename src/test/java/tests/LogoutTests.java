package tests;

import io.restassured.response.Response;
import models.login.LoginBodyModel;
import models.login.SuccessfulLoginResponseModel;
import models.logout.FieldNullResponseModel;
import models.logout.LogoutBodyModel;
import models.logout.UnauthorizedResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.login.LoginSpec.successLoginResponseSpec;
import static specs.logout.LogoutSpec.*;
import static specs.registration.RegistrationSpec.requestSpec;
import static specs.registration.RegistrationSpec.successRegistrationResponseSpec;
import static tests.TestData.*;

public class LogoutTests extends TestBase {

    String username;
    String password;

    @BeforeEach
    public void prepareTestData() {
        Faker faker = new Faker();
        username = faker.name().firstName();
        password = faker.name().firstName();
    }

    @Test
    @DisplayName("Успешный выход из учетной записи")
    public void successfulLogout() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(username, password);

        SuccessfulRegistrationResponseModel registrationResponse = given(requestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successRegistrationResponseSpec)
                .extract()
                .as(SuccessfulRegistrationResponseModel.class);

        assertThat(registrationResponse.username()).isEqualTo(username);

        LoginBodyModel loginData = new LoginBodyModel(username, password);

        SuccessfulLoginResponseModel loginResponse = given(requestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successLoginResponseSpec)
                .extract()
                .as(SuccessfulLoginResponseModel.class);

        String actualTokenRefresh = loginResponse.refresh();

        LogoutBodyModel logoutData = new LogoutBodyModel(actualTokenRefresh);

        Response logoutResponse = given(requestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(successLogoutResponseSpec)
                .extract()
                .response();

        assertThat(logoutResponse.body().asString()).isEqualTo("{}");

    }

    @Test
    @DisplayName("Refresh = null")
    public void transmittingZeroRefresh() {

        String refresh = null;

        LogoutBodyModel logoutData = new LogoutBodyModel(refresh);

        FieldNullResponseModel refreshNullResponse = given(requestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(nullFieldLogoutResponseSpec)
                .extract()
                .as(FieldNullResponseModel.class);

        String actualRefreshError = refreshNullResponse.refresh().get(0);
        assertThat(actualRefreshError).isEqualTo(NULL_FIELD_ERROR);

    }

    @Test
    @DisplayName("Невалидный refresh")
    public void passingInvalidRefresh() {

        String refresh = "string";

        LogoutBodyModel logoutData = new LogoutBodyModel(refresh);

        UnauthorizedResponseModel logoutUnauthorizedResponse = given(requestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(unauthorizedLogoutResponseSpec)
                .extract()
                .as(UnauthorizedResponseModel.class);

        String actualDetailError = logoutUnauthorizedResponse.detail();
        String actualCodeError = logoutUnauthorizedResponse.code();
        assertThat(actualDetailError).isEqualTo(TOKEN_INVALID_ERROR);
        assertThat(actualCodeError).isEqualTo(TOKEN_NOT_VALID_ERROR);
    }
}
