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
        return findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    @Override
    public Optional<User> save(User user) {
        return Optional.of(userRepository.save(user));
    }

    @Transactional
    @Override
    public User updateUser(User user, UpdateUserRequest updateRequest) {

        Predicate<String> isValid = _s -> _s != null && !_s.isBlank();

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
