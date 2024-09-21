package io.teamchallenge.project.bazario.exceptions;

public class AdvertisementPictureNotFoundException extends AppException {
    public AdvertisementPictureNotFoundException(Long id) {
        super(String.format("Advertisement picture with id %s not found", id));
    }
}
