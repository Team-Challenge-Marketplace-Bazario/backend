package io.teamchallenge.project.bazario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import io.teamchallenge.project.bazario.web.dto.LoginRequest;
import io.teamchallenge.project.bazario.web.dto.LoginResponse;
import io.teamchallenge.project.bazario.web.dto.RegisterRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;

public interface Helper {
    static LoginResponse registerUserAndGetTokens(WebTestClient webTestClient, String email, String password) {
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

    static AdvertisementDto createAdvertisement(WebTestClient webTestClient, AdvertisementDto dto, String token) throws JsonProcessingException {
        return createAdvertisement(webTestClient, dto, Collections.emptyList(), token);
    }

    static AdvertisementDto createAdvertisement(WebTestClient webTestClient, AdvertisementDto dto, List<String> files, String token) throws JsonProcessingException {
        final var objectMapper = new ObjectMapper();
        final var jsonString = objectMapper.writeValueAsString(dto);

        final var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder
                .part("adv", jsonString)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        for (String file : files) {
            final var resource = new ClassPathResource(file);
            multipartBodyBuilder.part("pics", resource);
        }

        return webTestClient.post()
                .uri("/adv")
                .header("Authorization", "Bearer " + token)
                .bodyValue(multipartBodyBuilder.build())
                .exchange()
                .returnResult(AdvertisementDto.class)
                .getResponseBody().blockFirst();
    }

    static AdvertisementDto updateAdvertisement(WebTestClient webTestClient, AdvertisementDto dto, String token) {
        return webTestClient.put()
                .uri("/adv/" + dto.getId())
                .header("Authorization", "Bearer " + token)
                .bodyValue(dto)
                .exchange()
                .returnResult(AdvertisementDto.class)
                .getResponseBody().blockFirst();
    }

    static List<AdvertisementDto> getFavList(WebTestClient webTestClient, String token) {
        return webTestClient.get()
                .uri("/fav")
                .headers(header -> {
                    if (token != null) {
                        header.set("Authorization", "Bearer " + token);
                    }
                })
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AdvertisementDto.class)
                .returnResult().getResponseBody();
    }

    static WebTestClient.ResponseSpec addToFavList(WebTestClient webTestClient, AdvertisementDto advertisement, String token) {
        return webTestClient.post()
                .uri("/fav/" + advertisement.getId())
                .headers(header -> {
                    if (token != null) {
                        header.set("Authorization", "Bearer " + token);
                    }
                })
                .exchange();
    }

    static WebTestClient.ResponseSpec deleteFromFavList(WebTestClient webTestClient,
                                                        AdvertisementDto advertisement,
                                                        String token) {
        return webTestClient.delete()
                .uri("/fav/" + advertisement.getId())
                .headers(header -> {
                    if (token != null) {
                        header.set("Authorization", "Bearer " + token);
                    }
                })
                .exchange();
    }
}
