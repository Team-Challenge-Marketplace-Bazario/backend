package io.teamchallenge.project.bazario.web.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyPasswordRequest(@NotBlank String token, @NotBlank String password) {
}
