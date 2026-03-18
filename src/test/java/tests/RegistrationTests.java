package tests;

import models.login.FieldRequiredResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.registration.RegistrationSpec.*;
import static tests.TestData.*;

public class RegistrationTests extends TestBase {

    String username;
    String password;

    @BeforeEach
    public void prepareTestData() {
        Faker faker = new Faker();
        username = faker.name().firstName();
        password = faker.name().firstName();
    }

    @Test
    @DisplayName("Регистрация с валидными данными")
    public void successfulRegistration() {
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
        assertThat(registrationResponse.firstName()).isEqualTo("");
        assertThat(registrationResponse.lastName()).isEqualTo("");
        assertThat(registrationResponse.email()).isEqualTo("");

        assertThat(registrationResponse.remoteAddr()).matches(REGISTRATION_IP_REGEXP);
    }

    @Test
    @DisplayName("Регистрация существующего пользователя")
    public void existingUserWrongRegistration() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(username, password);

        SuccessfulRegistrationResponseModel firstRegistrationResponse = given(requestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successRegistrationResponseSpec)
                .extract()
                .as(SuccessfulRegistrationResponseModel.class);

        assertThat(firstRegistrationResponse.username()).isEqualTo(username);

        FieldRequiredResponseModel secondRegistrationResponse = given(requestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(negativeRegistrationResponseSpec)
                .extract()
                .as(FieldRequiredResponseModel.class);

        String actualError = secondRegistrationResponse.username().get(0);
        assertThat(actualError).isEqualTo(REGISTRATION_EXISTING_USER_ERROR);
    }

    @Test
    @DisplayName("Регистрация с пустым полем \"Username\"")
    public void emptyUsernameFieldRegistration() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel("", password);

        FieldRequiredResponseModel emptyUserResponseModel  = given(requestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(negativeRegistrationResponseSpec)
                .extract()
                .as(FieldRequiredResponseModel.class);

        String actualError = emptyUserResponseModel.username().get(0);
        assertThat(actualError).isEqualTo(EMPTY_FIELD_ERROR);
    }

    @Test
    @DisplayName("Регистрация c \"Username\" длиной 151 символ")
    public void inputMoreThan150CharactersRegistration() {
        Faker faker = new Faker();
        username = faker.lorem().characters(151);
        password = faker.name().firstName();

        RegistrationBodyModel registrationData = new RegistrationBodyModel(username, password);

        FieldRequiredResponseModel longUserResponseModel  = given(requestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(negativeRegistrationResponseSpec)
                .extract()
                .as(FieldRequiredResponseModel.class);

        String actualError = longUserResponseModel.username().get(0);
        assertThat(actualError).isEqualTo(MORE_THAN_150_CHARACTERS_ERROR);
    }
}
