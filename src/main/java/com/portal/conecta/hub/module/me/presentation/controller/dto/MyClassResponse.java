package com.portal.conecta.hub.module.me.presentation.controller.dto;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;

import java.util.UUID;

public record MyClassResponse(
        UUID id,
        String name,
        Integer number,
        Shift shift,
        ClassRole classRole
) {
}
