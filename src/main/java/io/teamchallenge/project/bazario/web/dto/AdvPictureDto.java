package io.teamchallenge.project.bazario.web.dto;

import io.teamchallenge.project.bazario.entity.AdvPicture;

public class AdvPictureDto {
    private Long id;
    private String url;

    public AdvPictureDto() {
        this(null, null);
    }

    public AdvPictureDto(Long id, String url) {
        this.id = id;
        this.url = url;
    }

    public AdvPictureDto(AdvPicture vo) {
        this(vo.getId(), vo.getUrl());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
