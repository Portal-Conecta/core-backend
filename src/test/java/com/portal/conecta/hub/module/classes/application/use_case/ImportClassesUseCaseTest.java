package com.portal.conecta.hub.module.classes.application.use_case;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.classes.application.command.CreateClassCommand;
import com.portal.conecta.hub.module.classes.application.command.ImportClassesCommand;
import com.portal.conecta.hub.module.classes.application.importer.ClassImportRow;
import com.portal.conecta.hub.module.classes.application.importer.ClassImportSpreadsheetParser;
import com.portal.conecta.hub.module.classes.application.result.ClassImportResult;
import com.portal.conecta.hub.module.classes.application.use_case.classes.CreateClassUseCase;
import com.portal.conecta.hub.module.classes.application.use_case.classes.ImportClassesUseCase;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.validator.ClassPermissionValidator;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ImportClassesUseCaseTest {

    @Mock private ClassImportSpreadsheetParser spreadsheetParser;
    @Mock private CreateClassUseCase createClassUseCase;
    @Mock private CourseRepository courseRepository;
    @Mock private ClassRepository classRepository;
    @Mock private RequestContextProvider contextProvider;
    @Mock private CourseEntity course;

    private ImportClassesUseCase useCase;
    private MockMultipartFile file;
    private UUID courseId;

    @BeforeEach
    void setUp() {
        useCase = new ImportClassesUseCase(spreadsheetParser, createClassUseCase, courseRepository,
                classRepository, new ClassPermissionValidator(), contextProvider);
        file = new MockMultipartFile("file", "classes.csv", "text/csv", "ignored".getBytes());
        courseId = UUID.randomUUID();
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.ADMIN, List.of()));
    }

    @Test
    void createsValidRowsAndReportsDuplicatedClassInFile() {
        when(spreadsheetParser.parse(file)).thenReturn(List.of(
                new ClassImportRow(2, "DEV-01", "78", "normal"),
                new ClassImportRow(3, "DEV-01", "78", "normal")
        ));
        when(course.getId()).thenReturn(courseId);
        when(courseRepository.findByCodeAndDeletedAtIsNull("DEV-01")).thenReturn(Optional.of(course));
        when(classRepository.existsByNumberAndCourseIdAndDeletedAtIsNull(anyInt(), eq(courseId))).thenReturn(false);

        ClassImportResult result = useCase.execute(new ImportClassesCommand(file,
                ImportClassesCommand.ExistingClassHandling.REJECT, false));

        ArgumentCaptor<CreateClassCommand> command = ArgumentCaptor.forClass(CreateClassCommand.class);
        verify(createClassUseCase).execute(command.capture());
        assertThat(command.getValue().courseId()).isEqualTo(courseId);
        assertThat(command.getValue().number()).isEqualTo(78);
        assertThat(command.getValue().shift()).isEqualTo(Shift.FULL_AM_PM);
        assertThat(result.created()).isEqualTo(1);
        assertThat(result.rows()).extracting(ClassImportResult.RowResult::status)
                .containsExactly(ClassImportResult.Status.CREATED, ClassImportResult.Status.ERROR);
        assertThat(result.rows().get(1).message()).isEqualTo("Turma duplicada na planilha.");
    }

    @Test
    void skipsExistingClassWhenConfigured() {
        when(spreadsheetParser.parse(file)).thenReturn(List.of(new ClassImportRow(2, "DEV-01", "78", "segundo turno")));
        when(course.getId()).thenReturn(courseId);
        when(courseRepository.findByCodeAndDeletedAtIsNull("DEV-01")).thenReturn(Optional.of(course));
        when(classRepository.existsByNumberAndCourseIdAndDeletedAtIsNull(78, courseId)).thenReturn(true);

        ClassImportResult result = useCase.execute(new ImportClassesCommand(file,
                ImportClassesCommand.ExistingClassHandling.SKIP, false));

        verify(createClassUseCase, never()).execute(any());
        assertThat(result.created()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.rows().getFirst().status()).isEqualTo(ClassImportResult.Status.SKIPPED);
    }

    @Test
    void dryRunValidatesCourseAndPermissionWithoutCreatingClass() {
        when(spreadsheetParser.parse(file)).thenReturn(List.of(new ClassImportRow(2, "DEV-01", "78", "normal")));
        when(course.getId()).thenReturn(courseId);
        when(courseRepository.findByCodeAndDeletedAtIsNull("DEV-01")).thenReturn(Optional.of(course));
        when(classRepository.existsByNumberAndCourseIdAndDeletedAtIsNull(78, courseId)).thenReturn(false);

        ClassImportResult result = useCase.execute(new ImportClassesCommand(file,
                ImportClassesCommand.ExistingClassHandling.REJECT, true));

        verify(createClassUseCase, never()).execute(any());
        assertThat(result.dryRun()).isTrue();
        assertThat(result.created()).isZero();
        assertThat(result.rows().getFirst().message()).isEqualTo("Linha válida.");
    }

    @Test
    void mapsSecondShiftToFullPmNt() {
        when(spreadsheetParser.parse(file)).thenReturn(List.of(new ClassImportRow(2, "DEV-01", "78", "segundo turno")));
        when(course.getId()).thenReturn(courseId);
        when(courseRepository.findByCodeAndDeletedAtIsNull("DEV-01")).thenReturn(Optional.of(course));
        when(classRepository.existsByNumberAndCourseIdAndDeletedAtIsNull(78, courseId)).thenReturn(false);

        useCase.execute(new ImportClassesCommand(file, ImportClassesCommand.ExistingClassHandling.REJECT, false));

        ArgumentCaptor<CreateClassCommand> command = ArgumentCaptor.forClass(CreateClassCommand.class);
        verify(createClassUseCase).execute(command.capture());
        assertThat(command.getValue().shift()).isEqualTo(Shift.FULL_PM_NT);
    }

    @Test
    void reportsMissingCourseByCode() {
        when(spreadsheetParser.parse(file)).thenReturn(List.of(new ClassImportRow(2, "UNKNOWN", "78", "FULL_AM_PM")));
        when(courseRepository.findByCodeAndDeletedAtIsNull(anyString())).thenReturn(Optional.empty());

        ClassImportResult result = useCase.execute(new ImportClassesCommand(file,
                ImportClassesCommand.ExistingClassHandling.REJECT, false));

        verify(createClassUseCase, never()).execute(any());
        assertThat(result.rows().getFirst()).isEqualTo(new ClassImportResult.RowResult(2,
                ClassImportResult.Status.ERROR, "Curso não encontrado ou removido."));
    }

    @Test
    void reportsInvalidShift() {
        when(spreadsheetParser.parse(file)).thenReturn(List.of(new ClassImportRow(2, "DEV-01", "78", "INVALID")));
        when(courseRepository.findByCodeAndDeletedAtIsNull("DEV-01")).thenReturn(Optional.of(course));

        ClassImportResult result = useCase.execute(new ImportClassesCommand(file,
                ImportClassesCommand.ExistingClassHandling.REJECT, false));

        verify(createClassUseCase, never()).execute(any());
        assertThat(result.rows().getFirst().message()).isEqualTo("shift inválido. Use normal ou segundo turno.");
    }
}
