package io.teamchallenge.project.bazario;

import io.teamchallenge.project.bazario.web.dto.LoginRequest;
import io.teamchallenge.project.bazario.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = {"file:.env_test_local"})
class AuthTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void registerAndLoginUser() {
        final var timeMillis = System.currentTimeMillis();
        final var email = "user_" + timeMillis + "@server.com";
        final var password = "111111";

        final var registerRequest = new RegisterRequest("John", "Doe", "1234567890", email, password);

        webTestClient.post()
                .uri("/auth/register")
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        final var loginRequest = new LoginRequest(email, password);

        webTestClient.post()
                .uri("/auth/login")
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.refreshToken").isNotEmpty()
                .jsonPath("$.accessToken").isNotEmpty();
    }
}
