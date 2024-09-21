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

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisementDto {

    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String price;

    private List<AdvPictureDto> pics;

    public AdvertisementDto(Advertisement vo) {
        this.id = vo.getId();
        this.title = vo.getTitle();
        this.description = vo.getDescription();
        this.price = vo.getPrice().toString();

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
            this.title = node.get("title").asText().trim();
            this.description = node.get("description").asText().trim();
            this.price = node.get("price").asText().trim();
        } catch (JsonProcessingException e) {
            throw new AppException("error processing json string", e);
        }
    }
}
