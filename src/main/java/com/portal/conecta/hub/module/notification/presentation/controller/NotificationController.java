package com.portal.conecta.hub.module.notification.presentation.controller;

import com.portal.conecta.hub.module.notification.application.use_case.DismissNotificationUseCase;
import com.portal.conecta.hub.module.notification.application.use_case.GetUnreadNotificationCountUseCase;
import com.portal.conecta.hub.module.notification.application.use_case.GetUserNotificationsUseCase;
import com.portal.conecta.hub.module.notification.application.use_case.MarkAllNotificationsAsReadUseCase;
import com.portal.conecta.hub.module.notification.application.use_case.MarkNotificationAsReadUseCase;
import com.portal.conecta.hub.module.notification.domain.model.NotificationStatus;
import com.portal.conecta.hub.module.notification.presentation.dto.MarkAsReadNotificationsRequest;
import com.portal.conecta.hub.module.notification.presentation.dto.PagedNotificationsResponse;
import com.portal.conecta.hub.module.notification.presentation.dto.UnreadCountResponse;
import com.portal.conecta.hub.shared.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notificações", description = "Operações para gerenciamento, leitura e descarte de notificações do usuário.")
@RestController
@RequestMapping("/notifications")
@Slf4j
@Validated
public class NotificationController {

    private final MarkNotificationAsReadUseCase markAsReadUseCase;
    private final DismissNotificationUseCase dismissUseCase;
    private final MarkAllNotificationsAsReadUseCase markAllAsReadUseCase;
    private final GetUserNotificationsUseCase getUserNotificationsUseCase;
    private final GetUnreadNotificationCountUseCase getUnreadCountUseCase;

    public NotificationController(
            MarkNotificationAsReadUseCase markAsReadUseCase,
            DismissNotificationUseCase dismissUseCase,
            MarkAllNotificationsAsReadUseCase markAllAsReadUseCase,
            GetUserNotificationsUseCase getUserNotificationsUseCase,
            GetUnreadNotificationCountUseCase getUnreadCountUseCase
    ) {
        this.markAsReadUseCase = markAsReadUseCase;
        this.dismissUseCase = dismissUseCase;
        this.markAllAsReadUseCase = markAllAsReadUseCase;
        this.getUserNotificationsUseCase = getUserNotificationsUseCase;
        this.getUnreadCountUseCase = getUnreadCountUseCase;
    }

    @Operation(
            summary = "Listar notificações do usuário autenticado",
            description = "Retorna as notificações do usuário autenticado filtradas por status (lida ou não lida), com paginação, ordenadas da mais recente para a mais antiga. Notificações ocultadas não são retornadas.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de notificações retornada com sucesso.",
                    content = @Content(schema = @Schema(implementation = PagedNotificationsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Status inválido ou parâmetro de paginação fora do limite.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<PagedNotificationsResponse> listNotifications(
            @Parameter(description = "Filtra por notificações lidas ou não lidas.", example = "UNREAD")
            @RequestParam NotificationStatus status,
            @Parameter(description = "Número da página (base 0).", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamanho da página. Máximo 50.", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return ResponseEntity.ok(
                PagedNotificationsResponse.from(
                        getUserNotificationsUseCase.execute(status, page, size)
                )
        );
    }

    @Operation(
            summary = "Contar notificações não lidas",
            description = "Retorna a quantidade de notificações não lidas e não ocultadas do usuário autenticado.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contagem retornada com sucesso.",
                    content = @Content(schema = @Schema(implementation = UnreadCountResponse.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount() {
        var unreadCount = getUnreadCountUseCase.execute();

        var response = UnreadCountResponse.from(unreadCount);

        return ResponseEntity.ok(response);
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
            @PathVariable UUID notificationId
    ) {
        markAsReadUseCase.execute(notificationId);

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
            @PathVariable UUID notificationId
    ) {
        dismissUseCase.execute(notificationId);

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
    public ResponseEntity<Void> markAllAsRead() {
        markAllAsReadUseCase.execute();

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read")
    public ResponseEntity<Void> markAsRead(
            @RequestBody MarkAsReadNotificationsRequest request
    ){


    }

}