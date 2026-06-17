package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;

public record ReactivateClassResponse(ClassResponse classResponse) {
    public static ReactivateClassResponse from(ClassEntity entity) {
        return new ReactivateClassResponse(ClassResponse.from(entity));
    }
}
