package io.teamchallenge.project.bazario.web.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.teamchallenge.project.bazario.entity.Advertisement;
import io.teamchallenge.project.bazario.exceptions.AppException;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

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

    public AdvertisementDto() {
    }

    public AdvertisementDto(Long id, String title, String description, String price) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public List<AdvPictureDto> getPics() {
        return pics;
    }

    public void setPics(List<AdvPictureDto> pics) {
        this.pics = pics;
    }
}
