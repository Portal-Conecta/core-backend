package com.portal.conecta.hub.module.course.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload para criação de um novo curso.")
public record CreateCourseRequest(

        @NotBlank(message = "name é obrigatório")
        @Schema(description = "Nome descritivo do curso.", example = "Desenvolvimento de Sistemas")
        String name,

        @NotBlank(message = "code é obrigatório")
        @Schema(description = "Código identificador do curso.", example = "DEV-01")
        String code
) {
}