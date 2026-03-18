package tests;

import models.login.LoginBodyModel;
import models.login.FieldRequiredResponseModel;
import models.login.SuccessfulLoginResponseModel;
import models.login.WrongCredentialsResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.login.LoginSpec.*;
import static specs.registration.RegistrationSpec.*;
import static tests.TestData.*;

public class LoginTests extends TestBase {

    String username;
    String password;

    @BeforeEach
    public void prepareTestData() {
        Faker faker = new Faker();
        username = faker.name().firstName();
        password = faker.name().firstName();
    }

    @Test
    @DisplayName("Успешная авторизация")
    public void successfulLogin() {
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
                .extract().as(SuccessfulLoginResponseModel.class);

        String expectedTokenPath = LOGIN_TOKEN_PREFIX;
        String actualAccess = loginResponse.access();
        String actualRefresh = loginResponse.refresh();

        assertThat(actualAccess).startsWith(expectedTokenPath);
        assertThat(actualRefresh).startsWith(expectedTokenPath);
        assertThat(actualAccess).isNotEqualTo(actualRefresh);

    }
    @Test
    @DisplayName("Вход в аккаунт с неверным паролем")
    public void wrongCredentialLogin() {
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

        LoginBodyModel loginData = new LoginBodyModel(username, password + "1");

        WrongCredentialsResponseModel loginResponse = given(requestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(wrongCredentialLoginResponseSpec)
                .extract().as(WrongCredentialsResponseModel.class);

        String actualDetail = loginResponse.detail();
        assertThat(actualDetail).isEqualTo(LOGIN_WRONG_CREDENTIALS_ERROR);
    }

    @Test
    @DisplayName("Вход в аккаунт с незаполненным полем \"Username\"")
    public void emptyUsernameFieldLogin() {
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

        LoginBodyModel loginData = new LoginBodyModel("", password);

        FieldRequiredResponseModel emptyUsernameLoginResponse = given(requestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(emptyFieldLoginResponseSpec)
                .extract()
                .as(FieldRequiredResponseModel.class);

        String actualUsername = emptyUsernameLoginResponse.username().get(0);
        assertThat(actualUsername).isEqualTo(EMPTY_FIELD_ERROR);

    }

}
