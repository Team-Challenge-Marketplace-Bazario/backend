package io.teamchallenge.project.bazario.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Verification {
    @Column(name = "email_verification")
    private String token;

    @Column(nullable = false)
    private boolean verified;

    private LocalDateTime expires;
}
