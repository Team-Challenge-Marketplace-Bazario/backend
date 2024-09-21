package io.teamchallenge.project.bazario.web.dto;

import io.teamchallenge.project.bazario.entity.AdvPicture;

public record AdvPictureDto(Long id, String url) {
    public AdvPictureDto(AdvPicture vo) {
        this(vo.getId(), vo.getUrl());
    }
}
