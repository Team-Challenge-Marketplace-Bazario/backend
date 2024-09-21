package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.AdvPicture;
import io.teamchallenge.project.bazario.entity.Advertisement;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.helpers.CloudinaryHelper;
import io.teamchallenge.project.bazario.repository.AdvPictureRepository;
import io.teamchallenge.project.bazario.repository.AdvertisementRepository;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    private final CloudinaryHelper cloudinaryHelper;
    private final AdvertisementRepository advertisementRepository;
    private final AdvPictureRepository advPictureRepository;

    public AdvertisementServiceImpl(CloudinaryHelper cloudinaryHelper,
                                    AdvertisementRepository advertisementRepository,
                                    AdvPictureRepository advPictureRepository) {
        this.cloudinaryHelper = cloudinaryHelper;
        this.advertisementRepository = advertisementRepository;
        this.advPictureRepository = advPictureRepository;
    }

    @Override
    @Transactional
    public Advertisement add(@Valid AdvertisementDto dto, List<MultipartFile> pics, User user) {
        // 1. upload files to external service
        final var advPicsList = new ArrayList<AdvPicture>();

        if (pics != null) {
            for (MultipartFile picFile : pics) {
                if (picFile.isEmpty()) {
                    continue;
                }
                final var uploadResponse = cloudinaryHelper.uploadFile(picFile);
                advPicsList.add(new AdvPicture(null, uploadResponse.url(), uploadResponse.publicId(), null));
            }
        }

        // 2. save Adv object
        final var advertisement = advertisementRepository.save(new Advertisement(
                null,
                dto.getTitle(),
                dto.getDescription(),
                Collections.emptyList(),
                new BigDecimal(dto.getPrice()),
                dto.getStatus(),
                LocalDateTime.now(),
                user
        ));

        // 3. save adv_pic objects
        for (AdvPicture advPicture : advPicsList) {
            advPicture.setAdvertisement(advertisement);
            advPictureRepository.save(advPicture);
        }

        // 4. return adv object
        advertisement.setPictures(advPicsList);

        return advertisement;
    }

    @Override
    @Transactional
    public Advertisement addPictures(Long advertisementId, List<MultipartFile> pics, User user) {
        final var advertisement = advertisementRepository.findByIdAndUser(advertisementId, user)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Advertisement with id %d not found", advertisementId)));

        final var pictures = new ArrayList<AdvPicture>();
        for (MultipartFile pic : pics) {
            if (pic.isEmpty()) {
                continue;
            }
            final var uploadResult = cloudinaryHelper.uploadFile(pic);
            pictures.add(advPictureRepository.save(
                    new AdvPicture(null, uploadResult.url(), uploadResult.publicId(), advertisement)));
        }

        if (advertisement.getPictures() == null) {
            advertisement.setPictures(pictures);
        } else {
            advertisement.setPictures(
                    Stream.concat(advertisement.getPictures().stream(), pictures.stream())
                            .toList());
        }

        return advertisement;
    }

    @Override
    @Transactional
    public Advertisement deletePicture(Long advertisementId, Long pictureId, User user) {
        final var advertisement = advertisementRepository.findByIdAndUser(advertisementId, user)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Advertisement with id %d not found", advertisementId)));

        if (advertisement.getPictures() == null) {
            throw new EntityNotFoundException(String.format("Picture with id %d not found", pictureId));
        }

        final var advPicture = advertisement.getPictures().stream()
                .filter(pic -> pic.getId().equals(pictureId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Picture with id %d not found", pictureId)));

        if (advPicture.getExternalToken() != null) {
            cloudinaryHelper.deleteFile(advPicture.getExternalToken());
        }

        advPictureRepository.delete(advPicture);
        advertisement.getPictures().remove(advPicture);

        return advertisement;
    }

    @Override
    public Optional<Advertisement> getById(Long advertisementId) {
        return advertisementRepository.findById(advertisementId);
    }

    @Override
    public List<Advertisement> getAllActive() {
        return advertisementRepository.getAllByStatus(true);
    }
}
