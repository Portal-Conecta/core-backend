package com.portal.conecta.hub.module.course.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload para atualização de um curso. Envie apenas os campos que deseja alterar.")
public record UpdateCourseRequest(

        @Schema(description = "Novo nome descritivo do curso.", example = "Manutenção e Configuração de Redes Locais")
        String name,

        @Schema(description = "Novo código identificador do curso.", example = "RED-02")
        String code
) {
}