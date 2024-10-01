package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.Advertisement;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import io.teamchallenge.project.bazario.web.dto.AdvertisementFilter;
import io.teamchallenge.project.bazario.web.dto.PagedAdvertisementDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdvertisementService {
    Advertisement add(AdvertisementDto dto, List<MultipartFile> pics, User user);

    Advertisement addPictures(Long advertisementId, List<MultipartFile> pics, User user);

    Advertisement deletePicture(Long advertisementId, Long pictureId, User user);

    PagedAdvertisementDto getAllByFilter(AdvertisementFilter filter, PageRequest pageRequest);

    PageRequest getPageRequest(Integer page, Integer itemsPerPage, List<String> sort);

    Advertisement getById(Long advId, User user);

    Advertisement update(AdvertisementDto dto, User user);

    boolean delete(Long advertisementId, User user);

    AdvertisementFilter getFilter(String title, String category, Boolean status);
}
