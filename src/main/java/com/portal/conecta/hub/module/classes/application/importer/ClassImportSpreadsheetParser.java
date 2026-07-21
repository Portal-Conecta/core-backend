package com.portal.conecta.hub.module.classes.application.importer;

import com.portal.conecta.hub.module.classes.domain.exception.InvalidClassDataException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ClassImportSpreadsheetParser {

    private static final List<String> REQUIRED_HEADERS = List.of("course_code", "number", "shift");

    public List<ClassImportRow> parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidClassDataException("A planilha de turmas é obrigatória.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new InvalidClassDataException("O arquivo deve ser CSV ou XLSX.");
        }

        try (InputStream input = file.getInputStream()) {
            if (fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
                return parseCsv(new InputStreamReader(input, StandardCharsets.UTF_8));
            }
            if (fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
                return parseXlsx(input);
            }
        } catch (IOException exception) {
            throw new InvalidClassDataException("Não foi possível ler a planilha de turmas.");
        }

        throw new InvalidClassDataException("O arquivo deve ser CSV ou XLSX.");
    }

    private List<ClassImportRow> parseCsv(Reader reader) throws IOException {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                records.add(parseCsvLine(line));
            }
        }
        return rowsFromValues(records);
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    value.append(character);
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(character);
            }
        }
        if (quoted) {
            throw new InvalidClassDataException("CSV inválido: aspas não foram fechadas.");
        }
        values.add(value.toString());
        return values;
    }

    private List<ClassImportRow> parseXlsx(InputStream input) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(input)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new InvalidClassDataException("A planilha não contém abas.");
            }
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            List<List<String>> values = new ArrayList<>();
            for (Row row : sheet) {
                List<String> rowValues = new ArrayList<>();
                int cells = Math.max(row.getLastCellNum(), 0);
                for (int cell = 0; cell < cells; cell++) {
                    rowValues.add(formatter.formatCellValue(row.getCell(cell)));
                }
                values.add(rowValues);
            }
            return rowsFromValues(values);
        }
    }

    private List<ClassImportRow> rowsFromValues(List<List<String>> values) {
        if (values.isEmpty()) {
            throw new InvalidClassDataException("A planilha deve conter o cabeçalho obrigatório.");
        }
        Map<String, Integer> headers = headerIndexes(values.getFirst());
        List<ClassImportRow> rows = new ArrayList<>();
        for (int index = 1; index < values.size(); index++) {
            List<String> row = values.get(index);
            if (row.stream().allMatch(value -> value == null || value.isBlank())) {
                continue;
            }
            rows.add(new ClassImportRow(
                    index + 1,
                    valueAt(row, headers.get("course_code")),
                    valueAt(row, headers.get("number")),
                    valueAt(row, headers.get("shift"))
            ));
        }
        return rows;
    }

    private Map<String, Integer> headerIndexes(List<String> headerRow) {
        Map<String, Integer> indexes = new LinkedHashMap<>();
        for (int index = 0; index < headerRow.size(); index++) {
            String header = headerRow.get(index);
            if (header != null) {
                indexes.put(header.replace("\uFEFF", "").trim().toLowerCase(Locale.ROOT), index);
            }
        }
        if (!indexes.keySet().containsAll(REQUIRED_HEADERS)) {
            throw new InvalidClassDataException("Cabeçalho obrigatório: course_code, number, shift.");
        }
        return indexes;
    }

    private String valueAt(List<String> values, Integer index) {
        return index != null && index < values.size() ? values.get(index) : null;
    }
}
