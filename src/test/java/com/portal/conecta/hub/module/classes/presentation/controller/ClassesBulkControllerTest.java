package com.portal.conecta.hub.module.classes.presentation.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.portal.conecta.hub.module.classes.application.usecase.BulkClassesUseCase;
import com.portal.conecta.hub.module.classes.presentation.dto.BulkClassItemResponse;
import com.portal.conecta.hub.module.classes.presentation.dto.BulkClassesResponse;

class ClassesBulkControllerTest {

    private MockMvc mockMvc;

    private BulkClassesUseCase bulkClassesUseCase;

    @BeforeEach
    void setUp() {
        bulkClassesUseCase = org.mockito.Mockito.mock(BulkClassesUseCase.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ClassesBulkController(bulkClassesUseCase))
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldDefaultIncludeInactiveToFalse() throws Exception {
        UUID classId = UUID.randomUUID();
        BulkClassesResponse response = new BulkClassesResponse(
                List.of(new BulkClassItemResponse(classId, true)),
                List.of(classId),
                List.of());

        when(bulkClassesUseCase.execute(eq(List.of(classId)), eq(false))).thenReturn(response);

        mockMvc.perform(post("/classes/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": ["%s"]
                                }
                                """.formatted(classId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foundIds[0]").value(classId.toString()))
                .andExpect(jsonPath("$.missingIds").isEmpty())
                .andExpect(jsonPath("$.items[0].id").value(classId.toString()));

        verify(bulkClassesUseCase).execute(eq(List.of(classId)), eq(false));
    }

    @Test
    void shouldPassIncludeInactiveTrue() throws Exception {
        UUID classId = UUID.randomUUID();
        BulkClassesResponse response = new BulkClassesResponse(
                List.of(new BulkClassItemResponse(classId, false)),
                List.of(classId),
                List.of());

        when(bulkClassesUseCase.execute(eq(List.of(classId)), eq(true))).thenReturn(response);

        mockMvc.perform(post("/classes/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": ["%s"],
                                  "includeInactive": true
                                }
                                """.formatted(classId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].active").value(false));

        verify(bulkClassesUseCase).execute(eq(List.of(classId)), eq(true));
    }

    @Test
    void shouldReturnBadRequestWhenIdsAreMissing() throws Exception {
        mockMvc.perform(post("/classes/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "includeInactive": true
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bulkClassesUseCase);
    }
}
