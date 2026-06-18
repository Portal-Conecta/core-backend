package com.portal.conecta.hub.module.notification.presentation.controller;

import com.portal.conecta.hub.module.notification.application.use_case.DismissNotificationUseCase;
import com.portal.conecta.hub.module.notification.application.use_case.MarkAllNotificationsAsReadUseCase;
import com.portal.conecta.hub.module.notification.application.use_case.MarkNotificationAsReadUseCase;
import com.portal.conecta.hub.shared.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@Tag(name = "Notificações", description = "Operações para gerenciamento, leitura e descarte de notificações do usuário.")
@RestController
@RequestMapping("/api/v1/notifications")
@Slf4j
public class NotificationController {

    private final MarkNotificationAsReadUseCase markAsReadUseCase;
    private final DismissNotificationUseCase dismissUseCase;
    private final MarkAllNotificationsAsReadUseCase markAllAsReadUseCase;

    public NotificationController(
            MarkNotificationAsReadUseCase markAsReadUseCase,
            DismissNotificationUseCase dismissUseCase,
            MarkAllNotificationsAsReadUseCase markAllAsReadUseCase
    ) {
        this.markAsReadUseCase = markAsReadUseCase;
        this.dismissUseCase = dismissUseCase;
        this.markAllAsReadUseCase = markAllAsReadUseCase;
    }

    @Operation(
            summary = "Marcar notificação como lida",
            description = "Altera o status de uma notificação específica do usuário autenticado para lida.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notificação marcada como lida com sucesso."),
            @ApiResponse(responseCode = "400", description = "Formato de identificador inválido.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada para o usuário informado.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "Identificador da notificação.", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID notificationId,
            Principal principal
    ) {
        log.debug("Marcando notificação {} como lida", notificationId);
        UUID userId = UUID.fromString(principal.getName());
        markAsReadUseCase.execute(userId, notificationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Descartar/ocultar notificação",
            description = "Arquiva ou oculta uma notificação do painel do usuário autenticado.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notificação descartada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Formato de identificador inválido.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada para o usuário informado.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{notificationId}/dismiss")
    public ResponseEntity<Void> dismiss(
            @Parameter(description = "Identificador da notificação.", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID notificationId,
            Principal principal
    ) {
        log.debug("Descartando notificação {}", notificationId);
        UUID userId = UUID.fromString(principal.getName());
        dismissUseCase.execute(userId, notificationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Marcar todas as notificações como lidas",
            description = "Altera o status de todas as notificações não lidas do usuário autenticado para lidas em lote.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Todas as notificações foram marcadas como lidas."),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            Principal principal
    ) {
        log.debug("Marcando todas as notificações como lidas");
        UUID userId = UUID.fromString(principal.getName());
        markAllAsReadUseCase.execute(userId);
        return ResponseEntity.noContent().build();
    }
}