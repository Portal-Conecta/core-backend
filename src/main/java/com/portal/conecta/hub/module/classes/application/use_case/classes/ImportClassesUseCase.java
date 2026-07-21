package com.portal.conecta.hub.module.classes.application.use_case.classes;

import com.portal.conecta.hub.module.classes.application.command.CreateClassCommand;
import com.portal.conecta.hub.module.classes.application.command.ImportClassesCommand;
import com.portal.conecta.hub.module.classes.application.importer.ClassImportRow;
import com.portal.conecta.hub.module.classes.application.importer.ClassImportSpreadsheetParser;
import com.portal.conecta.hub.module.classes.application.result.ClassImportResult;
import com.portal.conecta.hub.module.classes.domain.exception.ClassNumberAlreadyInUseException;
import com.portal.conecta.hub.module.classes.domain.exception.InvalidClassDataException;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.validator.ClassPermissionValidator;
import com.portal.conecta.hub.module.course.domain.exception.CourseNotFoundException;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ImportClassesUseCase {

    private final ClassImportSpreadsheetParser spreadsheetParser;
    private final CreateClassUseCase createClassUseCase;
    private final CourseRepository courseRepository;
    private final ClassRepository classRepository;
    private final ClassPermissionValidator permissionValidator;
    private final RequestContextProvider contextProvider;

    public ImportClassesUseCase(
            ClassImportSpreadsheetParser spreadsheetParser,
            CreateClassUseCase createClassUseCase,
            CourseRepository courseRepository,
            ClassRepository classRepository,
            ClassPermissionValidator permissionValidator,
            RequestContextProvider contextProvider
    ) {
        this.spreadsheetParser = spreadsheetParser;
        this.createClassUseCase = createClassUseCase;
        this.courseRepository = courseRepository;
        this.classRepository = classRepository;
        this.permissionValidator = permissionValidator;
        this.contextProvider = contextProvider;
    }

    public ClassImportResult execute(ImportClassesCommand command) {
        if (command == null) {
            throw new InvalidClassDataException("A requisição de importação é obrigatória.");
        }

        RequestContext context = contextProvider.getRequestContext();
        List<ClassImportResult.RowResult> results = new ArrayList<>();
        Set<ClassReference> importedClasses = new HashSet<>();
        int created = 0;
        int skipped = 0;

        for (ClassImportRow row : spreadsheetParser.parse(command.file())) {
            try {
                CourseEntity course = findCourse(row.courseCode());
                Integer number = parseNumber(row.number());
                Shift shift = parseShift(row.shift());
                ClassReference reference = new ClassReference(course.getId(), number);

                if (!importedClasses.add(reference)) {
                    results.add(error(row.line(), "Turma duplicada na planilha."));
                    continue;
                }
                if (classRepository.existsByNumberAndCourseIdAndDeletedAtIsNull(number, course.getId())) {
                    if (command.existingClassHandling() == ImportClassesCommand.ExistingClassHandling.SKIP) {
                        results.add(new ClassImportResult.RowResult(row.line(), ClassImportResult.Status.SKIPPED,
                                "Turma existente ignorada."));
                        skipped++;
                    } else {
                        results.add(error(row.line(), "Já existe turma ativa com este número para o curso."));
                    }
                    continue;
                }

                CreateClassCommand createCommand = new CreateClassCommand(shift, course.getId(), number);
                if (command.dryRun()) {
                    validatePermission(context);
                } else {
                    createClassUseCase.execute(createCommand);
                }
                results.add(new ClassImportResult.RowResult(row.line(), ClassImportResult.Status.CREATED,
                        command.dryRun() ? "Linha válida." : "Turma criada."));
                if (!command.dryRun()) {
                    created++;
                }
            } catch (CourseNotFoundException exception) {
                results.add(error(row.line(), "Curso não encontrado ou removido."));
            } catch (ClassNumberAlreadyInUseException exception) {
                results.add(error(row.line(), "Já existe turma ativa com este número para o curso."));
            } catch (InvalidClassDataException exception) {
                results.add(error(row.line(), exception.getMessage()));
            } catch (UserPermissionDeniedException exception) {
                results.add(error(row.line(), "Usuário sem permissão para criar turmas."));
            } catch (RuntimeException exception) {
                results.add(error(row.line(), "Não foi possível importar esta linha."));
            }
        }
        return new ClassImportResult(command.dryRun(), created, skipped, List.copyOf(results));
    }

    private CourseEntity findCourse(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            throw new InvalidClassDataException("course_code é obrigatório.");
        }
        return courseRepository.findByCodeAndDeletedAtIsNull(courseCode.trim())
                .orElseThrow(CourseNotFoundException::new);
    }

    private Integer parseNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidClassDataException("number é obrigatório.");
        }
        try {
            int number = Integer.parseInt(value.trim());
            if (number < 1) {
                throw new InvalidClassDataException("number deve ser maior que zero.");
            }
            return number;
        } catch (NumberFormatException exception) {
            throw new InvalidClassDataException("number deve ser um número inteiro.");
        }
    }

    private Shift parseShift(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidClassDataException("shift é obrigatório.");
        }
        try {
            return Shift.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidClassDataException("shift inválido.");
        }
    }

    private void validatePermission(RequestContext context) {
        if (!permissionValidator.canCreate(context.userType())) {
            throw new UserPermissionDeniedException("Usuário não tem permissão para realizar essa operação.");
        }
    }

    private ClassImportResult.RowResult error(int line, String message) {
        return new ClassImportResult.RowResult(line, ClassImportResult.Status.ERROR, message);
    }

    private record ClassReference(java.util.UUID courseId, Integer number) {
    }
}
