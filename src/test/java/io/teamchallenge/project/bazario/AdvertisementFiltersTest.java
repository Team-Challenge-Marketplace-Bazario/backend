package io.teamchallenge.project.bazario;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.teamchallenge.project.bazario.entity.Category;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import io.teamchallenge.project.bazario.web.dto.AdvertisementFilter;
import io.teamchallenge.project.bazario.web.dto.PagedAdvertisementDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Set;

import static io.teamchallenge.project.bazario.Helper.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = {"file:.env_test_local"})
public class AdvertisementFiltersTest {

    @Autowired
    private WebTestClient webTestClient;

    private String user1Email;
    private String password;

    @BeforeEach()
    void setup() {
        final var currentTime = System.currentTimeMillis();
        user1Email = String.format("user1_%d@server.com", currentTime);
        password = "111111";
    }

    @Test
    void categoryFilterTest() throws JsonProcessingException {
        // create user
        final var tokens = registerUserAndGetTokens(webTestClient, user1Email, password);

        // create adv without category
        final var advWithoutCategory = createAdvertisement(webTestClient,
                new AdvertisementDto(null, "Adv Title", "categoryFilterTest", null, "123.45", true, null, null),
                tokens.accessToken());

        // create adv with electronics category
        final var advElectronics = createAdvertisement(webTestClient,
                new AdvertisementDto(null, "Adv Title", "categoryFilterTest", Category.ELECTRONICS.name(), "123.45",
                        true, null, null), tokens.accessToken());

        // create adv with garden category
        final var advGarden = createAdvertisement(webTestClient,
                new AdvertisementDto(null, "Adv Title", "categoryFilterTest", Category.GARDEN.name(), "123.45",
                        true, null, null), tokens.accessToken());

        // get all adv without filter (must be ok)
        var advs = getAdvertisementByFilter(webTestClient, new AdvertisementFilter(null, null, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        // make sure there are 3 adv
        assertNotNull(advs);
        assertEquals(3, advs.content().size());
        final var ids = Set.of(advWithoutCategory.getId(), advElectronics.getId(), advGarden.getId());
        assertTrue(advs.content().stream()
                .allMatch(_adv -> ids.contains(_adv.getId())));

        // get all adv with filter category=electronics
        advs = getAdvertisementByFilter(webTestClient, new AdvertisementFilter(null, Category.ELECTRONICS, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        // make sure there is one adv with and its category is electronics
        assertNotNull(advs);
        assertEquals(1, advs.content().size());
        assertEquals(advElectronics.getId(), advs.content().get(0).getId());

        // get all adv with filter category=garden
        advs = getAdvertisementByFilter(webTestClient, new AdvertisementFilter(null, Category.GARDEN, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        // make sure there is one adv and its category is garden
        assertNotNull(advs);
        assertEquals(1, advs.content().size());
        assertEquals(advGarden.getId(), advs.content().get(0).getId());


        // get all adv with filter category=household
        advs = getAdvertisementByFilter(webTestClient, new AdvertisementFilter(null, Category.HOUSEHOLD, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        // make sure there is no adv
        assertNotNull(advs);
        assertEquals(0, advs.content().size());

        // get all adv with filter to un exist category
        advs = webTestClient.get()
                .uri(builder -> builder.path("/adv")
                        .queryParam("category", "xxx")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        // make sure there are 3 adv with and without category
        assertNotNull(advs);
        assertEquals(3, advs.content().size());
        assertTrue(advs.content().stream().allMatch(adv -> ids.contains(adv.getId())));
    }

    // test title filter
    // test title and category filters combined

    // test sorting by price asc and desc
    // test sorting by date asc and desc
    // test sorting by price and date combined

    // test that only active advs in response

    // test pagination
}
