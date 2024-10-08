package io.teamchallenge.project.bazario.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.teamchallenge.project.bazario.entity.Advertisement;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.exceptions.AppException;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AdvertisementDto {

    private static final String TITLE_FIELD = "title";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String CATEGORY_FIELD = "category";
    private static final String PRICE_FIELD = "price";
    private static final String STATUS_FIELD = "status";

    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private String category;

    @NotBlank
    private String price;

    private Boolean status;

    private List<AdvPictureDto> pics;

    private String createDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserDto user;

    public AdvertisementDto(Advertisement vo) {
        this.id = vo.getId();
        this.title = vo.getTitle();
        this.description = vo.getDescription();
        this.category = vo.getCategory() == null ? null : vo.getCategory().name();
        this.price = vo.getPrice().toString();
        this.status = vo.isStatus();
        this.createDate = vo.getCreateDate().toString();

        if (vo.getPictures() != null) {
            this.pics = vo.getPictures().stream()
                    .map(AdvPictureDto::new)
                    .toList();
        }
    }

    public AdvertisementDto(Long id, String title, String description, String category, String price, Boolean status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
        this.status = status;
    }

    public AdvertisementDto(Advertisement vo, User user) {
        this(vo);

        if (user != null) {
            this.user = new UserDto(user);
        }
    }

    public AdvertisementDto(String jsonString) {
        try {
            final var objectMapper = new ObjectMapper();
            final var node = objectMapper.readTree(jsonString);

            if (node.has(TITLE_FIELD)) {
                this.title = node.get(TITLE_FIELD).asText().trim();
            }

            if (node.has(DESCRIPTION_FIELD)) {
                this.description = node.get(DESCRIPTION_FIELD).asText().trim();
            }

            if (node.has(CATEGORY_FIELD)) {
                this.category = node.get(CATEGORY_FIELD).asText().trim();
            }

            if (node.has(PRICE_FIELD)) {
                this.price = node.get(PRICE_FIELD).asText().trim();
            }

            if (node.has(STATUS_FIELD)) {
                final var statusValue = node.get(STATUS_FIELD).asText().trim();
                this.status = "true".equalsIgnoreCase(statusValue) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                this.status = Boolean.FALSE;
            }
        } catch (JsonProcessingException e) {
            throw new AppException("error processing json string", e);
        }
    }

    public static List<AdvertisementDto> toList(List<Advertisement> voList) {
        if (voList == null || voList.isEmpty()) {
            return Collections.emptyList();
        }

        return voList.stream()
                .map(AdvertisementDto::new)
                .toList();
    }
}
