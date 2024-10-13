package io.teamchallenge.project.bazario;

import io.teamchallenge.project.bazario.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;

import static io.teamchallenge.project.bazario.TestHelper.MAIL_PIT_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = {"file:.env_test_local"})
@ActiveProfiles("test")
@Sql("classpath:clean-db.sql")
class AuthTests {

    @Autowired
    private WebTestClient webTestClient;

    private String email;
    private String phone;
    private String password;

    @BeforeEach
    void setUp() {
        final var timeMillis = System.currentTimeMillis();
        email = "user_" + timeMillis + "@server.com";
        phone = String.format("+38%010d", timeMillis % 10000000000L);
        password = "111111";
    }

    @Test
    void registerAndLogin() {

        // register
        final var registerRequest = new RegisterRequest("Johnny", "Mnemonic", email, phone, password);
        webTestClient.post()
                .uri("/auth/register")
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().isOk();

        // retrieve token
        final var mailMessage = webTestClient.get()
                .uri(URI.create(MAIL_PIT_URL))
                .exchange()
                .expectStatus().isOk()
                .expectBody(MailMessage.class)
                .returnResult().getResponseBody();

        assertNotNull(mailMessage);
        assertNotNull(mailMessage.Text());

        final var elements = mailMessage.Text().split("\\?token=");
        assertEquals(2, elements.length);

        // verify email
        final var verifyEmailRequest = new VerifyEmailRequest(elements[1].trim());
        webTestClient.post()
                .uri("/auth/verify-email")
                .bodyValue(verifyEmailRequest)
                .exchange()
                .expectStatus().isOk();

        // login
        final var loginRequest = new LoginRequest(email, password);
        final var tokens = webTestClient.post()
                .uri("/auth/login")
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .returnResult().getResponseBody();

        assertNotNull(tokens);

        // get user info
        final var userDto = webTestClient.get()
                .uri("/user")
                .header("Authorization", "Bearer " + tokens.accessToken())
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .returnResult().getResponseBody();

        assertNotNull(userDto);
        assertEquals(email, userDto.email());
        assertNotNull(userDto.roles());
        assertThat(userDto.roles()).containsExactlyInAnyOrder("USER");
    }

    @Test
    void changePassword() {
        // register (must be ok)
        final var registerRequest = new RegisterRequest("Johnny", "Mnemonic", email, phone, password);
        webTestClient.post()
                .uri("/auth/register")
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().isOk();

        // send request to restore password (must be 401)
        final var usernameRequest = new UsernameRequest(email);
        webTestClient.post()
                .uri("/auth/send-restore-password")
                .bodyValue(usernameRequest)
                .exchange()
                .expectStatus().isBadRequest();

        // verify email (must be ok)
        final var mailMessage = webTestClient.get()
                .uri(URI.create(MAIL_PIT_URL))
                .exchange()
                .expectStatus().isOk()
                .expectBody(MailMessage.class)
                .returnResult().getResponseBody();

        assertNotNull(mailMessage);
        assertNotNull(mailMessage.Text());

        var elements = mailMessage.Text().split("\\?token=");
        assertEquals(2, elements.length);

        final var verifyEmailRequest = new VerifyEmailRequest(elements[1].trim());
        webTestClient.post()
                .uri("/auth/verify-email")
                .bodyValue(verifyEmailRequest)
                .exchange()
                .expectStatus().isOk();

        // login with old password (must be ok)
        final var oldTokens = webTestClient.post()
                .uri("/auth/login")
                .bodyValue(new LoginRequest(email, password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .returnResult().getResponseBody();

        assertNotNull(oldTokens);


        // send request to restore password (must be ok)
        webTestClient.post()
                .uri("/auth/send-restore-password")
                .bodyValue(new UsernameRequest(email))
                .exchange()
                .expectStatus().isOk();

        final var restorePasswordMailMessage = webTestClient.get()
                .uri(URI.create(MAIL_PIT_URL))
                .exchange()
                .expectStatus().isOk()
                .expectBody(MailMessage.class)
                .returnResult().getResponseBody();

        assertNotNull(restorePasswordMailMessage);
        assertNotNull(restorePasswordMailMessage.Text());

        elements = restorePasswordMailMessage.Text().split("\\?token=");
        assertEquals(2, elements.length);

        // restore password (must be ok)
        final var newPassword = "222222";
        webTestClient.post()
                .uri("/auth/restore-password")
                .bodyValue(new VerifyPasswordRequest(elements[1].trim(), newPassword))
                .exchange()
                .expectStatus().isOk();

        // login with old password (must be 401)
        webTestClient.post()
                .uri("/auth/login")
                .bodyValue(new LoginRequest(email, password))
                .exchange()
                .expectStatus().isUnauthorized();

        // login with new password (must be ok)
        webTestClient.post()
                .uri("/auth/login")
                .bodyValue(new LoginRequest(email, newPassword))
                .exchange()
                .expectStatus().isOk();
    }
}
