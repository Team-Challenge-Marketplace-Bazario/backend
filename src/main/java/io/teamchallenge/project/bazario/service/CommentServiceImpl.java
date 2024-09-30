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
import java.util.List;
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

        if (Objects.equals(user.getId(), advertisement.getUser().getId())) {
            throw new IllegalOperationException("You cannot add comments to your own advertisement.");
        }

        if (!advertisement.isStatus()) {
            throw new AdvertisementNotFoundException(advertisementId);
        }

        final var savedComment = commentRepository.save(
                new Comment(null, dto.description(), LocalDateTime.now(), advertisement, user));

        log.debug("created comment {}", savedComment);

        return savedComment;
    }

    @Override
    public List<Comment> getByAdvertisementId(Long advertisementId) {
        final var advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException(advertisementId));

        return commentRepository.findAllByAdvertisement(advertisement);
    }
}
