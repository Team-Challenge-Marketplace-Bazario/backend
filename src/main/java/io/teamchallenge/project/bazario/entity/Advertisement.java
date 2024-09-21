package io.teamchallenge.project.bazario.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ADV")
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ADV_SEQ")
    @SequenceGenerator(name = "ADV_SEQ", sequenceName = "ADV_SEQ", allocationSize = 1)
    private Long id;

    private String title;

    private String description;

    @OneToMany(mappedBy = "advertisement", fetch = FetchType.EAGER)
    private List<AdvPicture> pictures;

    private BigDecimal price;

    private boolean status;

    private LocalDateTime createDate;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

}
