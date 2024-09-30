package io.teamchallenge.project.bazario.service;

import io.teamchallenge.project.bazario.entity.Comment;
import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.exceptions.AdvertisementNotFoundException;
import io.teamchallenge.project.bazario.exceptions.IllegalOperationException;
import io.teamchallenge.project.bazario.repository.AdvertisementRepository;
import io.teamchallenge.project.bazario.repository.CommentRepository;
import io.teamchallenge.project.bazario.web.controller.CreateCommentRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {
    private final AdvertisementRepository advertisementRepository;
    private final CommentRepository commentRepository;

    public CommentServiceImpl(AdvertisementRepository advertisementRepository, CommentRepository commentRepository) {
        this.advertisementRepository = advertisementRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public Comment add(Long advertisementId, CreateCommentRequest dto, User user) {
        final var advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));

        if (Objects.equals(user.getId(), advertisement.getId())) {
            throw new IllegalOperationException("You cannot add comments to your own advertisement.");
        }

        if (!advertisement.isStatus()) {
            throw new AdvertisementNotFoundException(advertisementId);
        }

        final var savedComment = commentRepository.save(
                new Comment(null, dto.description(), LocalDateTime.now(), user));

        log.debug("created comment {}", savedComment);

        return savedComment;
    }
}
