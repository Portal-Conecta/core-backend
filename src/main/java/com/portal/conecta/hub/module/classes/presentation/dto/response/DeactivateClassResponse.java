package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;

public record DeactivateClassResponse(ClassResponse classResponse) {
    public static DeactivateClassResponse from(ClassEntity entity) {
        return new DeactivateClassResponse(ClassResponse.from(entity));
    }
}
