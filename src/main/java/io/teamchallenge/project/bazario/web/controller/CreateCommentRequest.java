package io.teamchallenge.project.bazario.web.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(@NotBlank @Size(min = 3, max = 255) String description) {
}
