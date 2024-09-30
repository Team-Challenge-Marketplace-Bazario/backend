package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.AdvPicture;
import io.teamchallenge.project.bazario.entity.Advertisement;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.exceptions.AdvertisementNotFoundException;
import io.teamchallenge.project.bazario.helpers.CloudinaryHelper;
import io.teamchallenge.project.bazario.repository.AdvPictureRepository;
import io.teamchallenge.project.bazario.repository.AdvertisementRepository;
import io.teamchallenge.project.bazario.repository.CommentRepository;
import io.teamchallenge.project.bazario.repository.FavouriteRepository;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import io.teamchallenge.project.bazario.web.dto.PagedAdvertisementDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    private final CloudinaryHelper cloudinaryHelper;
    private final AdvertisementRepository advertisementRepository;
    private final AdvPictureRepository advPictureRepository;
    private final FavouriteRepository favouriteRepository;
    private final CommentRepository commentRepository;

    public AdvertisementServiceImpl(CloudinaryHelper cloudinaryHelper,
                                    AdvertisementRepository advertisementRepository,
                                    AdvPictureRepository advPictureRepository,
                                    FavouriteRepository favouriteRepository,
                                    CommentRepository commentRepository) {
        this.cloudinaryHelper = cloudinaryHelper;
        this.advertisementRepository = advertisementRepository;
        this.advPictureRepository = advPictureRepository;
        this.favouriteRepository = favouriteRepository;
        this.commentRepository = commentRepository;
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
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                user
        ));

        // 3. save adv_pic objects
        for (AdvPicture advPicture : advPicsList) {
            advPicture.setAdvertisement(advertisement);
            final var savedPicture = advPictureRepository.save(advPicture);
            log.debug("picture saved in DB as: {}", savedPicture);
        }

        // 4. return adv object
        advertisement.setPictures(advPicsList);

        return advertisement;
    }

    @Override
    @Transactional
    public Advertisement addPictures(Long advertisementId, List<MultipartFile> pics, User user) {
        final var advertisement = advertisementRepository.findByIdAndUser(advertisementId, user)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));

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
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));

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
    public PagedAdvertisementDto getAllByFilter(String title, boolean status, PageRequest pageRequest) {
        final var pagedList = advertisementRepository.findAllByTitleContainingAndStatus(title, status, pageRequest);

        final var dtoList = pagedList.getContent().stream()
                .map(AdvertisementDto::new)
                .toList();

        return new PagedAdvertisementDto(
                dtoList,
                pagedList.getNumber(),
                pagedList.getTotalPages(),
                pagedList.getSize());
    }

    @Override
    public PageRequest getPageRequest(Integer page, Integer itemsPerPage, List<String> sortFields) {
        if (page == null || page < 0) {
            page = 0;
        }

        if (itemsPerPage == null || itemsPerPage < 1) {
            itemsPerPage = 20;
        }

        final var sort = getSort(sortFields);

        return PageRequest.of(page, itemsPerPage, sort);
    }

    @Override
    public Advertisement getById(Long advId, User user) {
        final var advertisement = advertisementRepository.findById(advId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advId));

        // according to business logic only owner can see his own inactive advertisements
        if (!advertisement.isStatus()
            && (user == null || !Objects.equals(user.getId(), advertisement.getUser().getId()))) {
            throw new AdvertisementNotFoundException(advId);
        }

        return advertisement;
    }

    @Override
    @Transactional
    public Advertisement update(AdvertisementDto dto, User user) {
        final var advertisement = advertisementRepository.findByIdAndUser(dto.getId(), user)
                .orElseThrow(() -> new AdvertisementNotFoundException(dto.getId()));

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            advertisement.setTitle(dto.getTitle().trim());
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            advertisement.setDescription(dto.getDescription().trim());
        }

        if (dto.getStatus() != null) {
            advertisement.setStatus(dto.getStatus());
        }

        if (dto.getPrice() != null && !dto.getPrice().isBlank()
            && dto.getPrice().trim().matches("^\\d{1,8}(\\.\\d{1,2})?$")) {
            advertisement.setPrice(new BigDecimal(dto.getPrice().trim()));
        }

        return advertisementRepository.save(advertisement);
    }

    @Override
    @Transactional
    public boolean delete(Long advertisementId, User user) {
        final var advertisement = advertisementRepository.findByIdAndUser(advertisementId, user)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));

        // 1. delete all associated pictures
        final var advPictures = advertisement.getPictures();
        for (AdvPicture advPicture : advPictures) {
            if (advPicture.getExternalToken() != null) {
                cloudinaryHelper.deleteFile(advPicture.getExternalToken());
                advPictureRepository.delete(advPicture);
            }
        }

        // 2. remove advertisement from fav lists
        favouriteRepository.deleteAllByAdvertisement(advertisement);

        // 3. remove advertisement's comments
        commentRepository.deleteAllByAdvertisement(advertisement);

        // 4. delete adv itself
        log.debug("removing advertisement: {}", advertisement);
        return advertisementRepository.deleteAdvertisementById(advertisement.getId()) == 1;
    }

    private Sort getSort(List<String> sortFields) {

        if (sortFields == null || sortFields.size() < 2 || sortFields.size() % 2 != 0) {
            return Sort.by(Sort.Direction.ASC, "id");
        }

        var sort = Sort.unsorted();

        for (int i = 0; i < sortFields.size(); i += 2) {
            final var field = getSortProperty(sortFields.get(i));
            final var direction = getSortDirection(sortFields.get(i + 1));

            if (field != null) {
                sort = sort.and(Sort.by(direction, field));
            }
        }

        return sort;
    }

    private String getSortProperty(String property) {
        if ("price".equals(property)) {
            return "price";
        } else if ("date".equals(property)) {
            return "createDate";
        }

        return null;
    }

    private Sort.Direction getSortDirection(String direction) {
        return "desc".equals(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }
}
