package io.teamchallenge.project.bazario.web.controller;

import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.service.AdvertisementService;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/adv")
public class AdvertisementController {

    private final AdvertisementService advService;

    public AdvertisementController(AdvertisementService advService) {
        this.advService = advService;
    }

    @PostMapping
    public ResponseEntity<AdvertisementDto> addAdvertisement(
            @RequestParam(value = "pics", required = false) List<MultipartFile> pics,
            @RequestParam("adv") String jsonString,
            @AuthenticationPrincipal User user) {

        final var advertisement = advService.add(new AdvertisementDto(jsonString), pics, user);
        return ResponseEntity.ok(new AdvertisementDto(advertisement));
    }

    @PostMapping("/{advId}/pics")
    public ResponseEntity<AdvertisementDto> addPicturesToAdvertisement(@PathVariable("advId") Long advertisementId,
                                                                       @RequestParam("pics") List<MultipartFile> pics,
                                                                       @AuthenticationPrincipal User user) {

        final var advertisement = advService.addPictures(advertisementId, pics, user);

        return ResponseEntity.ok(new AdvertisementDto(advertisement));
    }

    @DeleteMapping("/{advId}/pics/{pictureId}")
    public ResponseEntity<AdvertisementDto> deleteAdvertisementPicture(@PathVariable("advId") Long advertisementId,
                                                                       @PathVariable("pictureId") Long pictureId,
                                                                       @AuthenticationPrincipal User user) {
        final var advertisement = advService.deletePicture(advertisementId, pictureId, user);

        return ResponseEntity.ok(new AdvertisementDto(advertisement));
    }

    @GetMapping
    public ResponseEntity<List<AdvertisementDto>> getAllActiveAdvertisements() {
        final var voList = advService.getAllActive();
        //todo: need to implement pagination, filtering an sorting

        final var dtoList = voList.stream()
                .map(AdvertisementDto::new)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.debug(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
