package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.Comment;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.web.controller.CreateCommentRequest;

import java.util.List;

public interface CommentService {
    Comment add(Long advertisementId, CreateCommentRequest dto, User user);

    List<Comment> getByAdvertisementId(Long advertisementId);
}
