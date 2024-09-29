package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.Advertisement;
import io.teamchallenge.project.bazario.entity.User;

import java.util.List;

public interface FavouriteService {

    List<Advertisement> getAll(User user);

    void add(Long advId, User user);

    void delete(Long advId, User user);
}
