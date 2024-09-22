package io.teamchallenge.project.bazario.web.dto;

import java.util.List;

public record PagedAdvertisementDto(List<AdvertisementDto> content,
                                    Integer page,
                                    Integer pages,
                                    Integer size) {
}
