package io.teamchallenge.project.bazario.exceptions;

public class AdvertisementNotFoundException extends AppException {
    public AdvertisementNotFoundException(long advertisementId) {
        super(String.format("Advertisement with id: %d not found", advertisementId));
    }
}
