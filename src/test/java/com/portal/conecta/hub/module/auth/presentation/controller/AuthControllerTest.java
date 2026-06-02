package com.portal.conecta.hub.module.auth.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.portal.conecta.hub.module.auth.application.result.RefreshTokenResult;
import com.portal.conecta.hub.module.auth.application.use_case.LoginUseCase;
import com.portal.conecta.hub.module.auth.application.use_case.RefreshTokenUseCase;
import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.exception.RefreshTokenException;
import com.portal.conecta.hub.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private LoginUseCase loginUseCase;

    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(loginUseCase, refreshTokenUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returns200WithNewTokensOnSuccess() throws Exception {
        when(refreshTokenUseCase.execute(any()))
                .thenReturn(new RefreshTokenResult("new-access", "new-refresh", 900L));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"valid-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void returns400WhenRefreshTokenIsMissing() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400WhenRefreshTokenIsBlank() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns401WhenTokenIsExpiredOrInvalid() throws Exception {
        when(refreshTokenUseCase.execute(any()))
                .thenThrow(new AuthException("Invalid or expired refresh token"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"expired-token\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returns401WhenAccessTokenSentAsRefresh() throws Exception {
        when(refreshTokenUseCase.execute(any()))
                .thenThrow(new AuthException("Invalid token type"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"access-token\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returns403WhenUserIsInactive() throws Exception {
        when(refreshTokenUseCase.execute(any()))
                .thenThrow(new RefreshTokenException("User is inactive or blocked"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"valid-token\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void returns403WhenUserDoesNotExist() throws Exception {
        when(refreshTokenUseCase.execute(any()))
                .thenThrow(new RefreshTokenException("User not found"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"valid-token\"}"))
                .andExpect(status().isForbidden());
    }
}