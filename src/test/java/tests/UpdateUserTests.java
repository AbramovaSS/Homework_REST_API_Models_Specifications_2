package tests;

import models.login.FieldRequiredResponseModel;
import models.login.LoginBodyModel;
import models.login.SuccessfulLoginResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import models.update.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static specs.BaseSpec.requestSpec;
import static specs.update.UpdateSpec.*;
import static specs.login.LoginSpec.successLoginResponseSpec;
import static specs.registration.RegistrationSpec.successRegistrationResponseSpec;
import static tests.TestData.*;

public class UpdateUserTests extends TestBase {
    TestData testData = new TestData();

    @Test
    @DisplayName("Успешное обновление данных пользователя")
    public void successfulRegistration() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        SuccessfulRegistrationResponseModel registrationResponse = given(requestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successRegistrationResponseSpec)
                .extract()
                .as(SuccessfulRegistrationResponseModel.class);

        Assertions.assertThat(registrationResponse.username()).isEqualTo(testData.username);

        String registrationIp = registrationResponse.remoteAddr();

        LoginBodyModel loginData = new LoginBodyModel(testData.username, testData.password);

        SuccessfulLoginResponseModel loginResponse = given(requestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successLoginResponseSpec)
                .extract()
                .as(SuccessfulLoginResponseModel.class);

        String expectedTokenPath = LOGIN_TOKEN_PREFIX;
        String actualAccess = loginResponse.access();
        String actualRefresh = loginResponse.refresh();

        Assertions.assertThat(actualAccess).startsWith(expectedTokenPath);
        Assertions.assertThat(actualRefresh).startsWith(expectedTokenPath);
        Assertions.assertThat(actualAccess).isNotEqualTo(actualRefresh);

        UpdateBodyModel updateData = new UpdateBodyModel(testData.username, testData.firstName, testData.lastName, testData.email);

        SuccessfulUpdateResponseModel updateResponse = given(requestSpec)
                .header("Authorization", "Bearer " + actualAccess)
                .body(updateData)
                .when()
                .put("/users/me/")
                .then()
                .spec(successUpdateResponseSpec)
                .extract()
                .as(SuccessfulUpdateResponseModel.class);

        Assertions.assertThat(updateResponse.id()).isEqualTo(registrationResponse.id());
        Assertions.assertThat(updateResponse.username()).isEqualTo(testData.username);
        Assertions.assertThat(updateResponse.firstName()).isEqualTo(testData.firstName);
        Assertions.assertThat(updateResponse.lastName()).isEqualTo(testData.lastName);
        Assertions.assertThat(updateResponse.email()).isEqualTo(testData.email);

        Assertions.assertThat(registrationResponse.remoteAddr()).matches(REGISTRATION_IP_REGEXP);

        String updateIp = updateResponse.remoteAddr();
        Assertions.assertThat(registrationIp).isEqualTo(updateIp);
    }

    @Test
    @DisplayName("Успешное добавление \"email\"")
    public void successfulEmailUpdate() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        SuccessfulRegistrationResponseModel registrationResponse = given(requestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successRegistrationResponseSpec)
                .extract()
                .as(SuccessfulRegistrationResponseModel.class);

        Assertions.assertThat(registrationResponse.username()).isEqualTo(testData.username);

        String registrationIp = registrationResponse.remoteAddr();

        LoginBodyModel loginData = new LoginBodyModel(testData.username, testData.password);

        SuccessfulLoginResponseModel loginResponse = given(requestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successLoginResponseSpec)
                .extract()
                .as(SuccessfulLoginResponseModel.class);

        String expectedTokenPath = LOGIN_TOKEN_PREFIX;
        String actualAccess = loginResponse.access();
        String actualRefresh = loginResponse.refresh();

        Assertions.assertThat(actualAccess).startsWith(expectedTokenPath);
        Assertions.assertThat(actualRefresh).startsWith(expectedTokenPath);
        Assertions.assertThat(actualAccess).isNotEqualTo(actualRefresh);

        UpdateEmailBodyModel updateEmailData = new UpdateEmailBodyModel(testData.email);

        SuccessfulUpdateResponseModel updateResponse = given(requestSpec)
                .header("Authorization", "Bearer " + actualAccess)
                .body(updateEmailData)
                .when()
                .patch("/users/me/")
                .then()
                .spec(successUpdateResponseSpec)
                .extract()
                .as(SuccessfulUpdateResponseModel.class);

        Assertions.assertThat(updateResponse.id()).isEqualTo(registrationResponse.id());
        Assertions.assertThat(updateResponse.username()).isEqualTo(testData.username);
        Assertions.assertThat(updateResponse.firstName()).isEqualTo("");
        Assertions.assertThat(updateResponse.lastName()).isEqualTo("");
        Assertions.assertThat(updateResponse.email()).isEqualTo(testData.email);

        Assertions.assertThat(registrationResponse.remoteAddr()).matches(REGISTRATION_IP_REGEXP);

        String updateIp = updateResponse.remoteAddr();
        Assertions.assertThat(registrationIp).isEqualTo(updateIp);
    }

    @Test
    @DisplayName("Поле \"username\" обязательно для заполнения")
    public void UsernameFieldRequiredUpdate() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        SuccessfulRegistrationResponseModel registrationResponse = given(requestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successRegistrationResponseSpec)
                .extract()
                .as(SuccessfulRegistrationResponseModel.class);

        Assertions.assertThat(registrationResponse.username()).isEqualTo(testData.username);

        LoginBodyModel loginData = new LoginBodyModel(testData.username, testData.password);

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

        Assertions.assertThat(actualAccess).startsWith(expectedTokenPath);
        Assertions.assertThat(actualRefresh).startsWith(expectedTokenPath);
        Assertions.assertThat(actualAccess).isNotEqualTo(actualRefresh);

        UpdateWithoutUsernameBodyModel updateWithoutUsernameData = new UpdateWithoutUsernameBodyModel(testData.firstName,
                                                                                              testData.lastName, testData.email);

        FieldRequiredResponseModel updateWithoutUsernameResponse = given(requestSpec)
                .header("Authorization", "Bearer " + actualAccess)
                .body(updateWithoutUsernameData)
                .when()
                .put("/users/me/")
                .then()
                .spec(fieldRequiredResponseSpec)
                .extract()
                .as(FieldRequiredResponseModel.class);

        String actualUsernameError = updateWithoutUsernameResponse.username().get(0);
        Assertions.assertThat(actualUsernameError).isEqualTo(FIELD_IS_REQUIRED);
    }
}
