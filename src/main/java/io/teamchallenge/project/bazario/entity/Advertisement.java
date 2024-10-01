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

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private Category category;

    @OneToMany(mappedBy = "advertisement", fetch = FetchType.EAGER)
    private List<AdvPicture> pictures;

    private BigDecimal price;

    private boolean status;

    private LocalDateTime createDate;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @Override
    public String toString() {
        return "Advertisement{" +
               "id=" + id +
               ", title='" + title + '\'' +
               ", description='" + description + '\'' +
               ", category=" + category +
               ", pictures=" + pictures +
               ", price=" + price +
               ", status=" + status +
               ", createDate=" + createDate +
               ", user=" + user.getId() +
               '}';
    }
}
