package com.portal.conecta.hub.module.notification.application.usecase;

import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetUserNotificationsUseCase {

    private final UserNotificationRepository repository;
    private final RequestContextProvider contextProvider;

    public GetUserNotificationsUseCase(
            UserNotificationRepository repository,
            RequestContextProvider contextProvider
    ) {
        this.repository = repository;
        this.contextProvider = contextProvider;
    }

    @Transactional(readOnly = true)
    public Page<UserNotificationEntity> execute(boolean unreadOnly, int page, int size) {
        UUID userId = contextProvider.getRequestContext().userId();
        return repository.findVisibleByUserId(userId, unreadOnly, PageRequest.of(page, size));
    }
}