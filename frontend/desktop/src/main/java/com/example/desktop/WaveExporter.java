package com.example.desktop;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Выгрузка заявлений по волне в файл. Форматы: CSV и XLSX.
 */
public final class WaveExporter {

    private static final String[] HEADERS = {
            "ID", "Фамилия", "Имя", "Отчество", "Дата рождения", "Телефон",
            "Группа", "Институт", "Адрес регистрации", "ИНН", "Банковский счёт",
            "Основание помощи", "Статус", "Дата подачи", "Комментарий"
    };

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private WaveExporter() {
    }

    public static void exportCsv(List<Application> applications, java.io.File file) throws IOException {
        try (Writer out = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            // BOM, чтобы Excel корректно открывал кириллицу
            out.write('\ufeff');
            out.write(String.join(";", HEADERS));
            out.write("\n");
            for (Application a : applications) {
                out.write(String.join(";", escape(a.getId()), escape(a.getLastName()),
                        escape(a.getFirstName()), escape(a.getMiddleName()),
                        escape(a.getBirthDay()), escape(a.getPhone()),
                        escape(a.getGroup()), escape(a.getInstitute()),
                        escape(a.getRegistrationAddress()), escape(a.getInn()),
                        escape(a.getBankAccount()), escape(a.getReason().getDescription()),
                        escape(a.getStatus().badge), escape(a.getCreatedAt()),
                        escape(a.getComment())));
                out.write("\n");
            }
        }
    }

    public static void exportXlsx(List<Application> applications, java.io.File file) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Заявления");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(HEADERS[i]);
                sheet.setColumnWidth(i, 18 * 256);
            }

            int rowNumber = 1;
            for (Application a : applications) {
                Row row = sheet.createRow(rowNumber++);
                Object[] values = {
                        a.getId(), a.getLastName(), a.getFirstName(), a.getMiddleName(),
                        a.getBirthDay(), a.getPhone(), a.getGroup(), a.getInstitute(),
                        a.getRegistrationAddress(), a.getInn(), a.getBankAccount(),
                        a.getReason().getDescription(), a.getStatus().badge,
                        a.getCreatedAt(), a.getComment()
                };
                for (int i = 0; i < values.length; i++) {
                    Cell cell = row.createCell(i);
                    Object v = values[i];
                    if (v instanceof Number n) {
                        cell.setCellValue(n.doubleValue());
                    } else {
                        cell.setCellValue(v == null ? "" : v.toString());
                    }
                }
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }
        }
    }

    private static String escape(Object value) {
        if (value == null) return "";
        if (value instanceof LocalDate d) return DATE.format(d);
        if (value instanceof java.time.OffsetDateTime t) return DATE_TIME.format(t);
        String s = value.toString();
        if (s.contains(";") || s.contains("\"") || s.contains("\n")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }
}
