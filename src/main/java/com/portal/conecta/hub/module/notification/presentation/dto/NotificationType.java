package com.portal.conecta.hub.module.notification.presentation.dto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum NotificationType {
    CHECKLIST,
    MAPA,
    COMUNICADO,
    OUTRO;

    public static NotificationType fromSource(String source){
        if (source == null){
            return OUTRO;
        }

        return switch (source){
            case "checklist-service" -> CHECKLIST;
            case "seatmap-service" -> MAPA;
            case "comunicados-service" -> COMUNICADO;
            default -> {
                log.warn("Source de notificação sem mapeamento de tipo conhecido: {}", source);
                yield  OUTRO;
            }
        };
    }
}
