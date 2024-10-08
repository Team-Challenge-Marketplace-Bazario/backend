package io.teamchallenge.project.bazario.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank @EmailOrPhone String username, @NotBlank String password) {
}
