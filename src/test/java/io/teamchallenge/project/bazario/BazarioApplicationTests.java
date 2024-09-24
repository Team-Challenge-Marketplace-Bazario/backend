package io.teamchallenge.project.bazario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import io.teamchallenge.project.bazario.web.dto.LoginRequest;
import io.teamchallenge.project.bazario.web.dto.LoginResponse;
import io.teamchallenge.project.bazario.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Collections;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BazarioApplicationTests {

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

    @Test
    void getAdvByIdTest() throws JsonProcessingException {
        // 0.1 register and save a user1's token
        final var currentTime = System.currentTimeMillis();
        final var user1Email = String.format("user1_%d@server.com", currentTime);
        final var user2Email = String.format("user2_%d@server.com", currentTime);
        final var password = "111111";
        var loginResponse1 = registerUserAndGetTokens(user1Email, password);
        var loginResponse2 = registerUserAndGetTokens(user2Email, password);

        // 1.0 create active adv using user1's credentials
        final var activeAdvertisement = createAdvertisement(new AdvertisementDto(null, "Adv title", "Adv description",
                "123.45", true, Collections.emptyList(), null), loginResponse1.accessToken());

        // 1.1 create inactive adv using user1's credentials
        final var inActiveAdvertisement = createAdvertisement(new AdvertisementDto(null, "Adv title", "Adv description",
                "123.45", false, Collections.emptyList(), null), loginResponse1.accessToken());

        // 2.0 get active adv using user1's credential
        getAdvertisementById(activeAdvertisement.getId(), loginResponse1.accessToken())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(activeAdvertisement.getId());

        // 2.1 get active adv using user2's credential
        getAdvertisementById(activeAdvertisement.getId(), loginResponse2.accessToken())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(activeAdvertisement.getId());

        // 2.2 get active adv using no credential
        getAdvertisementById(activeAdvertisement.getId(), null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(activeAdvertisement.getId());

        // 3.0 get inactive adv using user1's credential
        getAdvertisementById(inActiveAdvertisement.getId(), loginResponse1.accessToken())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(inActiveAdvertisement.getId());

        // 3.1 get inactive adv using user2's credential
        getAdvertisementById(inActiveAdvertisement.getId(), loginResponse2.accessToken())
                .expectStatus().isNotFound();

        // 3.2 get inactive adv using no credential
        getAdvertisementById(inActiveAdvertisement.getId(), null)
                .expectStatus().isNotFound();
    }

    private LoginResponse registerUserAndGetTokens(String email, String password) {
        final var registerRequest = new RegisterRequest("John", "Doe", "1234567890", email, password);

        webTestClient.post()
                .uri("/auth/register")
                .bodyValue(registerRequest)
                .exchange();

        final var loginRequest = new LoginRequest(email, password);

        return webTestClient.post()
                .uri("/auth/login")
                .bodyValue(loginRequest)
                .exchange()
                .returnResult(LoginResponse.class)
                .getResponseBody().blockFirst();
    }

    private AdvertisementDto createAdvertisement(AdvertisementDto dto, String token) throws JsonProcessingException {
        final var objectMapper = new ObjectMapper();
        final var jsonString = objectMapper.writeValueAsString(dto);

        final var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder
                .part("adv", jsonString)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        return webTestClient.post()
                .uri("/adv")
                .header("Authorization", "Bearer " + token)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .returnResult(AdvertisementDto.class)
                .getResponseBody().blockFirst();
    }

    private WebTestClient.ResponseSpec getAdvertisementById(Long id, String token) {
        return webTestClient.get()
                .uri("/adv/" + id)
                .headers(header -> {
                    if (token != null) {
                        header.add("Authorization", "Bearer " + token);
                    }
                })
                .exchange();
    }
}
