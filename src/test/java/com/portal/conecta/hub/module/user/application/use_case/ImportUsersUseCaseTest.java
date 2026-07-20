package com.portal.conecta.hub.module.user.application.use_case;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.user.application.command.CreateUserCommand;
import com.portal.conecta.hub.module.user.application.command.ImportUsersCommand;
import com.portal.conecta.hub.module.user.application.importer.UserImportRow;
import com.portal.conecta.hub.module.user.application.importer.UserImportSpreadsheetParser;
import com.portal.conecta.hub.module.user.application.result.UserImportResult;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.policy.UserEmailPolicy;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ImportUsersUseCaseTest {

    @Mock private UserImportSpreadsheetParser spreadsheetParser;
    @Mock private CreateUserUseCase createUserUseCase;
    @Mock private UserRepository userRepository;
    @Mock private RequestContextProvider contextProvider;

    private ImportUsersUseCase useCase;
    private MockMultipartFile file;

    @BeforeEach
    void setUp() {
        useCase = new ImportUsersUseCase(spreadsheetParser, createUserUseCase, userRepository,
                new UserEmailPolicy(), new UserPermissionValidator(), contextProvider);
        file = new MockMultipartFile("file", "users.csv", "text/csv", "ignored".getBytes());
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.ADMIN, List.of()));
    }

    @Test
    void createsValidRowsAndReportsDuplicatedEmailInFile() {
        when(spreadsheetParser.parse(file)).thenReturn(List.of(
                new UserImportRow(2, "Ana", "ANA@ESTUDANTE.SESISENAI.ORG.BR", "STUDENT"),
                new UserImportRow(3, "Ana two", "ana@estudante.sesisenai.org.br", "STUDENT")
        ));
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);

        UserImportResult result = useCase.execute(new ImportUsersCommand(file,
                ImportUsersCommand.ExistingEmailHandling.REJECT, false));

        ArgumentCaptor<CreateUserCommand> command = ArgumentCaptor.forClass(CreateUserCommand.class);
        verify(createUserUseCase).execute(command.capture());
        assertThat(command.getValue().email()).isEqualTo("ana@estudante.sesisenai.org.br");
        assertThat(result.created()).isEqualTo(1);
        assertThat(result.rows()).extracting(UserImportResult.RowResult::status)
                .containsExactly(UserImportResult.Status.CREATED, UserImportResult.Status.ERROR);
        assertThat(result.rows().get(1).message()).isEqualTo("E-mail duplicado na planilha.");
    }

    @Test
    void skipsExistingEmailWhenConfigured() {
        when(spreadsheetParser.parse(file)).thenReturn(List.of(
                new UserImportRow(2, "Ana", "ana@estudante.sesisenai.org.br", "STUDENT")
        ));
        when(userRepository.existsByEmailIgnoreCase("ana@estudante.sesisenai.org.br")).thenReturn(true);

        UserImportResult result = useCase.execute(new ImportUsersCommand(file,
                ImportUsersCommand.ExistingEmailHandling.SKIP, false));

        verify(createUserUseCase, never()).execute(any());
        assertThat(result.created()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.rows().getFirst().status()).isEqualTo(UserImportResult.Status.SKIPPED);
    }

    @Test
    void dryRunDoesNotCreateUsers() {
        when(spreadsheetParser.parse(file)).thenReturn(List.of(
                new UserImportRow(2, "Ana", "ana@estudante.sesisenai.org.br", "STUDENT")
        ));
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);

        UserImportResult result = useCase.execute(new ImportUsersCommand(file,
                ImportUsersCommand.ExistingEmailHandling.REJECT, true));

        verify(createUserUseCase, never()).execute(any());
        assertThat(result.dryRun()).isTrue();
        assertThat(result.created()).isZero();
        assertThat(result.rows().getFirst().message()).isEqualTo("Linha válida.");
    }

    @Test
    void reportsPermissionErrorWithoutCreatingRow() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.WEG, List.of()));
        when(spreadsheetParser.parse(file)).thenReturn(List.of(
                new UserImportRow(2, "Teacher", "teacher@edu.sc.senai.br", "TEACHER")
        ));
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);

        UserImportResult result = useCase.execute(new ImportUsersCommand(file,
                ImportUsersCommand.ExistingEmailHandling.REJECT, true));

        verify(createUserUseCase, never()).execute(any());
        assertThat(result.rows().getFirst().status()).isEqualTo(UserImportResult.Status.ERROR);
    }
}
