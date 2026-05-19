package com.portal.conecta.hub.module.auth.presentation.mapper;

import com.portal.conecta.hub.module.auth.application.command.LoginCommand;
import com.portal.conecta.hub.module.auth.presentation.dto.LoginRequest;
import org.springframework.stereotype.Component;

@Component
public class LoginMapper {

    public LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(
            request.email(),
            request.password()
        );
    }

}