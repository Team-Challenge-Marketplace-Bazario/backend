package io.teamchallenge.project.bazario.web.controller;

import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.service.AdvertisementService;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/adv")
public class AdvertisementController {

    private final AdvertisementService advService;

    public AdvertisementController(AdvertisementService advService) {
        this.advService = advService;
    }

    @PostMapping
    public ResponseEntity<AdvertisementDto> addAdvertisement(@RequestParam("pics") List<MultipartFile> pics,
                                                             @RequestParam("adv") String jsonString,
                                                             @AuthenticationPrincipal User user) {

        final var advertisement = advService.add(new AdvertisementDto(jsonString), pics, user);
        return ResponseEntity.ok(new AdvertisementDto(advertisement));
    }

    @PostMapping("/{advId}")
    public ResponseEntity<AdvertisementDto> addPicturesToAdvertisement(@PathVariable("advId") Long advertisementId,
                                                          @RequestParam("pics") List<MultipartFile> pics,
                                                          @AuthenticationPrincipal User user) {

        final var advertisement = advService.addPictures(advertisementId, pics, user);

        return ResponseEntity.ok(new AdvertisementDto(advertisement));
    }

    @DeleteMapping("/{advId}/{pictureId}")
    public ResponseEntity<AdvertisementDto> deleteAdvertisementPicture(@PathVariable("advId") Long advertisementId,
                                                 @PathVariable("pictureId") Long pictureId,
                                                 @AuthenticationPrincipal User user) {

        final var advertisement = advService.deletePicture(advertisementId, pictureId, user);

        return ResponseEntity.ok(new AdvertisementDto(advertisement));
    }

    @GetMapping
    public ResponseEntity<List<AdvertisementDto>> getAllActiveAdvertisements() {
        final var voList = advService.getAllActive();

        final var dtoList = voList.stream()
                .map(AdvertisementDto::new)
                .toList();

        return ResponseEntity.ok(dtoList);
    }
}
