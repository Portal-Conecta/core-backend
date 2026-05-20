package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.query.GetAllUserQuery;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public Page<UserEntity> execute(GetAllUserQuery query) {
        GetAllUserQuery validQuery = requireQuery(query);
        RequestContext context = contextProvider.getRequestContext();

        permissionValidator.validateCanListUsers(context.userType());

        PageRequest pageRequest = validQuery.toPageRequest();

        if (validQuery.typeUser() == null) {
            return userRepository.findByDeletedAtIsNull(pageRequest);
        }

        return userRepository.findByDeletedAtIsNullAndType(validQuery.typeUser(), pageRequest);
    }

    private GetAllUserQuery requireQuery(GetAllUserQuery query) {
        if (query == null) {
            throw new InvalidUserDataException("List users query is required.");
        }

        return query;
    }
}
