package io.teamchallenge.project.bazario.web.controller;

import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.exceptions.AdvertisementNotFoundException;
import io.teamchallenge.project.bazario.service.AdvertisementService;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import io.teamchallenge.project.bazario.web.dto.PagedAdvertisementDto;
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
    public ResponseEntity<PagedAdvertisementDto> getAllActiveAdvertisements(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "sort", required = false) List<String> sort,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "ipp", required = false) Integer itemsPerPage) {

        final var pageRequest = advService.getPageRequest(page, itemsPerPage, sort);

        if (title == null) {
            title = "";
        }

        final var pagedDto = advService.getAllByFilter(title, true, pageRequest);

        return ResponseEntity.ok(pagedDto);
    }

    @GetMapping("/{advId}")
    public ResponseEntity<AdvertisementDto> getAdvertisementById(@PathVariable("advId") Long advId,
                                                                 @AuthenticationPrincipal User user) {
        final var advertisement = advService.getById(advId, user);

        return ResponseEntity.ok(new AdvertisementDto(advertisement));
    }

    @PutMapping("/{advId}")
    public ResponseEntity<AdvertisementDto> updateAdvertisement(@PathVariable("advId") Long advId,
                                                                @RequestBody AdvertisementDto dto,
                                                                @AuthenticationPrincipal User user) {

        dto.setId(advId);

        final var updatedAdvertisement = advService.update(dto, user);

        return ResponseEntity.ok(new AdvertisementDto(updatedAdvertisement));
    }

    @DeleteMapping("/{advId}")
    public ResponseEntity<Void> deleteAdvertisement(@PathVariable("advId") Long advId,
                                                    @AuthenticationPrincipal User user) {

        advService.delete(advId, user);

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler({EntityNotFoundException.class, AdvertisementNotFoundException.class})
    public ResponseEntity<Void> handleEntityNotFoundException(Exception ex) {
        log.debug(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
