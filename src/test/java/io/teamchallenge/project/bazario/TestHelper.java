package io.teamchallenge.project.bazario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.teamchallenge.project.bazario.web.controller.CreateCommentRequest;
import io.teamchallenge.project.bazario.web.dto.*;
import lombok.Setter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Setter
public class TestHelper {

    public static final String MAIL_PIT_URL = "http://localhost:8025/api/v1/message/latest";

    private WebTestClient webTestClient;

    public LoginResponse registerUserAndGetTokens(String email, String phone, String password) {
        final var registerRequest = new RegisterRequest("Johny", "Mnemonic", email, phone, password);

        webTestClient.post()
                .uri("/auth/register")
                .bodyValue(registerRequest)
                .exchange();

        // retrieve token
        final var mailMessage = webTestClient.get()
                .uri(URI.create(MAIL_PIT_URL))
                .exchange()
                .expectStatus().isOk()
                .expectBody(MailMessage.class)
                .returnResult().getResponseBody();

        final var elements = Objects.requireNonNull(mailMessage).Text().split("\\?token=");

        // verify email
        webTestClient.post()
                .uri("/auth/verify-email")
                .bodyValue(new VerifyEmailRequest(elements[1].trim()))
                .exchange()
                .expectStatus().isOk();

        final var loginRequest = new LoginRequest(email, password);

        return webTestClient.post()
                .uri("/auth/login")
                .bodyValue(loginRequest)
                .exchange()
                .returnResult(LoginResponse.class)
                .getResponseBody().blockFirst();
    }

    public AdvertisementDto createAdvertisement(AdvertisementDto dto, String token) throws JsonProcessingException {
        return createAdvertisement(dto, Collections.emptyList(), token);
    }

    public AdvertisementDto createAdvertisement(AdvertisementDto dto, List<String> files, String token) throws JsonProcessingException {
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

    public AdvertisementDto updateAdvertisement(AdvertisementDto dto, String token) {
        return webTestClient.put()
                .uri("/adv/" + dto.getId())
                .header("Authorization", "Bearer " + token)
                .bodyValue(dto)
                .exchange()
                .returnResult(AdvertisementDto.class)
                .getResponseBody().blockFirst();
    }

    public WebTestClient.ResponseSpec getAdvertisementById(Long id, String token) {
        return webTestClient.get()
                .uri("/adv/" + id)
                .headers(header -> {
                    if (token != null) {
                        header.add("Authorization", "Bearer " + token);
                    }
                })
                .exchange();
    }

    public WebTestClient.ResponseSpec deleteAdvertisement(AdvertisementDto dto, String token) {
        return webTestClient.delete()
                .uri("/adv/" + dto.getId())
                .headers(header -> {
                    if (token != null) {
                        header.add("Authorization", "Bearer " + token);
                    }
                })
                .exchange();
    }

    public List<AdvertisementDto> getFavList(String token) {
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

    public WebTestClient.ResponseSpec addToFavList(AdvertisementDto advertisement, String token) {
        return webTestClient.post()
                .uri("/fav/" + advertisement.getId())
                .headers(header -> {
                    if (token != null) {
                        header.set("Authorization", "Bearer " + token);
                    }
                })
                .exchange();
    }

    public WebTestClient.ResponseSpec deleteFromFavList(AdvertisementDto advertisement, String token) {
        return webTestClient.delete()
                .uri("/fav/" + advertisement.getId())
                .headers(header -> {
                    if (token != null) {
                        header.set("Authorization", "Bearer " + token);
                    }
                })
                .exchange();
    }

    public WebTestClient.ResponseSpec addComment(AdvertisementDto adv, CreateCommentRequest commentRequest, String token) {

        return webTestClient.post()
                .uri("/comment/" + adv.getId())
                .headers(header -> {
                    if (token != null) {
                        header.set("Authorization", "Bearer " + token);
                    }
                })
                .bodyValue(commentRequest)
                .exchange();
    }

    public List<CommentDto> getComments(AdvertisementDto adv) {

        return webTestClient.get()
                .uri("/comment/" + adv.getId())
                .exchange()
                .expectBodyList(CommentDto.class)
                .returnResult()
                .getResponseBody();
    }

    public WebTestClient.ResponseSpec getCommentsAsResponse(AdvertisementDto adv) {

        return webTestClient.get()
                .uri("/comment/" + adv.getId())
                .exchange();
    }

    public WebTestClient.ResponseSpec getAdvertisementByFilter(AdvertisementFilter filter) {
        return getAdvertisementByFilter(filter, Collections.emptyList());
    }

    public WebTestClient.ResponseSpec getAdvertisementByFilter(AdvertisementFilter filter,
                                                               List<Pair<String, String>> sorting) {
        return webTestClient.get()
                .uri(builder -> {
                    builder.path("/adv");
                    if (filter != null) {
                        if (filter.title() != null) {
                            builder.queryParam("title", filter.title());
                        }
                        if (filter.category() != null) {
                            builder.queryParam("category", filter.category());
                        }
                    }

                    if (sorting != null && !sorting.isEmpty()) {
                        sorting.forEach(entry -> builder.queryParam(entry.getFirst(), entry.getSecond()));
                    }

                    builder.queryParam("ipp", 1000);

                    return builder.build();
                }).exchange();

    }

    public UserDto getUser(String token) {
        return webTestClient.get()
                .uri("/user")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectBody(UserDto.class)
                .returnResult().getResponseBody();
    }

    public static AdvertisementDto getActiveAdvDtoWithTitleAndCategory(String title, String category) {
        return new AdvertisementDto(null, title, title, category, "123.45", true);
    }
}
