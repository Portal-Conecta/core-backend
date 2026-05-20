package com.portal.conecta.hub.module.user.presentation.controller;

import com.portal.conecta.hub.module.user.application.use_case.CreateUserUseCase;
import com.portal.conecta.hub.module.user.application.use_case.GetAllUserUseCase;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.presentation.dto.CreateUserRequest;
import com.portal.conecta.hub.module.user.presentation.dto.CreateUserResponse;
import com.portal.conecta.hub.module.user.presentation.dto.ListUsersRequest;
import com.portal.conecta.hub.module.user.presentation.dto.ListUsersResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetAllUserUseCase getAllUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase, GetAllUserUseCase getAllUserUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.getAllUserUseCase = getAllUserUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateUserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserEntity createdUser = createUserUseCase.execute(request.toCommand());

        return ResponseEntity.created(URI.create("/users/" + createdUser.getId()))
                .body(CreateUserResponse.from(createdUser));
    }

    @GetMapping
    public ResponseEntity<ListUsersResponse> list(@Valid @ModelAttribute ListUsersRequest request) {
        Page<UserEntity> users = getAllUserUseCase.execute(request.toQuery());

        return ResponseEntity.ok(ListUsersResponse.from(users));
    }
}
