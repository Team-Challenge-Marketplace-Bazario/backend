package io.teamchallenge.project.bazario.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotBlank @Size(min = 2, max = 50) String firstName,
                              @NotBlank @Size(min = 2, max = 50) String lastName,
                              @NotBlank @Email String email,
                              @NotBlank @Phone String phone,
                              @NotBlank @Size(max = 50) String password) {
}
