package io.teamchallenge.project.bazario.web.controller;

import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.service.UserService;
import io.teamchallenge.project.bazario.web.dto.UpdateUserRequest;
import io.teamchallenge.project.bazario.web.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDto> getUserInfo(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserDto(user));
    }

    @PutMapping
    public ResponseEntity<UserDto> updateUserInfo(@AuthenticationPrincipal User user,
                                                  @Valid @RequestBody UpdateUserRequest updateRequest) {

        final var updatedUser = userService.updateUser(user, updateRequest);

        return ResponseEntity.ok(new UserDto(updatedUser));

    }
}
