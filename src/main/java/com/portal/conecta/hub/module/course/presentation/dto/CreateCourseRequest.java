package com.portal.conecta.hub.module.course.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload para criação de um novo curso.")
public record CreateCourseRequest(

        @Schema(description = "Nome descritivo do curso.", example = "Desenvolvimento de Sistemas")
        String name,

        @Schema(description = "Código identificador do curso.", example = "DEV-01")
        String code
) {
}