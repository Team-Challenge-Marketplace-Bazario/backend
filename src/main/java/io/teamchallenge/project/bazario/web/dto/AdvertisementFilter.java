package io.teamchallenge.project.bazario.web.dto;

import io.teamchallenge.project.bazario.entity.Category;

public record AdvertisementFilter(String title, Category category, Boolean status) {
}
