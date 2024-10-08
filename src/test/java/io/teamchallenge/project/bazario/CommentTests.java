package io.teamchallenge.project.bazario;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.teamchallenge.project.bazario.web.controller.CreateCommentRequest;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = {"file:.env_test_local"})
@ActiveProfiles("test")
@Sql("classpath:clean-db.sql")
public class CommentTests {

    @Autowired
    private WebTestClient webTestClient;

    private TestHelper helper;

    private String user1Email;
    private String user1Phone;
    private String user2Email;
    private String user2Phone;
    private String password;

    private AdvertisementDto advDto1;


    @BeforeEach
    public void setUp() {
        final var currentTime = System.currentTimeMillis();
        user1Email = String.format("user1_%d@server.com", currentTime);
        user1Phone = String.format("+38%010d", currentTime % 10000000000L);

        user2Email = String.format("user2_%d@server.com", currentTime);
        user2Phone = String.format("+38%010d", (currentTime + 1) % 10000000000L);
        password = "111111";

        advDto1 = new AdvertisementDto(null, "Title User1", "Description User1", null, "123.45", true);

        helper = new TestHelper();
        helper.setWebTestClient(webTestClient);
    }

    @Test
    public void addCommentTest() throws JsonProcessingException {
        // create user1 and user2
        final var login1 = helper.registerUserAndGetTokens(user1Email, user1Phone, password);
        final var login2 = helper.registerUserAndGetTokens(user2Email, user2Phone, password);

        // create adv1 by user1
        final var adv1 = helper.createAdvertisement(advDto1, login1.accessToken());

        // add comment to adv1 without authentication (must be unauthorized)
        helper.addComment(adv1, new CreateCommentRequest("addCommentTest comment"), null)
                .expectStatus().isUnauthorized();

        // add comment to adv1 as user1 (must be bad request)
        helper.addComment(adv1, new CreateCommentRequest("addCommentTest comment"), login1.accessToken())
                .expectStatus().isBadRequest();

        // add invalid comment to adv1 as user2 (must be bad request)
        helper.addComment(adv1, new CreateCommentRequest(":)"), login2.accessToken())
                .expectStatus().isBadRequest();

        // make adv1 inactive
        var updatedAdv1 = helper.updateAdvertisement(new AdvertisementDto(adv1.getId(), null, null, null, null, false),
                login1.accessToken());

        assertFalse(updatedAdv1.getStatus());

        // add comment to adv1 as user2 (must be not found)
        helper.addComment(adv1, new CreateCommentRequest("addCommentTest comment"), login2.accessToken())
                .expectStatus().isNotFound();

        // make adv1 active
        updatedAdv1 = helper.updateAdvertisement(new AdvertisementDto(adv1.getId(), null, null, null, null, true),
                login1.accessToken());

        assertTrue(updatedAdv1.getStatus());

        // add comment as comment1 to adv1 as user2 (must be ok)
        helper.addComment(adv1, new CreateCommentRequest("addCommentTest comment"), login2.accessToken())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.description").isEqualTo("addCommentTest comment")
                .jsonPath("$.user").exists();

        // get list of all comments of adv1
        final var commentList = helper.getComments(adv1);

        // make sure that there is one comment comment1
        assertEquals(1, commentList.size());
        assertEquals("addCommentTest comment", commentList.get(0).getDescription());

        // delete adv1 as user1
        helper.deleteAdvertisement(adv1, login1.accessToken())
                .expectStatus().isOk();

        // get list of all comments of adv1 (must be not found)
        helper.getCommentsAsResponse(adv1)
                .expectStatus().isNotFound();
    }
}
