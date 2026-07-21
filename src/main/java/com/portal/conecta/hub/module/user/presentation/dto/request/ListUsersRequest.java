package com.portal.conecta.hub.module.user.presentation.dto.request;

import com.portal.conecta.hub.module.user.application.query.GetAllUserQuery;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

public record ListUsersRequest(
        @Min(value = 0, message = "A página deve ser maior ou igual a 0.")
        Integer page,

        @Min(value = 1, message = "O tamanho deve ser maior ou igual a 1.")
        @Max(value = 100, message = "O tamanho deve ser menor ou igual a 100.")
        Integer size,

        TypeUser typeUser,

        @Schema(description = "Filtra usuarios ativos cujo nome contenha o valor informado, sem diferenciar maiusculas de minusculas.", example = "Ana")
        String name,

        @Schema(
                description = "Filtra por um ou mais status da conta. Quando ausente, retorna somente usuarios ACTIVE.",
                example = "ACTIVE",
                allowableValues = {"PENDING_ACTIVATION", "ACTIVE", "DISABLED", "PENDING_DELETION"}
        )
        List<String> status,

        @Schema(
                description = "Quando true, para alunos e representantes retorna apenas usuarios sem vinculo de aluno ou representante em turma ativa. Nao altera a listagem de outros tipos de usuario.",
                example = "true"
        )
        Boolean semTurmaAtiva
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    public GetAllUserQuery toQuery() {
        return new GetAllUserQuery(resolvePage(), resolveSize(), typeUser, name, resolveStatuses(), Boolean.TRUE.equals(semTurmaAtiva));
    }

    private int resolvePage() {
        if (page == null) {
            return DEFAULT_PAGE;
        }

        return page;
    }

    private int resolveSize() {
        if (size == null) {
            return DEFAULT_SIZE;
        }

        return size;
    }

    private List<AccountStatus> resolveStatuses() {
        if (status == null || status.isEmpty()) {
            return List.of(AccountStatus.ACTIVE);
        }

        return status.stream()
                .map(this::toAccountStatus)
                .toList();
    }

    private AccountStatus toAccountStatus(String value) {
        try {
            return AccountStatus.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidUserDataException("Status de usuário inválido: " + value + ".");
        }
    }
}
