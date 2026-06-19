package com.portal.conecta.hub.module.notification.application.usecase;

import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetUnreadNotificationCountUseCase {

    private final UserNotificationRepository repository;
    private final RequestContextProvider contextProvider;

    public GetUnreadNotificationCountUseCase(
            UserNotificationRepository repository,
            RequestContextProvider contextProvider
    ) {
        this.repository = repository;
        this.contextProvider = contextProvider;
    }

    @Transactional(readOnly = true)
    public long execute() {
        UUID userId = contextProvider.getRequestContext().userId();
        return repository.countUnreadByUserId(userId);
    }
}