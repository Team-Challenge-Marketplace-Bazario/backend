package io.teamchallenge.project.bazario.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ADV_PICS")
public class AdvPicture {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ADV_PIC_SEQ")
    @SequenceGenerator(name = "ADV_PIC_SEQ", sequenceName = "ADV_PIC_SEQ", allocationSize = 1)
    private Long id;

    private String url;

    private String externalToken;

    @ManyToOne
    @JoinColumn(name = "ADV_ID")
    private Advertisement advertisement;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdvPicture that = (AdvPicture) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
