package com.portal.conecta.hub.module.room.presentation.controller;

import com.portal.conecta.hub.module.room.application.use_case.CreateRoomUseCase;
import com.portal.conecta.hub.module.room.application.use_case.GetAllRoomUseCase;
import com.portal.conecta.hub.module.room.application.use_case.GetRoomByIdUseCase;
import com.portal.conecta.hub.module.room.presentation.dto.CreateRoomRequest;
import com.portal.conecta.hub.module.room.presentation.dto.CreateRoomResponse;
import com.portal.conecta.hub.module.room.presentation.dto.RoomResponse;
import com.portal.conecta.hub.module.room.presentation.mapper.RoomMapper;
import com.portal.conecta.hub.shared.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Salas", description = "Operações para consulta e administração de salas físicas do Hub.")
@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final CreateRoomUseCase createRoomUseCase;
    private final GetAllRoomUseCase getAllRoomUseCase;
    private final GetRoomByIdUseCase getRoomByIdUseCase;
    private final RoomMapper roomMapper;

    public RoomController(CreateRoomUseCase createRoomUseCase, GetAllRoomUseCase getAllRoomUseCase, GetRoomByIdUseCase getRoomByIdUseCase, RoomMapper roomMapper) {
        this.createRoomUseCase = createRoomUseCase;
        this.getAllRoomUseCase = getAllRoomUseCase;
        this.getRoomByIdUseCase = getRoomByIdUseCase;
        this.roomMapper = roomMapper;
    }

    @Operation(
            summary = "Cria sala",
            description = "Cadastra uma nova sala física no Hub. Apenas perfis autorizados podem executar esta operação.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Sala criada com sucesso.",
                    content = @Content(schema = @Schema(implementation = CreateRoomResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou número de sala já em uso.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário autenticado sem permissão para criar salas.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping
    public ResponseEntity<CreateRoomResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para criação da sala.",
                    required = true
            )
            @RequestBody @Valid CreateRoomRequest request) {
        var room = createRoomUseCase.execute(roomMapper.toCommand(request));
        return ResponseEntity.created(URI.create("/rooms/" + room.getId()))
                .body(CreateRoomResponse.from(room));
    }

    @Operation(
            summary = "Lista salas ativas",
            description = "Retorna todas as salas físicas ativas cadastradas no Hub. Salas removidas logicamente não são retornadas.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de salas ativas retornada com sucesso.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RoomResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<RoomResponse>> findAll() {
        List<RoomResponse> rooms = getAllRoomUseCase.execute()
                .stream()
                .map(RoomResponse::from)
                .toList();
        return ResponseEntity.ok(rooms);
    }

    @Operation(
            summary = "Consulta sala por ID",
            description = "Retorna os dados de uma sala ativa pelo identificador informado. Salas removidas logicamente retornam 404.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sala encontrada com sucesso.",
                    content = @Content(schema = @Schema(implementation = RoomResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sala inexistente ou removida logicamente.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> findById(
            @Parameter(description = "Identificador da sala.", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                RoomResponse.from(getRoomByIdUseCase.execute(id))
        );
    }
}