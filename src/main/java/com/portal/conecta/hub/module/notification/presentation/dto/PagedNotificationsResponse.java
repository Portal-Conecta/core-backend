package com.portal.conecta.hub.module.notification.presentation.dto;

import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import org.springframework.data.domain.Page;

import java.util.List;

public record PagedNotificationsResponse(
        List<NotificationResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static PagedNotificationsResponse from(Page<UserNotificationEntity> page) {
        return new PagedNotificationsResponse(
                page.getContent().stream()
                        .map(NotificationResponse::from)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}