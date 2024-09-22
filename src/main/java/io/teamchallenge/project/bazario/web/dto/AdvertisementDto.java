package io.teamchallenge.project.bazario.web.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.teamchallenge.project.bazario.entity.Advertisement;
import io.teamchallenge.project.bazario.exceptions.AppException;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneOffset;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisementDto {

    private static final String TITLE_FIELD = "title";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String PRICE_FIELD = "price";
    private static final String STATUS_FIELD = "status";

    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String price;

    private Boolean status;

    private List<AdvPictureDto> pics;

    private Long createDate;

    public AdvertisementDto(Advertisement vo) {
        this.id = vo.getId();
        this.title = vo.getTitle();
        this.description = vo.getDescription();
        this.price = vo.getPrice().toString();
        this.status = vo.isStatus();
        this.createDate = vo.getCreateDate().toEpochSecond(ZoneOffset.UTC);

        if (vo.getPictures() != null) {
            this.pics = vo.getPictures().stream()
                    .map(AdvPictureDto::new)
                    .toList();
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
}
