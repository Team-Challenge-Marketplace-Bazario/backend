package io.teamchallenge.project.bazario.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank String firstName,
                              @NotBlank String lastName,
                              @NotBlank String phone,
                              @NotBlank @Email String email,
                              @NotBlank String password) {
}
