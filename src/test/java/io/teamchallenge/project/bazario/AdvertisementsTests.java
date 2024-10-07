package io.teamchallenge.project.bazario;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = {"file:.env_test_local"})
@ActiveProfiles("test")
@Sql("classpath:clean-db.sql")
class AdvertisementsTests {

    @Autowired
    private WebTestClient webTestClient;

    private TestHelper helper;

    private String user1Email;
    private String user1Phone;

    private String user2Email;
    private String user2Phone;

    private String password;

    @BeforeEach()
    void setup() {
        final var currentTime = System.currentTimeMillis();
        user1Email = String.format("user1_%d@server.com", currentTime);
        user1Phone = String.format("+38%010d", currentTime % 10000000000L);

        user2Email = String.format("user2_%d@server.com", currentTime);
        user2Phone = String.format("+38%010d", (currentTime + 1) % 10000000000L);
        password = "111111";

        helper = new TestHelper();
        helper.setWebTestClient(webTestClient);
    }

    @Test
    void addAdvTest() {
        //todo: add advertisement test (with and without pictures, with and without category)

        /// add adv with picture

        /// add adv without picture

        /// add adv with category

        /// add adv without category
    }

    @Test
    void getAdvByIdTest() throws JsonProcessingException {
        // 0.1 register and save a user1's token

        var loginResponse1 = helper.registerUserAndGetTokens(user1Email, user1Phone, password);
        var loginResponse2 = helper.registerUserAndGetTokens(user2Email, user2Phone, password);

        // 1.0 create active adv using user1's credentials
        final var activeAdvertisement = helper.createAdvertisement(new AdvertisementDto(null, "Adv title", "getAdvByIdTest", null, "123.45", true),
                loginResponse1.accessToken());

        // 1.1 create inactive adv using user1's credentials
        final var inActiveAdvertisement = helper.createAdvertisement(new AdvertisementDto(null, "Adv title", "getAdvByIdTest", null, "123.45", false),
                loginResponse1.accessToken());

        // 2.0 get active adv using user1's credential
        helper.getAdvertisementById(activeAdvertisement.getId(), loginResponse1.accessToken())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(activeAdvertisement.getId());

        // 2.1 get active adv using user2's credential
        helper.getAdvertisementById(activeAdvertisement.getId(), loginResponse2.accessToken())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(activeAdvertisement.getId());

        // 2.2 get active adv using no credential
        helper.getAdvertisementById(activeAdvertisement.getId(), null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(activeAdvertisement.getId());

        // 3.0 get inactive adv using user1's credential
        helper.getAdvertisementById(inActiveAdvertisement.getId(), loginResponse1.accessToken())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(inActiveAdvertisement.getId());

        // 3.1 get inactive adv using user2's credential
        helper.getAdvertisementById(inActiveAdvertisement.getId(), loginResponse2.accessToken())
                .expectStatus().isNotFound();

        // 3.2 get inactive adv using no credential
        helper.getAdvertisementById(inActiveAdvertisement.getId(), null)
                .expectStatus().isNotFound();
    }

    @Test
    void getAdvById_CheckUser() throws JsonProcessingException {
        // register user
        var tokens1 = helper.registerUserAndGetTokens(user1Email, user1Phone, password);
        var tokens2 = helper.registerUserAndGetTokens(user2Email, user2Phone, password);

        // create adv
        final var activeAdvertisement = helper.createAdvertisement(
                new AdvertisementDto(null, "Adv title", "getAdvById_CheckUser", null, "123.45", true), tokens1.accessToken());

        // get adv by id with auth token (must be user field with user in response)
        final var advWithUser = helper.getAdvertisementById(activeAdvertisement.getId(), tokens2.accessToken())
                .expectStatus().isOk()
                .expectBody(AdvertisementDto.class)
                .returnResult().getResponseBody();

        final var user1 = helper.getUser(tokens1.accessToken());

        assertNotNull(advWithUser);
        assertNotNull(advWithUser.getId());
        assertNotNull(advWithUser.getUser());
        assertEquals(user1.id(), advWithUser.getUser().id());

        // get adv without auth token (user field must be null in response)
        final var advWithoutUser = helper.getAdvertisementById(activeAdvertisement.getId(), null)
                .expectStatus().isOk()
                .expectBody(AdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advWithoutUser);
        assertNotNull(advWithoutUser.getId());
        assertNull(advWithoutUser.getUser());
    }

    @Test
    void updateAdvertisementTest() throws JsonProcessingException {
        /// setup
        // create two users
        final var loginResponse1 = helper.registerUserAndGetTokens(user1Email, user1Phone, password);
        final var loginResponse2 = helper.registerUserAndGetTokens(user2Email, user2Phone, password);

        // create adv1 with user1
        final var advertisement = helper.createAdvertisement(
                new AdvertisementDto(null, "Adv title", "updateAdvertisementTest", null, "123.45", true),
                loginResponse1.accessToken());

        /// test
        // update adv1 with user1 (should be success and fields must change accordingly)
        webTestClient.put()
                .uri("/adv/" + advertisement.getId())
                .header("Authorization", "Bearer " + loginResponse1.accessToken())
                .bodyValue(new AdvertisementDto(null, "updated title", "updated updateAdvertisementTest", null,
                        "234.56", false))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(advertisement.getId())
                .jsonPath("$.title").isEqualTo("updated title")
                .jsonPath("$.description").isEqualTo("updated updateAdvertisementTest")
                .jsonPath("$.price").isEqualTo("234.56")
                .jsonPath("$.status").isEqualTo(false);

        // update no existing adv with user1 (should be not found)
        webTestClient.put()
                .uri("/adv/" + 1_000_000_000)
                .header("Authorization", "Bearer " + loginResponse1.accessToken())
                .bodyValue(new AdvertisementDto(null, "updated title 2", "updated updateAdvertisementTest 2", null,
                        "234.56", false))
                .exchange()
                .expectStatus().isNotFound();

        // update adv1 with user2 (should be not found)
        webTestClient.put()
                .uri("/adv/" + advertisement.getId())
                .header("Authorization", "Bearer " + loginResponse2.accessToken())
                .bodyValue(new AdvertisementDto(null, "updated title", "updated updateAdvertisementTest", null,
                        "234.56", false))
                .exchange()
                .expectStatus().isNotFound();

        // update adv with no user (should be unauthorized)
        webTestClient.put()
                .uri("/adv/" + advertisement.getId())
                .bodyValue(new AdvertisementDto(null, "updated title", "updated updateAdvertisementTest", null,
                        "234.56", false))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void deleteAdvertisementWithoutPicturesTest() throws JsonProcessingException {
        /// setup
        // create two users
        final var loginResponse1 = helper.registerUserAndGetTokens(user1Email, user1Phone, password);
        final var loginResponse2 = helper.registerUserAndGetTokens(user2Email, user2Phone, password);

        /// test
        // create advertisement adv 1 without picture with user1
        final var activeAdvertisement = helper.createAdvertisement(
                new AdvertisementDto(null, "Adv title", "Adv deleteAdvertisementWithoutPicturesTest", null,
                        "123.45", true), loginResponse1.accessToken());

        // delete adv1 with user2 (should be not found)
        helper.deleteAdvertisement(activeAdvertisement, loginResponse2.accessToken())
                .expectStatus().isNotFound();

        // delete adv1 with no user (should be unauthorized)
        helper.deleteAdvertisement(activeAdvertisement, null)
                .expectStatus().isUnauthorized();

        // delete adv1 with user1 (should be ok)
        helper.deleteAdvertisement(activeAdvertisement, loginResponse1.accessToken())
                .expectStatus().isOk();

        // get advertisement adv1 by id with user1 (should be not found)
        helper.getAdvertisementById(activeAdvertisement.getId(), loginResponse1.accessToken())
                .expectStatus().isNotFound();
    }

    @Test
    void deleteAdvertisementWithPicturesTest() throws JsonProcessingException {
        /// setup
        // create two users
        final var loginResponse1 = helper.registerUserAndGetTokens(user1Email, user1Phone, password);
        final var loginResponse2 = helper.registerUserAndGetTokens(user2Email, user2Phone, password);

        /// test
        // create advertisement adv2 with 2 pictures with user2
        final var advertisement = helper.createAdvertisement(new AdvertisementDto(null, "Adv title with pics",
                        "deleteAdvertisementWithPicturesTest", null, "123.45", true), loginResponse2.accessToken());

        final var advertisementResult = helper.createAdvertisement(advertisement,
                List.of("pics/pic1.png", "pics/pic2.jpg"), loginResponse2.accessToken());

        // delete adv2 with user1 (should be not found)
        helper.deleteAdvertisement(advertisementResult, loginResponse1.accessToken())
                .expectStatus().isNotFound();

        // delete adv2 with no user (should be unauthorized)
        helper.deleteAdvertisement(advertisementResult, null)
                .expectStatus().isUnauthorized();

        // delete adv2 with user2 (should be ok, make sure there are no advPics linked to this adv)
        helper.deleteAdvertisement(advertisementResult, loginResponse2.accessToken())
                .expectStatus().isOk();

        // get advertisement adv2 by id with user2 (should be not found)
        helper.getAdvertisementById(advertisementResult.getId(), loginResponse2.accessToken())
                .expectStatus().isNotFound();
    }

    @Test
    void deleteAdvertisementAddedToFavList() throws JsonProcessingException {
        // register user1 and user2
        final var loginResponse1 = helper.registerUserAndGetTokens(user1Email, user1Phone, password);
        final var loginResponse2 = helper.registerUserAndGetTokens(user2Email, user2Phone, password);

        // create adv1 as user1 and adv2 as user2
        final var adv1 = helper.createAdvertisement(
                new AdvertisementDto(null, "Adv1 title", "deleteAdvertisementAddedToFavList 1", null, "123.45", true),
                loginResponse1.accessToken());

        final var adv2 = helper.createAdvertisement(
                new AdvertisementDto(null, "Adv2 title", "deleteAdvertisementAddedToFavList 2", null, "123.45", true),
                loginResponse2.accessToken());

        // add adv1 and adv2 to user1's fav list
        helper.addToFavList(adv1, loginResponse1.accessToken())
                .expectStatus().isOk();
        helper.addToFavList(adv2, loginResponse1.accessToken())
                .expectStatus().isOk();

        // get list of user1's favs and make sure that adv1 and adv2 are there
        var favList1 = helper.getFavList(loginResponse1.accessToken());
        assertEquals(2, favList1.size());
        assertEquals(1, favList1.stream()
                .filter(_adv -> _adv.getId().equals(adv1.getId()))
                .count());
        assertEquals(1, favList1.stream()
                .filter(_adv -> _adv.getId().equals(adv2.getId()))
                .count());

        // delete adv2 as user2 (must be ok)
        helper.deleteAdvertisement(adv2, loginResponse2.accessToken())
                .expectStatus().isOk();

        // get list of user1's favs and make sure that only adv1 remains in the list
        favList1 = helper.getFavList(loginResponse1.accessToken());
        assertEquals(1, favList1.size());
        assertEquals(1, favList1.stream()
                .filter(_adv -> _adv.getId().equals(adv1.getId()))
                .count());
    }
}
