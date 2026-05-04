package com.quangph.crawlerapp.service;

import com.quangph.crawlerapp.dto.response.CrawledCompanyExcelRow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class ExcelExportService {

    public byte[] exportCompanyRows(List<CrawledCompanyExcelRow> rows) {
        SXSSFWorkbook workbook = createStreamingWorkbook();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = createCompanySheet(workbook);
            appendCompanyRows(sheet, rows, 1);
            writeWorkbook(workbook, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Xuất file Excel thất bại.", e);
        } finally {
            workbook.dispose();
            try {
                workbook.close();
            } catch (IOException ignored) {
                // no-op
            }
        }
    }

    public SXSSFWorkbook createStreamingWorkbook() {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        return workbook;
    }

    public Sheet createCompanySheet(SXSSFWorkbook workbook) {
        Sheet sheet = workbook.createSheet("Crawl Result");
        String[] headers = {
                "Tên CTY",
                "Tình trạng",
                "Country",
                "Địa chỉ",
                "Email",
                "WeChat",
                "Whatsapp",
                "Skype",
                "Phone",
                "Company size",
                "Note"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        applyColumnWidths(sheet);
        return sheet;
    }

    public int appendCompanyRows(Sheet sheet, List<CrawledCompanyExcelRow> rows, int startRowIndex) {
        int rowIndex = startRowIndex;
        for (CrawledCompanyExcelRow row : rows) {
            Row excelRow = sheet.createRow(rowIndex++);
            excelRow.createCell(0).setCellValue(defaultString(row.companyName()));
            excelRow.createCell(1).setCellValue(defaultString(row.status()));
            excelRow.createCell(2).setCellValue(defaultString(row.country()));
            excelRow.createCell(3).setCellValue(defaultString(row.address()));
            excelRow.createCell(4).setCellValue(defaultString(row.email()));
            excelRow.createCell(5).setCellValue(defaultString(row.weChat()));
            excelRow.createCell(6).setCellValue(defaultString(row.whatsapp()));
            excelRow.createCell(7).setCellValue(defaultString(row.skype()));
            excelRow.createCell(8).setCellValue(defaultString(row.phone()));
            excelRow.createCell(9).setCellValue(defaultString(row.companySize()));
            excelRow.createCell(10).setCellValue(defaultString(row.note()));
        }
        return rowIndex;
    }

    public void writeWorkbook(SXSSFWorkbook workbook, OutputStream outputStream) throws IOException {
        workbook.write(outputStream);
    }

    private void applyColumnWidths(Sheet sheet) {
        int[] columnWidths = {
                24 * 256,
                16 * 256,
                16 * 256,
                36 * 256,
                24 * 256,
                18 * 256,
                18 * 256,
                18 * 256,
                18 * 256,
                16 * 256,
                28 * 256
        };
        for (int i = 0; i < columnWidths.length; i++) {
            sheet.setColumnWidth(i, columnWidths[i]);
        }
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
