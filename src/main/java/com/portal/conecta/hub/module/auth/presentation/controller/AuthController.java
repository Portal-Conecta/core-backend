package com.portal.conecta.hub.module.auth.presentation.controller;

import com.portal.conecta.hub.module.auth.application.command.LoginCommand;
import com.portal.conecta.hub.module.auth.application.usecase.LoginUseCase;
import com.portal.conecta.hub.module.auth.presentation.dto.LoginRequest;
import com.portal.conecta.hub.module.auth.presentation.dto.LoginResponse;
import com.portal.conecta.hub.module.auth.presentation.mapper.LoginMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final LoginMapper loginMapper;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginCommand command = loginMapper.toCommand(loginRequest);
        LoginResponse response = loginUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

}