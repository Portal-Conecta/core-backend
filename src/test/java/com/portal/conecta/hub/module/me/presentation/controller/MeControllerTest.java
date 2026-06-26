package com.portal.conecta.hub.module.me.presentation.controller;

import com.portal.conecta.hub.module.me.application.use_case.GetMeUseCase;
import com.portal.conecta.hub.module.me.application.use_case.GetMyCoursesUseCase;
import com.portal.conecta.hub.module.me.presentation.controller.MeController;
import com.portal.conecta.hub.module.me.presentation.dto.MyProfileResponse;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MeControllerTest {

    @Mock
    private GetMeUseCase getMeUseCase;

    @Mock
    private GetMyCoursesUseCase getMyCoursesUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MeController(getMyCoursesUseCase, getMeUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getMeReturnsAuthenticatedUserProfileOnly() throws Exception {
        UUID userId = UUID.randomUUID();

        when(getMeUseCase.execute()).thenReturn(new MyProfileResponse(
                userId,
                "Lucas Eckert",
                "lucas@senai.br",
                TypeUser.STUDENT,
                "https://cdn.example.com/avatar.png"
        ));

        mockMvc.perform(get("/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Lucas Eckert"))
                .andExpect(jsonPath("$.email").value("lucas@senai.br"))
                .andExpect(jsonPath("$.typeUser").value("STUDENT"))
                .andExpect(jsonPath("$.avatarUrl").value("https://cdn.example.com/avatar.png"))
                .andExpect(jsonPath("$", not(hasKey("password"))))
                .andExpect(jsonPath("$", not(hasKey("passwordHash"))))
                .andExpect(jsonPath("$", not(hasKey("active"))))
                .andExpect(jsonPath("$", not(hasKey("createdAt"))))
                .andExpect(jsonPath("$", not(hasKey("updatedAt"))))
                .andExpect(jsonPath("$", not(hasKey("deletedAt"))))
                .andExpect(jsonPath("$", not(hasKey("createdBy"))))
                .andExpect(jsonPath("$", not(hasKey("updatedBy"))))
                .andExpect(jsonPath("$", not(hasKey("deletedBy"))))
                .andExpect(jsonPath("$", not(hasKey("courses"))))
                .andExpect(jsonPath("$", not(hasKey("classes"))));

        verify(getMeUseCase).execute();
        verifyNoInteractions(getMyCoursesUseCase);
    }

    @Test
    void getMeReturns404WhenAuthenticatedUserIsNotFound() throws Exception {
        when(getMeUseCase.execute()).thenThrow(new UserNotFoundException());

        mockMvc.perform(get("/me"))
                .andExpect(status().isNotFound());

        verify(getMeUseCase).execute();
        verifyNoInteractions(getMyCoursesUseCase);
    }
}