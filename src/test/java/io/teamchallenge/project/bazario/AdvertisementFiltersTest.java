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
import org.springframework.data.util.Pair;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static io.teamchallenge.project.bazario.TestHelper.getActiveAdvDtoWithTitleAndCategory;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = {"file:.env_test_local"})
@ActiveProfiles("test")
@Sql("classpath:clean-db.sql")
public class AdvertisementFiltersTest {

    @Autowired
    private WebTestClient webTestClient;
    private TestHelper helper;

    private String user1Email;
    private String user1Phone;
    private String password;

    @BeforeEach()
    void setup() {
        final var currentTime = System.currentTimeMillis();
        user1Email = String.format("user1_%d@server.com", currentTime);
        user1Phone = String.format("+38%010d", currentTime % 10000000000L);
        password = "111111";

        helper = new TestHelper();
        helper.setWebTestClient(webTestClient);
    }

    @Test
    void categoryFilterTest() throws JsonProcessingException {
        // create user
        final var tokens = helper.registerUserAndGetTokens(user1Email, user1Phone, password);

        // create adv without category
        final var advWithoutCategory = helper.createAdvertisement(
                new AdvertisementDto(null, "Adv Title", "categoryFilterTest", null, "123.45", true), tokens.accessToken());

        // create adv with electronics category
        final var advElectronics = helper.createAdvertisement(
                new AdvertisementDto(null, "Adv Title", "categoryFilterTest", Category.ELECTRONICS.name(), "123.45",
                        true), tokens.accessToken());

        // create adv with garden category
        final var advGarden = helper.createAdvertisement(new AdvertisementDto(null, "Adv Title", "categoryFilterTest", Category.GARDEN.name(), "123.45",
                true), tokens.accessToken());

        // get all adv without filter (must be ok)
        var advs = helper.getAdvertisementByFilter(new AdvertisementFilter(null, null, null))
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
        advs = helper.getAdvertisementByFilter(new AdvertisementFilter(null, Category.ELECTRONICS, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        // make sure there is one adv with and its category is electronics
        assertNotNull(advs);
        assertTrue(advs.content().stream()
                .allMatch(adv -> Category.ELECTRONICS.name().equals(adv.getCategory())));

        // get all adv with filter category=garden
        advs = helper.getAdvertisementByFilter(new AdvertisementFilter(null, Category.GARDEN, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        // make sure there is one adv and its category is garden
        assertNotNull(advs);
        assertEquals(1, advs.content().size());
        assertEquals(advGarden.getId(), advs.content().get(0).getId());


        // get all adv with filter category=household
        advs = helper.getAdvertisementByFilter(new AdvertisementFilter(null, Category.HOUSEHOLD, null))
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
                        .queryParam("ipp", 1000)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        // make sure there are all adv that in ids
        assertNotNull(advs);
        assertFalse(advs.content().isEmpty());
    }

    // test title filter
    @Test
    void titleFilterTest() throws JsonProcessingException {
        // create user
        final var tokens = helper.registerUserAndGetTokens(user1Email, user1Phone, password);

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
            helper.createAdvertisement(getActiveAdvDtoWithTitleAndCategory(title, null), tokens.accessToken());
        }

        // get all adv with filter title=keyword1 (must be advs with keyword1 in title only)
        var advs = helper.getAdvertisementByFilter(new AdvertisementFilter("keyword1", null, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().stream()
                .allMatch(adv -> adv.getTitle().toLowerCase().contains("keyword1")));

        // get all adv with filter title=Keyword1 (must be advs with keyword1 without respect to case in title only)
        advs = helper.getAdvertisementByFilter(new AdvertisementFilter("KeyWord1", null, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().stream()
                .allMatch(adv -> adv.getTitle().toLowerCase().contains("keyword1")));


        // get all adv with filter title=Keyword2
        advs = helper.getAdvertisementByFilter(new AdvertisementFilter("KeyWord2", null, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().stream()
                .allMatch(adv -> adv.getTitle().toLowerCase().contains("keyword2")));

        // get all adv with filter title=nonExistingKeyword (must be 0 advs)
        advs = helper.getAdvertisementByFilter(new AdvertisementFilter("nonExistingKeyword", null, null))
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
            helper.createAdvertisement(getActiveAdvDtoWithTitleAndCategory(title, null), tokens.accessToken());
        }

        advs = helper.getAdvertisementByFilter(new AdvertisementFilter("ключовеСлово", null, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().stream()
                .allMatch(adv -> adv.getTitle().toLowerCase().contains("ключовеслово")));

        // get all adv with filter title=nonExistingKeyword (must be 0 advs)
        advs = helper.getAdvertisementByFilter(new AdvertisementFilter("nonExistingKeyword", null, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().isEmpty());
    }

    // test title and category filters combined
    @Test
    void categoryTitleFilterTest() throws JsonProcessingException {
        // create user
        final var tokens = helper.registerUserAndGetTokens(user1Email, user1Phone, password);

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
            helper.createAdvertisement(getActiveAdvDtoWithTitleAndCategory(title, Category.CLOTHES.name()),
                    tokens.accessToken());

            helper.createAdvertisement(getActiveAdvDtoWithTitleAndCategory(title, Category.ELECTRONICS.name()),
                    tokens.accessToken());

            helper.createAdvertisement(getActiveAdvDtoWithTitleAndCategory(title, null),
                    tokens.accessToken());
        }

        // get all by title=keyword1 and category=clothes
        var advs = helper.getAdvertisementByFilter(new AdvertisementFilter("keyword1", Category.CLOTHES, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().stream()
                .allMatch(adv -> adv.getTitle().toLowerCase().contains("keyword1")
                                 && adv.getCategory().equals(Category.CLOTHES.name())));

        // get all by title=keyword1 and category=clothes
        advs = helper.getAdvertisementByFilter(new AdvertisementFilter("keyword2", Category.ELECTRONICS, null))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().stream()
                .allMatch(adv -> adv.getTitle().toLowerCase().contains("keyword2")
                                 && adv.getCategory().equals(Category.ELECTRONICS.name())));

    }

    // test sorting by price asc and desc
    @Test
    void sortingByPriceTest() throws JsonProcessingException {
        final var tokens = helper.registerUserAndGetTokens(user1Email, user1Phone, password);

        helper.createAdvertisement(new AdvertisementDto(null, "sortingByPriceTest",
                        "sortingByPriceTest description", Category.ELECTRONICS.name(), "10.00", true),
                tokens.accessToken());

        helper.createAdvertisement(new AdvertisementDto(null, "sortingByPriceTest",
                        "sortingByPriceTest description", Category.ELECTRONICS.name(), "20.00", true),
                tokens.accessToken());

        helper.createAdvertisement(new AdvertisementDto(null, "sortingByPriceTest",
                        "sortingByPriceTest description", Category.ELECTRONICS.name(), "30.00", true),
                tokens.accessToken());

        // sort by price ascending
        var advs = helper.getAdvertisementByFilter(new AdvertisementFilter(null, null, null),
                List.of(Pair.of("sort", "price"), Pair.of("sort", "asc")))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().size() > 1);

        for (int i = 1; i < advs.content().size(); i++) {
            final var previous = advs.content().get(i - 1);
            final var current = advs.content().get(i);

            assertTrue(new BigDecimal(previous.getPrice()).compareTo(new BigDecimal(current.getPrice())) <= 0);
        }

        // sort by price descending
        advs = helper.getAdvertisementByFilter(new AdvertisementFilter(null, null, null),
                List.of(Pair.of("sort", "price"), Pair.of("sort", "desc")))
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().size() > 1);

        for (int i = 1; i < advs.content().size(); i++) {
            final var previous = advs.content().get(i - 1);
            final var current = advs.content().get(i);

            assertTrue(new BigDecimal(previous.getPrice()).compareTo(new BigDecimal(current.getPrice())) >= 0);
        }
    }
    // test sorting by date asc and desc
    // test sorting by price and date combined

    // test that only active advs in response
    @Test
    void activeAdvTest() throws JsonProcessingException {
        final var tokens = helper.registerUserAndGetTokens(user1Email, user1Phone, password);

        helper.createAdvertisement(new AdvertisementDto(null, "activeAdvTest", "activeAdvTest",
                Category.CLOTHES.name(), "123.45", true), tokens.accessToken());

        helper.createAdvertisement(new AdvertisementDto(null, "activeAdvTest", "activeAdvTest",
                Category.CLOTHES.name(), "123.45", false), tokens.accessToken());

        helper.createAdvertisement(new AdvertisementDto(null, "activeAdvTest", "activeAdvTest",
                null, "123.45", true), tokens.accessToken());

        helper.createAdvertisement(new AdvertisementDto(null, "activeAdvTest", "activeAdvTest",
                null, "123.45", false), tokens.accessToken());

        final var advs = helper.getAdvertisementByFilter(null)
                .expectStatus().isOk()
                .expectBody(PagedAdvertisementDto.class)
                .returnResult().getResponseBody();

        assertNotNull(advs);
        assertTrue(advs.content().stream()
                .allMatch(AdvertisementDto::getStatus));

    }

    //todo: test pagination
}
