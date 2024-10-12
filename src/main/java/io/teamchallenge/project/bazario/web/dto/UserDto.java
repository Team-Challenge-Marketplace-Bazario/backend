package io.teamchallenge.project.bazario.web.dto;

import io.teamchallenge.project.bazario.entity.Role;
import io.teamchallenge.project.bazario.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

public record UserDto(Long id, String firstName, String lastName, String phone, String email, Set<String> roles) {

    public UserDto(User user) {
        this(user.getId(), user.getFirstName(), user.getLastName(), user.getPhone(), user.getEmail(),
                user.getRoles().stream()
                        .map(Role::getAuthority)
                        .collect(Collectors.toSet()));
    }
}
