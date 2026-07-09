package com.portal.conecta.hub.module.notification.presentation.dto;

public record UnreadCountResponse(long unreadCount) {
    public static UnreadCountResponse from(long unreadCount) {
        return new UnreadCountResponse(unreadCount);
    }
}