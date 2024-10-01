package io.teamchallenge.project.bazario.entity;

import org.springframework.data.jpa.domain.Specification;

public interface AdvertisementSpecifications {
    static Specification<Advertisement> hasStatus(boolean status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    static Specification<Advertisement> hasCategory(Category category) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category"), category.name());
    }

    static Specification<Advertisement> containsTitle(String title) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }
}
