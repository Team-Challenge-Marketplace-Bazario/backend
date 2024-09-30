package io.teamchallenge.project.bazario.web.dto;

import io.teamchallenge.project.bazario.entity.Comment;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class CommentDto {
    private Long id;
    private String description;
    private String createDate;
    private UserDto user;

    public CommentDto(Comment vo) {
        this.id = vo.getId();
        this.description = vo.getDescription();
        this.createDate = vo.getCreateDate().toString();
        this.user = new UserDto(vo.getUser());
    }

    public static List<CommentDto> toList(List<Comment> voList) {
        if (voList == null || voList.isEmpty()) {
            return Collections.emptyList();
        }

        return voList.stream()
                .map(CommentDto::new)
                .toList();
    }
}
