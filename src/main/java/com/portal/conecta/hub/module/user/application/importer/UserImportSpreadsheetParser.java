package com.portal.conecta.hub.module.user.application.importer;

import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
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
public class UserImportSpreadsheetParser {

    private static final List<String> REQUIRED_HEADERS = List.of("name", "email");

    public List<UserImportRow> parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidUserDataException("A planilha de usuários é obrigatória.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new InvalidUserDataException("O arquivo deve ser CSV ou XLSX.");
        }

        try (InputStream input = file.getInputStream()) {
            if (fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
                return parseCsv(new InputStreamReader(input, StandardCharsets.UTF_8));
            }
            if (fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
                return parseXlsx(input);
            }
        } catch (IOException exception) {
            throw new InvalidUserDataException("Não foi possível ler a planilha de usuários.");
        }

        throw new InvalidUserDataException("O arquivo deve ser CSV ou XLSX.");
    }

    private List<UserImportRow> parseCsv(Reader reader) throws IOException {
        List<List<String>> records = new ArrayList<>();
        try (java.io.BufferedReader bufferedReader = new java.io.BufferedReader(reader)) {
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
            throw new InvalidUserDataException("CSV inválido: aspas não foram fechadas.");
        }
        values.add(value.toString());
        return values;
    }

    private List<UserImportRow> parseXlsx(InputStream input) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(input)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new InvalidUserDataException("A planilha não contém abas.");
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

    private List<UserImportRow> rowsFromValues(List<List<String>> values) {
        if (values.isEmpty()) {
            throw new InvalidUserDataException("A planilha deve conter o cabeçalho obrigatório.");
        }
        Map<String, Integer> headers = headerIndexes(values.getFirst());
        List<UserImportRow> rows = new ArrayList<>();
        for (int index = 1; index < values.size(); index++) {
            List<String> row = values.get(index);
            if (row.stream().allMatch(value -> value == null || value.isBlank())) {
                continue;
            }
            rows.add(new UserImportRow(
                    index + 1,
                    valueAt(row, headers.get("name")),
                    valueAt(row, headers.get("email")),
                    valueAt(row, headers.get("type_user"))
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
            throw new InvalidUserDataException("Cabeçalho obrigatório: name, email.");
        }
        return indexes;
    }

    private String valueAt(List<String> values, Integer index) {
        return index != null && index < values.size() ? values.get(index) : null;
    }
}
