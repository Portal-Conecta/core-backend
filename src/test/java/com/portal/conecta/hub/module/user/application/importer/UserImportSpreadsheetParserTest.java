package com.portal.conecta.hub.module.user.application.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class UserImportSpreadsheetParserTest {

    private final UserImportSpreadsheetParser parser = new UserImportSpreadsheetParser();

    @Test
    void parsesCsvWithRequiredHeaders() {
        MockMultipartFile file = new MockMultipartFile("file", "users.csv", "text/csv", """
                name,email,type_user
                Ana Silva,ana@estudante.sesisenai.org.br,STUDENT
                """.getBytes());

        List<UserImportRow> rows = parser.parse(file);

        assertThat(rows).containsExactly(new UserImportRow(2, "Ana Silva", "ana@estudante.sesisenai.org.br", "STUDENT"));
    }

    @Test
    void parsesXlsxWithRequiredHeaders() throws Exception {
        byte[] content;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet();
            sheet.createRow(0).createCell(0).setCellValue("name");
            sheet.getRow(0).createCell(1).setCellValue("email");
            sheet.getRow(0).createCell(2).setCellValue("type_user");
            sheet.createRow(1).createCell(0).setCellValue("Ana Silva");
            sheet.getRow(1).createCell(1).setCellValue("ana@estudante.sesisenai.org.br");
            sheet.getRow(1).createCell(2).setCellValue("STUDENT");
            workbook.write(output);
            content = output.toByteArray();
        }

        List<UserImportRow> rows = parser.parse(new MockMultipartFile("file", "users.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content));

        assertThat(rows).containsExactly(new UserImportRow(2, "Ana Silva", "ana@estudante.sesisenai.org.br", "STUDENT"));
    }

    @Test
    void rejectsMissingRequiredHeader() {
        MockMultipartFile file = new MockMultipartFile("file", "users.csv", "text/csv", "name,email\nAna,a@b.com".getBytes());

        assertThrows(InvalidUserDataException.class, () -> parser.parse(file));
    }
}
