package com.portal.conecta.hub.module.user.presentation.controller;

import com.portal.conecta.hub.module.user.application.command.CreateUserCommand;
import com.portal.conecta.hub.module.user.application.command.DeactivateUserCommand;
import com.portal.conecta.hub.module.user.application.use_case.CreateUserUseCase;
import com.portal.conecta.hub.module.user.application.use_case.DeactivateUserUseCase;
import com.portal.conecta.hub.module.user.application.use_case.GetAllUserUseCase;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.presentation.dto.CreateUserRequest;
import com.portal.conecta.hub.module.user.presentation.dto.CreateUserResponse;
import com.portal.conecta.hub.module.user.presentation.dto.ListUsersRequest;
import com.portal.conecta.hub.module.user.presentation.dto.ListUsersResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetAllUserUseCase getAllUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase, GetAllUserUseCase getAllUserUseCase, DeactivateUserUseCase deactivateUserUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.getAllUserUseCase = getAllUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateUserResponse> create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserEntity createdUser = createUserUseCase.execute(new CreateUserCommand(
                request.name(),
                request.email(),
                request.password(),
                request.typeUser()
        ));

        return ResponseEntity.created(URI.create("/users/" + createdUser.getId()))
                .body(CreateUserResponse.from(createdUser));
    }

    @GetMapping
    public ResponseEntity<ListUsersResponse> list(@Valid @ModelAttribute ListUsersRequest request) {
        Page<UserEntity> users = getAllUserUseCase.execute(request.toQuery());

        return ResponseEntity.ok(ListUsersResponse.from(users));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        deactivateUserUseCase.execute(new DeactivateUserCommand(id));
        return ResponseEntity.noContent().build();
    }
}
