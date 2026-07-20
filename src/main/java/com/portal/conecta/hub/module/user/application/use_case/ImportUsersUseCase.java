package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.command.CreateUserCommand;
import com.portal.conecta.hub.module.user.application.command.ImportUsersCommand;
import com.portal.conecta.hub.module.user.application.importer.UserImportRow;
import com.portal.conecta.hub.module.user.application.importer.UserImportSpreadsheetParser;
import com.portal.conecta.hub.module.user.application.result.UserImportResult;
import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.policy.UserEmailPolicy;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ImportUsersUseCase {

    private final UserImportSpreadsheetParser spreadsheetParser;
    private final CreateUserUseCase createUserUseCase;
    private final UserRepository userRepository;
    private final UserEmailPolicy userEmailPolicy;
    private final UserPermissionValidator permissionValidator;
    private final RequestContextProvider contextProvider;

    public ImportUsersUseCase(
            UserImportSpreadsheetParser spreadsheetParser,
            CreateUserUseCase createUserUseCase,
            UserRepository userRepository,
            UserEmailPolicy userEmailPolicy,
            UserPermissionValidator permissionValidator,
            RequestContextProvider contextProvider
    ) {
        this.spreadsheetParser = spreadsheetParser;
        this.createUserUseCase = createUserUseCase;
        this.userRepository = userRepository;
        this.userEmailPolicy = userEmailPolicy;
        this.permissionValidator = permissionValidator;
        this.contextProvider = contextProvider;
    }

    public UserImportResult execute(ImportUsersCommand command) {
        if (command == null) {
            throw new InvalidUserDataException("A requisição de importação é obrigatória.");
        }

        RequestContext context = contextProvider.getRequestContext();
        List<UserImportResult.RowResult> results = new ArrayList<>();
        Set<String> importedEmails = new HashSet<>();
        int created = 0;
        int skipped = 0;

        for (UserImportRow row : spreadsheetParser.parse(command.file())) {
            try {
                TypeUser type = parseType(row.typeUser());
                String email = userEmailPolicy.validateForCreation(row.email(), type);
                CreateUserCommand createCommand = new CreateUserCommand(row.name(), email, type);

                if (!importedEmails.add(email)) {
                    results.add(error(row.line(), "E-mail duplicado na planilha."));
                    continue;
                }
                if (userRepository.existsByEmailIgnoreCase(email)) {
                    if (command.existingEmailHandling() == ImportUsersCommand.ExistingEmailHandling.SKIP) {
                        results.add(new UserImportResult.RowResult(row.line(), UserImportResult.Status.SKIPPED,
                                "Usuário existente ignorado."));
                        skipped++;
                    } else {
                        results.add(error(row.line(), "E-mail já cadastrado."));
                    }
                    continue;
                }

                if (command.dryRun()) {
                    permissionValidator.validateCanCreate(context.userType(), type);
                } else {
                    createUserUseCase.execute(createCommand);
                }
                results.add(new UserImportResult.RowResult(row.line(), UserImportResult.Status.CREATED,
                        command.dryRun() ? "Linha válida." : "Usuário criado."));
                if (!command.dryRun()) {
                    created++;
                }
            } catch (EmailAlreadyInUseException exception) {
                results.add(error(row.line(), "E-mail já cadastrado."));
            } catch (InvalidUserDataException exception) {
                results.add(error(row.line(), exception.getMessage()));
            } catch (UserPermissionDeniedException exception) {
                results.add(error(row.line(), "Usuário sem permissão para criar este type_user."));
            } catch (RuntimeException exception) {
                results.add(error(row.line(), "Não foi possível importar esta linha."));
            }
        }
        return new UserImportResult(command.dryRun(), created, skipped, List.copyOf(results));
    }

    private TypeUser parseType(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserDataException("type_user é obrigatório.");
        }
        try {
            return TypeUser.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidUserDataException("type_user inválido.");
        }
    }

    private UserImportResult.RowResult error(int line, String message) {
        return new UserImportResult.RowResult(line, UserImportResult.Status.ERROR, message);
    }
}
