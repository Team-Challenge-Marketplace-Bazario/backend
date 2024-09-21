package io.teamchallenge.project.bazario.repository;

import io.teamchallenge.project.bazario.entity.AdvPicture;
import io.teamchallenge.project.bazario.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AdvPictureRepository extends JpaRepository<AdvPicture, Long> {
    @Query("SELECT p FROM AdvPicture p " +
           "    JOIN p.advertisement a " +
           "    JOIN a.user u " +
           "WHERE p.id = :pictureId " +
           "    AND a.id = :advertisementId " +
           "    AND u = :user")
    Optional<AdvPicture> findByIdAndAdvertisementIdAndUser(Long pictureId, Long advertisementId, User user);
}
