package io.teamchallenge.project.bazario.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "ROLES", uniqueConstraints = {
        @UniqueConstraint(name="UNIQUE_ROLE__USER_ID", columnNames = {"ROLE", "USER_ID"})})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ROLE_SEQ")
    @SequenceGenerator(name = "ROLE_SEQ", sequenceName = "ROLE_SEQ", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false, length = 50)
    private UserRole role;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;


    @Override
    public String getAuthority() {
        return role.name();
    }

    @Override
    public String toString() {
        return "Role{" +
               "id=" + id +
               ", role=" + role +
               '}';
    }
}
