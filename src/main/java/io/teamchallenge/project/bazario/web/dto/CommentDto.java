package io.teamchallenge.project.bazario.web.dto;

import io.teamchallenge.project.bazario.entity.Comment;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
