package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.repository.UserRepository;
import io.teamchallenge.project.bazario.web.dto.UpdateUserRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Predicate;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username != null && username.contains("@")) {
            return findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        } else {
            return findByPhone(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with phone: " + username));
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Transactional
    @Override
    public User updateUser(User user, UpdateUserRequest updateRequest) {

        Predicate<String> isValid = str -> str != null && !str.isBlank();

        if (isValid.test(updateRequest.firstName())) {
            user.setFirstName(updateRequest.firstName());
        }

        if (isValid.test(updateRequest.lastName())) {
            user.setLastName(updateRequest.lastName());
        }

        if (isValid.test(updateRequest.email())) {
            user.setEmail(updateRequest.email());
        }

        if (isValid.test(updateRequest.phone())) {
            user.setPhone(updateRequest.phone());
        }

        return userRepository.save(user);
    }
}
