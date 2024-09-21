package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.Advertisement;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface AdvertisementService {
    Advertisement add(AdvertisementDto dto, List<MultipartFile> pics, User user);
    Optional<Advertisement> getById(Long advertisementId);

    List<Advertisement> getAllActive();

    Advertisement addPictures(Long advertisementId, List<MultipartFile> pics, User user);

    Advertisement deletePicture(Long advertisementId, Long pictureId, User user);
}
