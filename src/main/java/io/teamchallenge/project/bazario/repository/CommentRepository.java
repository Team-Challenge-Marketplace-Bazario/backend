package io.teamchallenge.project.bazario.repository;

import io.teamchallenge.project.bazario.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
