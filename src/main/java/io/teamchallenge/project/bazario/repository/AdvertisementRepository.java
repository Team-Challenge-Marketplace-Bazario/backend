package io.teamchallenge.project.bazario.repository;


import io.teamchallenge.project.bazario.entity.Advertisement;
import io.teamchallenge.project.bazario.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    List<Advertisement> getAllByStatus(boolean status);

    Optional<Advertisement> findByIdAndUser(Long advertisementId, User user);
}
