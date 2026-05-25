package com.portal.conecta.hub.module.user.presentation.controller;

import com.portal.conecta.hub.module.user.application.command.DeactivateUserCommand;
import com.portal.conecta.hub.module.user.application.command.UpdateUserCommand;
import com.portal.conecta.hub.module.user.application.use_case.CreateUserUseCase;
import com.portal.conecta.hub.module.user.application.use_case.DeactivateUserUseCase;
import com.portal.conecta.hub.module.user.application.use_case.GetAllUserUseCase;
import com.portal.conecta.hub.module.user.application.use_case.UpdateUserUseCase;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;

import com.portal.conecta.hub.module.user.presentation.dto.request.CreateUserRequest;
import com.portal.conecta.hub.module.user.presentation.dto.request.ListUsersRequest;
import com.portal.conecta.hub.module.user.presentation.dto.request.UpdateUserRequest;

import com.portal.conecta.hub.module.user.presentation.dto.response.CreateUserResponse;
import com.portal.conecta.hub.module.user.presentation.dto.response.ListUsersResponse;
import com.portal.conecta.hub.module.user.presentation.dto.response.UpdateUserResponse;

import jakarta.validation.Valid;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetAllUserUseCase getAllUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;

    public UserController(
            CreateUserUseCase createUserUseCase,
            GetAllUserUseCase getAllUserUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeactivateUserUseCase deactivateUserUseCase
    ) {
        this.createUserUseCase = createUserUseCase;
        this.getAllUserUseCase = getAllUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateUserResponse> create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserEntity createdUser = createUserUseCase.execute(request.toCommand());

        return ResponseEntity.created(URI.create("/users/" + createdUser.getId()))
                .body(CreateUserResponse.from(createdUser));
    }

    @GetMapping
    public ResponseEntity<ListUsersResponse> list(
            @Valid @ModelAttribute ListUsersRequest request
    ) {
        Page<UserEntity> users = getAllUserUseCase.execute(request.toQuery());

        return ResponseEntity.ok(ListUsersResponse.from(users));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UpdateUserResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserEntity updated = updateUserUseCase.execute(
                new UpdateUserCommand(
                        id,
                        request.name(),
                        request.email(),
                        request.avatarUrl()
                )
        );

        return ResponseEntity.ok(UpdateUserResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        deactivateUserUseCase.execute(new DeactivateUserCommand(id));

        return ResponseEntity.noContent().build();
    }
}