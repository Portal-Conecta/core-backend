package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import org.springframework.data.domain.Page;

import java.util.List;

public record ListClassesResponse(
        List<ListClassItemResponse> items,
        int page,
        int size,
        long totalElements,
        long totalPages
) {

    public static ListClassesResponse from(Page<ClassEntity> page){
        return new ListClassesResponse(
                page.getContent()
                        .stream()
                        .map(ListClassItemResponse::from)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
