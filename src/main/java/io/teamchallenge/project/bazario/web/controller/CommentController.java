package io.teamchallenge.project.bazario.web.controller;

import io.teamchallenge.project.bazario.entity.User;
import io.teamchallenge.project.bazario.exceptions.AdvertisementNotFoundException;
import io.teamchallenge.project.bazario.exceptions.IllegalOperationException;
import io.teamchallenge.project.bazario.service.CommentService;
import io.teamchallenge.project.bazario.web.dto.CommentDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{advId}")
    public ResponseEntity<CommentDto> createComment(@PathVariable(name = "advId") Long advId,
                                                    @Valid @RequestBody CreateCommentRequest dto,
                                                    @AuthenticationPrincipal User user) {

        final var comment = commentService.add(advId, dto, user);

        return ResponseEntity.ok(new CommentDto(comment));
    }


    @GetMapping("/{advId}")
    public ResponseEntity<List<CommentDto>> getAdvertisementComments(@PathVariable(name = "advId") Long advId) {
        final var list = commentService.getByAdvertisementId(advId);

        return ResponseEntity.ok(CommentDto.toList(list));
    }

    @ExceptionHandler(AdvertisementNotFoundException.class)
    public ResponseEntity<Void> handleEntityNotFoundException(Exception ex) {
        log.debug(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler({IllegalOperationException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Void> handleIllegalOperationException(Exception ex) {
        log.debug(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
