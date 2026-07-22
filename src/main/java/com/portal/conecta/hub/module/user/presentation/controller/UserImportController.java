package com.portal.conecta.hub.module.user.presentation.controller;

import com.portal.conecta.hub.module.user.application.command.ImportUsersCommand;
import com.portal.conecta.hub.module.user.application.result.UserImportResult;
import com.portal.conecta.hub.module.user.application.use_case.ImportUsersUseCase;
import com.portal.conecta.hub.module.user.presentation.dto.response.UserImportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Importações", description = "Importação de dados administrativos.")
@RestController
@RequestMapping("/imports")
public class UserImportController {

    private static final byte[] TEMPLATE = "name,email\nAna Silva,ana@estudante.sesisenai.org.br\n"
            .getBytes(StandardCharsets.UTF_8);

    private final ImportUsersUseCase importUsersUseCase;

    public UserImportController(ImportUsersUseCase importUsersUseCase) {
        this.importUsersUseCase = importUsersUseCase;
    }

    @Operation(summary = "Importa usuários por planilha", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserImportResponse> importUsers(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "REJECT") ImportUsersCommand.ExistingEmailHandling onExisting,
            @RequestParam(defaultValue = "false") boolean dryRun
    ) {
        UserImportResult result = importUsersUseCase.execute(new ImportUsersCommand(file, onExisting, dryRun));
        return ResponseEntity.ok(UserImportResponse.from(result));
    }

    @Operation(summary = "Baixa o modelo de importação de usuários", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/templates/users")
    public ResponseEntity<byte[]> userTemplate() {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("import-users-template.csv", StandardCharsets.UTF_8).build().toString())
                .body(TEMPLATE);
    }
}
