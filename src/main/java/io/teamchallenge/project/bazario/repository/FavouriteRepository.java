package io.teamchallenge.project.bazario.repository;

import io.teamchallenge.project.bazario.entity.Favourite;
import io.teamchallenge.project.bazario.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    Optional<Favourite> findByAdvertisementIdAndUser(Long advertisementId, User user);

    List<Favourite> findAllByUser(User user);
}
