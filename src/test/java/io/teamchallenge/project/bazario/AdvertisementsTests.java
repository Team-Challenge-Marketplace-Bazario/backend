package io.teamchallenge.project.bazario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.teamchallenge.project.bazario.web.dto.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Collections;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdvertisementsTests {

    private static String user1Email;
    private static String user2Email;
    private static String password;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void setup() {
        final var currentTime = System.currentTimeMillis();
        user1Email = String.format("user1_%d@server.com", currentTime);
        user2Email = String.format("user2_%d@server.com", currentTime);
        password = "111111";
    }

    @Test
    void getAdvByIdTest() throws JsonProcessingException {
        // 0.1 register and save a user1's token

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

    @Test
    void updateAdvertisementTest() throws JsonProcessingException {
        /// setup
        // create two users
        final var loginResponse1 = registerUserAndGetTokens(user1Email, password);
        final var loginResponse2 = registerUserAndGetTokens(user2Email, password);

        // create adv1 with user1
        final var advertisement = createAdvertisement(new AdvertisementDto(null, "Adv title", "Adv description",
                "123.45", true, Collections.emptyList(), null), loginResponse1.accessToken());

        /// test
        // update adv1 with user1 (should be success and fields must change accordingly)
        webTestClient.put()
                .uri("/adv/" + advertisement.getId())
                .header("Authorization", "Bearer " + loginResponse1.accessToken())
                .bodyValue(new AdvertisementDto(null, "updated title", "updated description", "234.56",
                        false, Collections.emptyList(), null))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(advertisement.getId())
                .jsonPath("$.title").isEqualTo("updated title")
                .jsonPath("$.description").isEqualTo("updated description")
                .jsonPath("$.price").isEqualTo("234.56")
                .jsonPath("$.status").isEqualTo(false);

        // update no existing adv with user1 (should be not found)
        webTestClient.put()
                .uri("/adv/" + 1_000_000_000)
                .header("Authorization", "Bearer " + loginResponse1.accessToken())
                .bodyValue(new AdvertisementDto(null, "updated title 2", "updated description 2", "234.56",
                        false, Collections.emptyList(), null))
                .exchange()
                .expectStatus().isNotFound();

        // update adv1 with user2 (should be not found)
        webTestClient.put()
                .uri("/adv/" + advertisement.getId())
                .header("Authorization", "Bearer " + loginResponse2.accessToken())
                .bodyValue(new AdvertisementDto(null, "updated title", "updated description", "234.56",
                        false, Collections.emptyList(), null))
                .exchange()
                .expectStatus().isNotFound();

        // update adv with no user (should be unauthorized)
        webTestClient.put()
                .uri("/adv/" + advertisement.getId())
                .bodyValue(new AdvertisementDto(null, "updated title", "updated description", "234.56",
                        false, Collections.emptyList(), null))
                .exchange()
                .expectStatus().isUnauthorized();
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
