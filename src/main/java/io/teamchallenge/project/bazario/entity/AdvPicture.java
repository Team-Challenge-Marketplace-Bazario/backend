package io.teamchallenge.project.bazario.entity;

import jakarta.persistence.*;

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

    public AdvPicture() {
    }

    public AdvPicture(Long id, String url, String externalToken, Advertisement advertisement) {
        this.id = id;
        this.url = url;
        this.externalToken = externalToken;
        this.advertisement = advertisement;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExternalToken() {
        return externalToken;
    }

    public void setExternalToken(String externalToken) {
        this.externalToken = externalToken;
    }

    public Advertisement getAdvertisement() {
        return advertisement;
    }

    public void setAdvertisement(Advertisement advertisement) {
        this.advertisement = advertisement;
    }
}
