package com.portal.conecta.hub.module.notification.presentation.controller;

import com.portal.conecta.hub.module.notification.aplication.use_case.UpdateUserNotificationStatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@Tag(name = "Notificações", description = "Operações para gerenciamento de notificações do usuário autenticado.")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final UpdateUserNotificationStatusUseCase useCase;

    public NotificationController(UpdateUserNotificationStatusUseCase useCase) {
        this.useCase = useCase;
    }

    @Operation(summary = "Marcar notificação como lida", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notificação marcada como lida com sucesso."),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada para o usuário autenticado.")
    })
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID notificationId,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        useCase.markAsRead(userId, notificationId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Marcar todas as notificações como lidas", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Notificações atualizadas com sucesso.")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        useCase.markAllAsRead(userId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ocultar notificação (Dismiss)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Operação realizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Identificador da notificação em formato inválido."),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou token inválido."),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada para o usuário autenticado.")
    })
    @PatchMapping("/{notificationId}/dismiss")
    public ResponseEntity<Void> dismiss(
            @PathVariable UUID notificationId,
            Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        useCase.dismiss(userId, notificationId);

        return ResponseEntity.noContent().build();
    }
}