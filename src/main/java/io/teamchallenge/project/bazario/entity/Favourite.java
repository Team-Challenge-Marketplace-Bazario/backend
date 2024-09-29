package io.teamchallenge.project.bazario.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "FAV",
        uniqueConstraints = {@UniqueConstraint(name="UNIQUE_ADV_ID__USER_ID", columnNames = {"ADV_ID", "USER_ID"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favourite {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FAV_SEQ")
    @SequenceGenerator(name = "FAV_SEQ", sequenceName = "FAV_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADV_ID")
    private Advertisement advertisement;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    private LocalDateTime createDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Favourite favourite = (Favourite) o;
        return Objects.equals(id, favourite.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
