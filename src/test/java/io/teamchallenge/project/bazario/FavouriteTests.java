package io.teamchallenge.project.bazario;

import com.fasterxml.jackson.core.JsonProcessingException;
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
public class FavouriteTests {

    @Autowired
    private WebTestClient webTestClient;

    private String user1Email;
    private String user2Email;
    private String password;

    private AdvertisementDto adv1;
    private AdvertisementDto adv2;

    @BeforeEach
    public void setUp() {
        final var currentTime = System.currentTimeMillis();
        user1Email = String.format("user1_%d@server.com", currentTime);
        user2Email = String.format("user2_%d@server.com", currentTime);
        password = "111111";

        adv1 = new AdvertisementDto(
                null, "Title User1", "Description User1", null, "123.45", true, Collections.emptyList(), null);

        adv2 = new AdvertisementDto(
                null, "Title User2", "Description User2", null, "67.89", true, Collections.emptyList(), null);
    }

    @Test
    public void addFavourite() throws JsonProcessingException {
        // create user1 and user2
        final var loginResponse1 = TestHelper.registerUserAndGetTokens(webTestClient, user1Email, password);
        final var loginResponse2 = TestHelper.registerUserAndGetTokens(webTestClient, user2Email, password);

        // create adv1 for user1 and adv2 for user2
        final var advertisement1 = TestHelper.createAdvertisement(webTestClient, adv1, loginResponse1.accessToken());
        final var advertisement2 = TestHelper.createAdvertisement(webTestClient, adv2, loginResponse2.accessToken());

        // make user1 add adv1 to fav
        addToFavList(webTestClient, advertisement1, loginResponse1.accessToken())
                .expectStatus().isOk();

        // get list of user1's fav and make sure that adv1 is there
        var favList1 = getFavList(webTestClient, loginResponse1.accessToken());

        assertNotNull(favList1);
        assertTrue(
                favList1.stream().anyMatch(_adv -> _adv.getId().equals(advertisement1.getId()))
        );

        // make user1 add adv2 to fav
        addToFavList(webTestClient, advertisement2, loginResponse1.accessToken())
                .expectStatus().isOk();

        // get list of user1's fav and make sure that adv1 and adv2 are there.
        favList1 = getFavList(webTestClient, loginResponse1.accessToken());

        assertNotNull(favList1);
        assertEquals(2, favList1.stream()
                .filter(_adv -> _adv.getId().equals(advertisement1.getId())
                                || _adv.getId().equals(advertisement2.getId()))
                .count());

        // make adv1 inactive by user1
        var updatedAdv1 = TestHelper.updateAdvertisement(webTestClient, new AdvertisementDto(
                        advertisement1.getId(), null, null, null, null, false, Collections.emptyList(), null),
                loginResponse1.accessToken());

        assertFalse(updatedAdv1.getStatus());

        // get list of user1's fav and make sure that adv1 and adv2 are there
        favList1 = getFavList(webTestClient, loginResponse1.accessToken());

        assertNotNull(favList1);
        assertEquals(2, favList1.stream()
                .filter(_adv -> _adv.getId().equals(advertisement1.getId())
                                || _adv.getId().equals(advertisement2.getId()))
                .count());


        // make adv2 inactive by user2
        var updatedAdv2 = TestHelper.updateAdvertisement(webTestClient, new AdvertisementDto(
                        advertisement2.getId(), null, null, null, null, false, Collections.emptyList(), null),
                loginResponse2.accessToken());

        assertFalse(updatedAdv2.getStatus());

        // get list of user1's fav and make sure that adv2 isn't there
        favList1 = getFavList(webTestClient, loginResponse1.accessToken());

        assertNotNull(favList1);
        assertEquals(1, favList1.stream()
                .filter(_adv -> _adv.getId().equals(advertisement1.getId()))
                .count());

        assertEquals(0, favList1.stream()
                .filter(_adv -> _adv.getId().equals(advertisement2.getId()))
                .count());

        // make user1 add adv1 to fav again
        webTestClient.post()
                .uri("/fav/" + advertisement1.getId())
                .header("Authorization", "Bearer " + loginResponse1.accessToken())
                .exchange()
                .expectStatus().isOk();

        // get list of user1's fav and make sure that adv1 only once there
        favList1 = getFavList(webTestClient, loginResponse1.accessToken());

        assertNotNull(favList1);
        assertEquals(1, favList1.stream()
                .filter(_adv -> _adv.getId().equals(advertisement1.getId()))
                .count());
    }

