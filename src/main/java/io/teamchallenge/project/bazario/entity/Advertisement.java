package io.teamchallenge.project.bazario.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    public Advertisement() {
    }

    public Advertisement(Long id, String title, String description, List<AdvPicture> pictures, BigDecimal price,
                         boolean status, LocalDateTime createDate, User user) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.pictures = pictures;
        this.price = price;
        this.status = status;
        this.createDate = createDate;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AdvPicture> getPictures() {
        return pictures;
    }

    public void setPictures(List<AdvPicture> pictures) {
        this.pictures = pictures;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
