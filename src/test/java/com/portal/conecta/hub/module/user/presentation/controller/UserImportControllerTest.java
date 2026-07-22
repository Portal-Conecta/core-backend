package com.portal.conecta.hub.module.user.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.portal.conecta.hub.module.user.application.command.ImportUsersCommand;
import com.portal.conecta.hub.module.user.application.result.UserImportResult;
import com.portal.conecta.hub.module.user.application.use_case.ImportUsersUseCase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserImportControllerTest {

    @Mock private ImportUsersUseCase importUsersUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserImportController(importUsersUseCase)).build();
    }

    @Test
    void importsUsersWithConfiguredOptions() throws Exception {
        when(importUsersUseCase.execute(any())).thenReturn(new UserImportResult(true, 0, 0,
                List.of(new UserImportResult.RowResult(2, UserImportResult.Status.CREATED, "Linha válida."))));
        MockMultipartFile file = new MockMultipartFile("file", "users.csv", "text/csv",
                "name,email,type_user".getBytes());

        mockMvc.perform(multipart("/imports/users").file(file)
                        .param("onExisting", "SKIP")
                        .param("dryRun", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dryRun").value(true))
                .andExpect(jsonPath("$.rows[0].line").value(2))
                .andExpect(jsonPath("$.rows[0].status").value("CREATED"));

        ArgumentCaptor<ImportUsersCommand> command = ArgumentCaptor.forClass(ImportUsersCommand.class);
        verify(importUsersUseCase).execute(command.capture());
        org.junit.jupiter.api.Assertions.assertEquals(ImportUsersCommand.ExistingEmailHandling.SKIP,
                command.getValue().existingEmailHandling());
        org.junit.jupiter.api.Assertions.assertTrue(command.getValue().dryRun());
    }

    @Test
    void providesCsvTemplate() throws Exception {
        mockMvc.perform(get("/imports/templates/users"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name,email")));
    }
}