    @Test
    public void deleteFavourite() throws JsonProcessingException {
        // create user1 and user2
        final var loginResponse1 = registerUserAndGetTokens(webTestClient, user1Email, password);
        final var loginResponse2 = registerUserAndGetTokens(webTestClient, user2Email, password);

        // create adv1 as user1 and adv2 as user2
        final var advertisement1 = createAdvertisement(webTestClient, adv1, loginResponse1.accessToken());
        final var advertisement2 = createAdvertisement(webTestClient, adv1, loginResponse2.accessToken());

        // add adv1 without auth (must be 401)
        addToFavList(webTestClient, advertisement1, null)
                .expectStatus().isUnauthorized();

        // add adv1 to user1's fav
        addToFavList(webTestClient, advertisement1, loginResponse1.accessToken())
                .expectStatus().isOk();

        // add adv2 to user1's fav
        addToFavList(webTestClient, advertisement2, loginResponse1.accessToken())
                .expectStatus().isOk();

        // get fav list as user1 and make sure that there are adv1 and adv2
        var favList1 = getFavList(webTestClient, loginResponse1.accessToken());
        assertEquals(2, favList1.size());

        assertEquals(1, favList1.stream()
                .filter(_adv -> _adv.getId().equals(advertisement1.getId()))
                .count());

        assertEquals(1, favList1.stream()
                .filter(_adv -> _adv.getId().equals(advertisement2.getId()))
                .count());

        // delete adv1 without auth (must be 401 - unauthorized)
        deleteFromFavList(webTestClient, advertisement1, null)
                .expectStatus().isUnauthorized();

        // delete adv1 from user1's fav (must be ok - 200)
        deleteFromFavList(webTestClient, advertisement1, loginResponse1.accessToken())
                .expectStatus().isOk();

        // delete adv1 from user1's fav (must be not found - 404)
        deleteFromFavList(webTestClient, advertisement1, loginResponse1.accessToken())
                .expectStatus().isNotFound();

        // get fav list as user1 and make sure that there is only adv2
        favList1 = getFavList(webTestClient, loginResponse1.accessToken());
        assertEquals(1, favList1.size());
        assertEquals(1, favList1.stream()
                .filter(_adv -> _adv.getId().equals(advertisement2.getId()))
                .count());

        // make adv2 inactive as user2
        updateAdvertisement(webTestClient,
                new AdvertisementDto(advertisement2.getId(), null, null, null, null, false, Collections.emptyList(), null),
                loginResponse2.accessToken());

        // get fav list as user1 and make sure that there are no any advs
        favList1 = getFavList(webTestClient, loginResponse1.accessToken());
        assertEquals(0, favList1.size());

        // delete adv2 from user1's fav (must be not found - 404)
        deleteFromFavList(webTestClient, advertisement2, loginResponse1.accessToken())
                .expectStatus().isNotFound();

        // make adv2 active as user2
        updateAdvertisement(webTestClient,
                new AdvertisementDto(advertisement2.getId(), null, null, null, null, true, Collections.emptyList(), null),
                loginResponse2.accessToken());

        // get fav list as user1 and make sure that adv2 is there
        favList1 = getFavList(webTestClient, loginResponse1.accessToken());
        assertEquals(1, favList1.size());
        assertEquals(1, favList1.stream()
                .filter(_adv -> _adv.getId().equals(advertisement2.getId()))
                .count());

        // delete adv2 from user1's fav (must be ok - 200)
        deleteFromFavList(webTestClient, advertisement2, loginResponse1.accessToken())
                .expectStatus().isOk();
    }
}
