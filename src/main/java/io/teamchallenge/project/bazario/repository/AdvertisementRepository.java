package io.teamchallenge.project.bazario.repository;


import io.teamchallenge.project.bazario.entity.Advertisement;
import io.teamchallenge.project.bazario.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long>,
        JpaSpecificationExecutor<Advertisement> {

    Optional<Advertisement> findByIdAndUser(Long advertisementId, User user);

    long deleteAdvertisementById(Long id);
}
