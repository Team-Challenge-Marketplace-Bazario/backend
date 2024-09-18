package io.teamchallenge.project.bazario.web.dto;

import io.teamchallenge.project.bazario.entity.User;

public record UserDto(Long id, String firstName, String lastName, String phone, String email) {

    public UserDto(User user) {
        this(user.getId(), user.getFirstName(), user.getLastName(), user.getPhone(), user.getEmail());
    }
}
