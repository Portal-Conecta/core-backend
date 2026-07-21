package com.portal.conecta.hub.module.classes.application.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.portal.conecta.hub.module.classes.domain.exception.InvalidClassDataException;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ClassImportSpreadsheetParserTest {

    private final ClassImportSpreadsheetParser parser = new ClassImportSpreadsheetParser();

    @Test
    void parsesCsvWithRequiredHeaders() {
        MockMultipartFile file = new MockMultipartFile("file", "classes.csv", "text/csv", """
                course_code,number,shift
                DEV-01,78,FULL_AM_PM
                """.getBytes());

        List<ClassImportRow> rows = parser.parse(file);

        assertThat(rows).containsExactly(new ClassImportRow(2, "DEV-01", "78", "FULL_AM_PM"));
    }

    @Test
    void parsesXlsxWithRequiredHeaders() throws Exception {
        byte[] content;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet();
            sheet.createRow(0).createCell(0).setCellValue("course_code");
            sheet.getRow(0).createCell(1).setCellValue("number");
            sheet.getRow(0).createCell(2).setCellValue("shift");
            sheet.createRow(1).createCell(0).setCellValue("DEV-01");
            sheet.getRow(1).createCell(1).setCellValue(78);
            sheet.getRow(1).createCell(2).setCellValue("FULL_AM_PM");
            workbook.write(output);
            content = output.toByteArray();
        }

        List<ClassImportRow> rows = parser.parse(new MockMultipartFile("file", "classes.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content));

        assertThat(rows).containsExactly(new ClassImportRow(2, "DEV-01", "78", "FULL_AM_PM"));
    }

    @Test
    void rejectsMissingRequiredHeader() {
        MockMultipartFile file = new MockMultipartFile("file", "classes.csv", "text/csv", "course_code,number\nDEV-01,78".getBytes());

        assertThrows(InvalidClassDataException.class, () -> parser.parse(file));
    }
}
