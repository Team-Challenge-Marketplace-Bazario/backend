package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.Comment;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.web.controller.CreateCommentRequest;

public interface CommentService {
    Comment add(Long advertisementId, CreateCommentRequest dto, User user);
}
