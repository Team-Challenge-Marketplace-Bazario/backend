package io.teamchallenge.project.bazario.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(@Size(max = 100) String firstName,
                                @Size(max = 100) String lastName,
                                @Size(max = 100) String phone,
                                @Size(max = 100) @Email String email) {

}
