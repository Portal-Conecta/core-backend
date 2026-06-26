package com.portal.conecta.hub.module.me.presentation.dto;

import java.util.List;
import java.util.UUID;

public record MyCourseResponse(
        UUID id,
        String name,
        String code,
        List<MyClassResponse> classes
) {
}
