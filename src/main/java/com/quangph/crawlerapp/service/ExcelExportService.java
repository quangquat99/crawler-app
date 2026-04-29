package com.quangph.crawlerapp.service;

import com.quangph.crawlerapp.dto.response.CrawledCompanyExcelRow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    public byte[] exportCompanyRows(List<CrawledCompanyExcelRow> rows) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

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

            int rowIndex = 1;
            for (CrawledCompanyExcelRow row : rows) {
                Row excelRow = sheet.createRow(rowIndex++);

                excelRow.createCell(0).setCellValue(row.companyName());
                excelRow.createCell(1).setCellValue(row.status());
                excelRow.createCell(2).setCellValue(row.country());
                excelRow.createCell(3).setCellValue(row.address());
                excelRow.createCell(4).setCellValue(row.email());
                excelRow.createCell(5).setCellValue(row.weChat());
                excelRow.createCell(6).setCellValue(row.whatsapp());
                excelRow.createCell(7).setCellValue(row.skype());
                excelRow.createCell(8).setCellValue(row.phone());
                excelRow.createCell(9).setCellValue(row.companySize());
                excelRow.createCell(10).setCellValue(row.note());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Export Excel failed", e);
        }
    }

}
