package io.teamchallenge.project.bazario;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;

import static io.teamchallenge.project.bazario.Helper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdvertisementsTests {

    @Autowired
    private WebTestClient webTestClient;

    private String user1Email;
    private String user2Email;
    private String password;

    @BeforeEach()
    void setup() {
        final var currentTime = System.currentTimeMillis();
        user1Email = String.format("user1_%d@server.com", currentTime);
        user2Email = String.format("user2_%d@server.com", currentTime);
        password = "111111";
    }

    @Test
    void getAdvByIdTest() throws JsonProcessingException {
        // 0.1 register and save a user1's token

        var loginResponse1 = registerUserAndGetTokens(webTestClient, user1Email, password);
        var loginResponse2 = registerUserAndGetTokens(webTestClient, user2Email, password);

        // 1.0 create active adv using user1's credentials
        final var activeAdvertisement = createAdvertisement(webTestClient,
                new AdvertisementDto(null, "Adv title", "getAdvByIdTest", "123.45", true, Collections.emptyList(), null),
                loginResponse1.accessToken());

        // 1.1 create inactive adv using user1's credentials
        final var inActiveAdvertisement = createAdvertisement(webTestClient,
                new AdvertisementDto(null, "Adv title", "getAdvByIdTest", "123.45", false, Collections.emptyList(), null),
                loginResponse1.accessToken());

        // 2.0 get active adv using user1's credential
        getAdvertisementById(webTestClient, activeAdvertisement.getId(), loginResponse1.accessToken())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(activeAdvertisement.getId());

        // 2.1 get active adv using user2's credential
        getAdvertisementById(webTestClient, activeAdvertisement.getId(), loginResponse2.accessToken())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(activeAdvertisement.getId());

        // 2.2 get active adv using no credential
        getAdvertisementById(webTestClient, activeAdvertisement.getId(), null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(activeAdvertisement.getId());

        // 3.0 get inactive adv using user1's credential
        getAdvertisementById(webTestClient, inActiveAdvertisement.getId(), loginResponse1.accessToken())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(inActiveAdvertisement.getId());

        // 3.1 get inactive adv using user2's credential
        getAdvertisementById(webTestClient, inActiveAdvertisement.getId(), loginResponse2.accessToken())
                .expectStatus().isNotFound();

        // 3.2 get inactive adv using no credential
        getAdvertisementById(webTestClient, inActiveAdvertisement.getId(), null)
                .expectStatus().isNotFound();
    }

    @Test
    void updateAdvertisementTest() throws JsonProcessingException {
        /// setup
        // create two users
        final var loginResponse1 = registerUserAndGetTokens(webTestClient, user1Email, password);
        final var loginResponse2 = registerUserAndGetTokens(webTestClient, user2Email, password);

        // create adv1 with user1
        final var advertisement = createAdvertisement(webTestClient,
                new AdvertisementDto(null, "Adv title", "updateAdvertisementTest",
                        "123.45", true, Collections.emptyList(), null), loginResponse1.accessToken());

        /// test
        // update adv1 with user1 (should be success and fields must change accordingly)
        webTestClient.put()
                .uri("/adv/" + advertisement.getId())
                .header("Authorization", "Bearer " + loginResponse1.accessToken())
                .bodyValue(new AdvertisementDto(null, "updated title", "updated updateAdvertisementTest", "234.56",
                        false, Collections.emptyList(), null))
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
                .bodyValue(new AdvertisementDto(null, "updated title 2", "updated updateAdvertisementTest 2", "234.56",
                        false, Collections.emptyList(), null))
                .exchange()
                .expectStatus().isNotFound();

        // update adv1 with user2 (should be not found)
        webTestClient.put()
                .uri("/adv/" + advertisement.getId())
                .header("Authorization", "Bearer " + loginResponse2.accessToken())
                .bodyValue(new AdvertisementDto(null, "updated title", "updated updateAdvertisementTest", "234.56",
                        false, Collections.emptyList(), null))
                .exchange()
                .expectStatus().isNotFound();

        // update adv with no user (should be unauthorized)
        webTestClient.put()
                .uri("/adv/" + advertisement.getId())
                .bodyValue(new AdvertisementDto(null, "updated title", "updated updateAdvertisementTest", "234.56",
                        false, Collections.emptyList(), null))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void deleteAdvertisementWithoutPicturesTest() throws JsonProcessingException {
        /// setup
        // create two users
        final var loginResponse1 = registerUserAndGetTokens(webTestClient, user1Email, password);
        final var loginResponse2 = registerUserAndGetTokens(webTestClient, user2Email, password);

        /// test
        // create advertisement adv 1 without picture with user1
        final var activeAdvertisement = createAdvertisement(webTestClient,
                new AdvertisementDto(null, "Adv title", "Adv deleteAdvertisementWithoutPicturesTest",
                        "123.45", true, Collections.emptyList(), null), loginResponse1.accessToken());

        // delete adv1 with user2 (should be not found)
        deleteAdvertisement(webTestClient, activeAdvertisement, loginResponse2.accessToken())
                .expectStatus().isNotFound();

        // delete adv1 with no user (should be unauthorized)
        deleteAdvertisement(webTestClient, activeAdvertisement, null)
                .expectStatus().isUnauthorized();

        // delete adv1 with user1 (should be ok)
        deleteAdvertisement(webTestClient, activeAdvertisement, loginResponse1.accessToken())
                .expectStatus().isOk();

        // get advertisement adv1 by id with user1 (should be not found)
        getAdvertisementById(webTestClient, activeAdvertisement.getId(), loginResponse1.accessToken())
                .expectStatus().isNotFound();
    }

    @Test
    void deleteAdvertisementWithPicturesTest() throws JsonProcessingException {
        /// setup
        // create two users
        final var loginResponse1 = registerUserAndGetTokens(webTestClient, user1Email, password);
        final var loginResponse2 = registerUserAndGetTokens(webTestClient, user2Email, password);

        /// test
        // create advertisement adv2 with 2 pictures with user2
        final var advertisement = createAdvertisement(webTestClient,
                new AdvertisementDto(null, "Adv title with pics",
                        "deleteAdvertisementWithPicturesTest", "123.45", true, Collections.emptyList(), null),
                loginResponse2.accessToken());

        final var advertisementResult = createAdvertisement(webTestClient, advertisement,
                List.of("pics/pic1.png", "pics/pic2.jpg"), loginResponse2.accessToken());

        // delete adv2 with user1 (should be not found)
        deleteAdvertisement(webTestClient, advertisementResult, loginResponse1.accessToken())
                .expectStatus().isNotFound();

        // delete adv2 with no user (should be unauthorized)
        deleteAdvertisement(webTestClient, advertisementResult, null)
                .expectStatus().isUnauthorized();

        // delete adv2 with user2 (should be ok, make sure there are no advPics linked to this adv)
        deleteAdvertisement(webTestClient, advertisementResult, loginResponse2.accessToken())
                .expectStatus().isOk();

        // get advertisement adv2 by id with user2 (should be not found)
        getAdvertisementById(webTestClient, advertisementResult.getId(), loginResponse2.accessToken())
                .expectStatus().isNotFound();
    }

    @Test
    void deleteAdvertisementAddedToFavList() throws JsonProcessingException {
        // register user1 and user2
        final var loginResponse1 = registerUserAndGetTokens(webTestClient, user1Email, password);
        final var loginResponse2 = registerUserAndGetTokens(webTestClient, user2Email, password);

        // create adv1 as user1 and adv2 as user2
        final var adv1 = createAdvertisement(webTestClient,
                new AdvertisementDto(null, "Adv1 title", "deleteAdvertisementAddedToFavList 1", "123.45", true,
                        Collections.emptyList(), null),
                loginResponse1.accessToken());

        final var adv2 = createAdvertisement(webTestClient,
                new AdvertisementDto(null, "Adv2 title", "deleteAdvertisementAddedToFavList 2", "123.45", true,
                        Collections.emptyList(), null),
                loginResponse2.accessToken());

        // add adv1 and adv2 to user1's fav list
        addToFavList(webTestClient, adv1, loginResponse1.accessToken())
                .expectStatus().isOk();
        addToFavList(webTestClient, adv2, loginResponse1.accessToken())
                .expectStatus().isOk();

        // get list of user1's favs and make sure that adv1 and adv2 are there
        var favList1 = getFavList(webTestClient, loginResponse1.accessToken());
        assertEquals(2, favList1.size());
        assertEquals(1, favList1.stream()
                .filter(_adv -> _adv.getId().equals(adv1.getId()))
                .count());
        assertEquals(1, favList1.stream()
                .filter(_adv -> _adv.getId().equals(adv2.getId()))
                .count());

        // delete adv2 as user2 (must be ok)
        deleteAdvertisement(webTestClient, adv2, loginResponse2.accessToken())
                .expectStatus().isOk();

        // get list of user1's favs and make sure that only adv1 remains in the list
        favList1 = getFavList(webTestClient, loginResponse1.accessToken());
        assertEquals(1, favList1.size());
        assertEquals(1, favList1.stream()
                .filter(_adv -> _adv.getId().equals(adv1.getId()))
                .count());
    }
}
