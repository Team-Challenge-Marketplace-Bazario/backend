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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Set;

import static io.teamchallenge.project.bazario.Helper.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = {"file:.env_test_local"})
@ActiveProfiles("test")
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

        // make sure there are all ids here
        assertNotNull(advs);
        final var ids = Set.of(advWithoutCategory.getId(), advElectronics.getId(), advGarden.getId());
        assertEquals(ids.size(), advs.content().stream()
                .filter(_adv -> ids.contains(_adv.getId()))
                .count());

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

        // make sure there are all adv that in ids
        assertNotNull(advs);
        assertEquals(ids.size(), advs.content().stream()
                .filter(_adv -> ids.contains(_adv.getId()))
                .count());
    }

    // test title filter
    @Test
    void titleFilterTest() throws JsonProcessingException {
        // create user
        final var tokens = registerUserAndGetTokens(webTestClient, user1Email, password);

        final var titles = List.of(
                "Title keyword1 separated",
                "Title prefixkeyword1suffix keyword inside other word",
                "keyword1suffix title with keyword at the beginning",
                "Title with keyword at the end keyword1",
                "Title with keyword at the end keyWorD1",
                "Title with keyword at the end keyword2",
                "Plain Title"
        );

        for (String title : titles) {
            createAdvertisement(webTestClient, getActiveDtoWithTitle(title), tokens.accessToken());
        }

        // get all adv with filter title=keyword1 (must be advs with keyword1 in title only)
        var advs = getAdvertisementByFilter(webTestClient, new AdvertisementFilter("keyword1", null, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().stream()
                .allMatch(adv -> adv.getTitle().toLowerCase().contains("keyword1")));

        // get all adv with filter title=Keyword1 (must be advs with keyword1 without respect to case in title only)
        advs = getAdvertisementByFilter(webTestClient, new AdvertisementFilter("KeyWord1", null, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().stream()
                .allMatch(adv -> adv.getTitle().toLowerCase().contains("keyword1")));


        // get all adv with filter title=Keyword2
        advs = getAdvertisementByFilter(webTestClient, new AdvertisementFilter("KeyWord2", null, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().stream()
                .allMatch(adv -> adv.getTitle().toLowerCase().contains("keyword2")));

        // get all adv with filter title=nonExistingKeyword (must be 0 advs)
        advs = getAdvertisementByFilter(webTestClient, new AdvertisementFilter("nonExistingKeyword", null, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().isEmpty());

        /// national alphabet
        final var nationalTitles = List.of("Заголовок ключовеСлово окремо",
                "Заголовок ПрефіксключовеСловопостфікс ключове слово всередині",
                "Ключовеслово заголовок спочатку",
                "Заголовк з ключовим словом вкінці ключовеслово",
                "Інший заголовок з Ключовим словом",
                "Просто заголовок"
        );

        for (String title : nationalTitles) {
            createAdvertisement(webTestClient, getActiveDtoWithTitle(title), tokens.accessToken());
        }

        advs = getAdvertisementByFilter(webTestClient, new AdvertisementFilter("ключовеСлово", null, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().stream()
                .allMatch(adv -> adv.getTitle().toLowerCase().contains("ключовеслово")));

        // get all adv with filter title=nonExistingKeyword (must be 0 advs)
        advs = getAdvertisementByFilter(webTestClient, new AdvertisementFilter("nonExistingKeyword", null, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().isEmpty());
    }

    // test title and category filters combined

    // test sorting by price asc and desc
    // test sorting by date asc and desc
    // test sorting by price and date combined

    // test that only active advs in response

    // test pagination
}
