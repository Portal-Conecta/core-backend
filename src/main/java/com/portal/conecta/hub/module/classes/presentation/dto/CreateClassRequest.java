package com.portal.conecta.hub.module.classes.presentation.dto;

import com.portal.conecta.hub.module.classes.domain.model.Shift;

import java.util.UUID;

public record CreateClassRequest(
        Shift shift,
        UUID courseId
) {
}
