package com.portal.conecta.hub.module.user.application.query;

import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Parâmetros de paginação e filtro para listagem de usuários.
 *
 * <p>Validações aplicadas no construtor compacto:
 * <ul>
 *   <li>{@code page} deve ser maior ou igual a 0;</li>
 *   <li>{@code size} deve estar entre 1 e 100 (inclusive).</li>
 * </ul>
 *
 * <p>Ordenação padrão: {@code createdAt} decrescente, desempate por {@code id} crescente.
 * Os filtros {@code typeUser}, {@code name} e {@code status} são opcionais. Quando o status
 * não é informado, a consulta retorna somente usuários ativos.
 *
 * @throws InvalidUserDataException se {@code page} ou {@code size} violarem os limites.
 */
public record GetAllUserQuery(
        int page,
        int size,
        TypeUser typeUser,
        String name,
        List<AccountStatus> accountStatuses,
        boolean semTurmaAtiva
) {

    private static final int MAX_SIZE = 100;
    private static final Sort DEFAULT_SORT = Sort.by("createdAt").descending().and(Sort.by("id").ascending());

    public GetAllUserQuery {
        if (page < 0) {
            throw new InvalidUserDataException("page deve ser maior ou igual a 0.");
        }

        if (size < 1) {
            throw new InvalidUserDataException("size deve ser maior ou igual a 1.");
        }

        if (size > MAX_SIZE) {
            throw new InvalidUserDataException("size deve ser menor ou igual a 100.");
        }

        if (name != null) {
            name = name.trim();
            if (name.isEmpty()) {
                name = null;
            }
        }

        if (accountStatuses == null || accountStatuses.isEmpty()) {
            accountStatuses = List.of(AccountStatus.ACTIVE);
        } else {
            accountStatuses = List.copyOf(new LinkedHashSet<>(accountStatuses));
        }
    }

    /**
     * Converte a query em um {@link PageRequest} com a ordenação padrão.
     *
     * @return instância pronta para uso nos métodos de repositório paginados.
     */
    public PageRequest toPageRequest() {
        return PageRequest.of(page, size, DEFAULT_SORT);
    }
}
