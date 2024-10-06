package io.teamchallenge.project.bazario;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.teamchallenge.project.bazario.web.controller.CreateCommentRequest;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;

import static io.teamchallenge.project.bazario.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = {"file:.env_test_local"})
public class CommentTests {

    @Autowired
    private WebTestClient webTestClient;

    private String user1Email;
    private String user2Email;
    private String password;

    private AdvertisementDto advDto1;

    @BeforeEach
    public void setUp() {
        final var currentTime = System.currentTimeMillis();
        user1Email = String.format("user1_%d@server.com", currentTime);
        user2Email = String.format("user2_%d@server.com", currentTime);
        password = "111111";

        advDto1 = new AdvertisementDto(
                null, "Title User1", "Description User1", null, "123.45", true, Collections.emptyList(), null);
    }

    @Test
    public void addCommentTest() throws JsonProcessingException {
        // create user1 and user2
        final var login1 = registerUserAndGetTokens(webTestClient, user1Email, password);
        final var login2 = registerUserAndGetTokens(webTestClient, user2Email, password);

        // create adv1 by user1
        final var adv1 = createAdvertisement(webTestClient, advDto1, login1.accessToken());

        // add comment to adv1 without authentication (must be unauthorized)
        addComment(webTestClient, adv1, new CreateCommentRequest("addCommentTest comment"), null)
                .expectStatus().isUnauthorized();

        // add comment to adv1 as user1 (must be bad request)
        addComment(webTestClient, adv1, new CreateCommentRequest("addCommentTest comment"), login1.accessToken())
                .expectStatus().isBadRequest();

        // add invalid comment to adv1 as user2 (must be bad request)
        addComment(webTestClient, adv1, new CreateCommentRequest(":)"), login2.accessToken())
                .expectStatus().isBadRequest();

        // make adv1 inactive
        var updatedAdv1 = updateAdvertisement(webTestClient,
                new AdvertisementDto(adv1.getId(), null, null, null, null, false, Collections.emptyList(), null),
                login1.accessToken());

        assertFalse(updatedAdv1.getStatus());

        // add comment to adv1 as user2 (must be not found)
        addComment(webTestClient, adv1, new CreateCommentRequest("addCommentTest comment"), login2.accessToken())
                .expectStatus().isNotFound();

        // make adv1 active
        updatedAdv1 = updateAdvertisement(webTestClient,
                new AdvertisementDto(adv1.getId(), null, null, null, null, true, Collections.emptyList(), null),
                login1.accessToken());

        assertTrue(updatedAdv1.getStatus());

        // add comment as comment1 to adv1 as user2 (must be ok)
        addComment(webTestClient, adv1, new CreateCommentRequest("addCommentTest comment"), login2.accessToken())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.description").isEqualTo("addCommentTest comment")
                .jsonPath("$.user").exists();

        // get list of all comments of adv1
        final var commentList = getComments(webTestClient, adv1);

        // make sure that there is one comment comment1
        assertEquals(1, commentList.size());
        assertEquals("addCommentTest comment", commentList.get(0).getDescription());

        // delete adv1 as user1
        deleteAdvertisement(webTestClient, adv1, login1.accessToken())
                .expectStatus().isOk();

        // get list of all comments of adv1 (must be not found)
        getCommentsAsResponse(webTestClient, adv1)
                .expectStatus().isNotFound();
    }
}
