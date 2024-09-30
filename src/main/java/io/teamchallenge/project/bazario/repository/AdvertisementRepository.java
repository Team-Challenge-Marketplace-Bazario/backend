package io.teamchallenge.project.bazario.repository;


import io.teamchallenge.project.bazario.entity.Advertisement;
import io.teamchallenge.project.bazario.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    Optional<Advertisement> findByIdAndUser(Long advertisementId, User user);

    Page<Advertisement> findAllByTitleContainingAndStatus(String title, boolean status, Pageable pageRequest);

    long deleteAdvertisementById(Long id);
}
