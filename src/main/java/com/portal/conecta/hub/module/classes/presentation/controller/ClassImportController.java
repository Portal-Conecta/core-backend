package com.portal.conecta.hub.module.classes.presentation.controller;

import com.portal.conecta.hub.module.classes.application.command.ImportClassesCommand;
import com.portal.conecta.hub.module.classes.application.result.ClassImportResult;
import com.portal.conecta.hub.module.classes.application.use_case.classes.ImportClassesUseCase;
import com.portal.conecta.hub.module.classes.presentation.dto.response.ClassImportResponse;
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
public class ClassImportController {

    private static final byte[] TEMPLATE = "course_code,number,shift\nDEV-01,78,FULL_AM_PM\n"
            .getBytes(StandardCharsets.UTF_8);

    private final ImportClassesUseCase importClassesUseCase;

    public ClassImportController(ImportClassesUseCase importClassesUseCase) {
        this.importClassesUseCase = importClassesUseCase;
    }

    @Operation(summary = "Importa turmas por planilha", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/classes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClassImportResponse> importClasses(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "REJECT") ImportClassesCommand.ExistingClassHandling onExisting,
            @RequestParam(defaultValue = "false") boolean dryRun
    ) {
        ClassImportResult result = importClassesUseCase.execute(new ImportClassesCommand(file, onExisting, dryRun));
        return ResponseEntity.ok(ClassImportResponse.from(result));
    }

    @Operation(summary = "Baixa o modelo de importação de turmas", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/templates/classes")
    public ResponseEntity<byte[]> classTemplate() {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("import-classes-template.csv", StandardCharsets.UTF_8).build().toString())
                .body(TEMPLATE);
    }
}
