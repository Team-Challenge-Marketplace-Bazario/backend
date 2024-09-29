package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.Advertisement;
import io.teamchallenge.project.bazario.entity.Favourite;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.exceptions.AdvertisementNotFoundException;
import io.teamchallenge.project.bazario.repository.AdvertisementRepository;
import io.teamchallenge.project.bazario.repository.FavouriteRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class FavouriteServiceImpl implements FavouriteService {

    private final FavouriteRepository favouriteRepository;
    private final AdvertisementRepository advertisementRepository;

    public FavouriteServiceImpl(FavouriteRepository favouriteRepository,
                                AdvertisementRepository advertisementRepository) {
        this.favouriteRepository = favouriteRepository;
        this.advertisementRepository = advertisementRepository;
    }

    /**
     * @param user app user
     * @return Gets all favourite advertisement of current user and other users' active advertisement
     */
    @Override
    public List<Advertisement> getAll(User user) {
        final var favourites = favouriteRepository.findAllByUser(user);

        return favourites.stream()
                .map(Favourite::getAdvertisement)
                .filter(advertisement -> isActiveOrOwner(advertisement, user))
                .toList();
    }

    @Override
    @Transactional
    public void add(Long advId, User user) {
        final var favourite = favouriteRepository.findByAdvertisementIdAndUser(advId, user);

        if (favourite.isPresent()) {
            return;
        }

        final var advertisement = advertisementRepository.findById(advId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advId));

        if (!isActiveOrOwner(advertisement, user)) {
            throw new AdvertisementNotFoundException(advId);
        }

        favouriteRepository.save(
                new Favourite(null, advertisement, user, LocalDateTime.now()));

    }

    @Override
    @Transactional
    public void delete(Long advId, User user) {
        final var favourite = favouriteRepository.findByAdvertisementIdAndUser(advId, user)
                .orElseThrow(() -> new AdvertisementNotFoundException(advId));

        if (!isActiveOrOwner(favourite.getAdvertisement(), user)) {
            throw new AdvertisementNotFoundException(advId);
        }

        favouriteRepository.delete(favourite);
    }

    private boolean isActiveOrOwner(Advertisement advertisement, User user) {
        return advertisement != null && user != null
               && (advertisement.isStatus() || Objects.equals(advertisement.getUser(), user));
    }
}
