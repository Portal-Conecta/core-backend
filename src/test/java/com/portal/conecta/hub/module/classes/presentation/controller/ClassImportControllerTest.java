package com.portal.conecta.hub.module.classes.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.portal.conecta.hub.module.classes.application.command.ImportClassesCommand;
import com.portal.conecta.hub.module.classes.application.result.ClassImportResult;
import com.portal.conecta.hub.module.classes.application.use_case.classes.ImportClassesUseCase;
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
class ClassImportControllerTest {

    @Mock private ImportClassesUseCase importClassesUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ClassImportController(importClassesUseCase)).build();
    }

    @Test
    void importsClassesWithConfiguredOptions() throws Exception {
        when(importClassesUseCase.execute(any())).thenReturn(new ClassImportResult(true, 0, 0,
                List.of(new ClassImportResult.RowResult(2, ClassImportResult.Status.CREATED, "Linha válida."))));
        MockMultipartFile file = new MockMultipartFile("file", "classes.csv", "text/csv",
                "course_code,number,shift".getBytes());

        mockMvc.perform(multipart("/imports/classes").file(file)
                        .param("onExisting", "SKIP")
                        .param("dryRun", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dryRun").value(true))
                .andExpect(jsonPath("$.rows[0].line").value(2))
                .andExpect(jsonPath("$.rows[0].status").value("CREATED"));

        ArgumentCaptor<ImportClassesCommand> command = ArgumentCaptor.forClass(ImportClassesCommand.class);
        verify(importClassesUseCase).execute(command.capture());
        org.junit.jupiter.api.Assertions.assertEquals(ImportClassesCommand.ExistingClassHandling.SKIP,
                command.getValue().existingClassHandling());
        org.junit.jupiter.api.Assertions.assertTrue(command.getValue().dryRun());
    }

    @Test
    void providesCsvTemplate() throws Exception {
        mockMvc.perform(get("/imports/templates/classes"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("DEV-01,78,normal")));
    }
}
