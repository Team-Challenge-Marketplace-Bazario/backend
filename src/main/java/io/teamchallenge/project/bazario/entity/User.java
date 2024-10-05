package io.teamchallenge.project.bazario.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "USERS")

public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USER_SEQ")
    @SequenceGenerator(name = "USER_SEQ", sequenceName = "USER_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone", nullable = false, unique = true, length = 13)
    private String phone;

    @Column(nullable = false)
    private boolean verified;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "token", column = @Column(name = "EMAIL_TOKEN")),
            @AttributeOverride(name = "expires", column = @Column(name = "EMAIL_TOKEN_EXPIRES",
                    columnDefinition = "timestamp"))
    })
    private Verification emailVerification;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "token", column = @Column(name = "PASSWORD_TOKEN")),
            @AttributeOverride(name = "expires", column = @Column(name = "PASSWORD_TOKEN_EXPIRES",
                    columnDefinition = "timestamp"))
    })
    private Verification passwordVerification;

    public User(Long id, String firstName, String lastName, String email, String password, String phone,
                boolean verified, Verification emailVerification, Verification passwordVerification) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.verified = verified;
        this.emailVerification = emailVerification;
        this.passwordVerification = passwordVerification;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isEnabled() {
        return verified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", email='" + email + '\'' +
               ", phone='" + phone + '\'' +
               ", verified=" + verified +
               '}';
    }
}
