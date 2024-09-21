package io.teamchallenge.project.bazario.exceptions;

public class AdvertisementNotFoundException extends AppException {
    public AdvertisementNotFoundException(Long id) {
        super(String.format("Advertisement with id %s not found", id));
    }
}
