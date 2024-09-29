package io.teamchallenge.project.bazario.web.controller;

import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.exceptions.AdvertisementNotFoundException;
import io.teamchallenge.project.bazario.service.FavouriteService;
import io.teamchallenge.project.bazario.web.dto.AdvertisementDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fav")
@Slf4j
public class FavouriteController {

    private final FavouriteService favouriteService;

    public FavouriteController(FavouriteService favouriteService) {
        this.favouriteService = favouriteService;
    }

    @GetMapping
    public ResponseEntity<List<AdvertisementDto>> getAllFavourites(@AuthenticationPrincipal User user) {
        final var advList = favouriteService.getAll(user);


        return ResponseEntity.ok(AdvertisementDto.toList(advList));
    }

    @PostMapping("/{advId}")
    public ResponseEntity<Void> addFavourite(@PathVariable("advId") Long advId, @AuthenticationPrincipal User user) {
        favouriteService.add(advId, user);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{advId}")
    public ResponseEntity<Void> deleteFavourite(@PathVariable("advId") Long advId, @AuthenticationPrincipal User user) {
        favouriteService.delete(advId, user);

        return ResponseEntity.ok().build();
    }


    @ExceptionHandler(AdvertisementNotFoundException.class)
    public ResponseEntity<Void> handleEntityNotFoundException(Exception ex) {
        log.debug(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
