package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.query.GetAllUserQuery;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Retorna lista paginada de usuários, com filtros opcionais por tipo, nome e status.
 *
 * @throws com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException se o requisitante não tiver permissão para listar usuários.
 * @throws InvalidUserDataException      se a query for nula.
 */
@Service
public class GetAllUserUseCase {

    private final UserRepository userRepository;
    private final UserPermissionValidator permissionValidator;
    private final RequestContextProvider contextProvider;

    public GetAllUserUseCase(
            UserRepository userRepository,
            UserPermissionValidator permissionValidator,
            RequestContextProvider contextProvider
    ) {
        this.userRepository = userRepository;
        this.permissionValidator = permissionValidator;
        this.contextProvider = contextProvider;
    }

    /**
     * Executa a consulta paginada.
     *
     * <p>Quando nenhum status é informado, a query usa {@link AccountStatus#ACTIVE} como padrão.
     *
     * @param query parâmetros de paginação e filtro; não pode ser nulo.
     * @return página de usuários conforme filtros aplicados.
     */
    @Transactional(readOnly = true)
    public Page<UserEntity> execute(GetAllUserQuery query) {
        query = requireQuery(query);
        PageRequest pageRequest = query.toPageRequest();

        if (query.typeUser() == null && query.name() == null) {
            return userRepository.findByAccountStatusIn(query.accountStatuses(), pageRequest);
        }

        if (query.typeUser() == null) {
            return userRepository.findByAccountStatusInAndNameContainingIgnoreCase(
                    query.accountStatuses(),
                    query.name(),
                    pageRequest
            );
        }

        if (query.name() == null) {
            return userRepository.findByAccountStatusInAndType(query.accountStatuses(), query.typeUser(), pageRequest);
        }

        return userRepository.findByAccountStatusInAndTypeAndNameContainingIgnoreCase(
                query.accountStatuses(),
                query.typeUser(),
                query.name(),
                pageRequest
        );
    }

    private GetAllUserQuery requireQuery(GetAllUserQuery query) {
        if (query == null) {
            throw new InvalidUserDataException("A consulta para listar usuários é obrigatória.");
        }

        return query;
    }
}
