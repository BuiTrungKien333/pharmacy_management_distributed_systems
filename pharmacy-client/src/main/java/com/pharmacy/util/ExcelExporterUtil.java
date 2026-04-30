package com.pharmacy.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Slf4j
public class ExcelExporterUtil {

	public static void exportDataToExcel(String filePath, String sheetName, String[] headers, List<Object[]> data) {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet(sheetName);

		try {
			DataFormat format = workbook.createDataFormat();
			
			CellStyle doubleStyle = workbook.createCellStyle();
			doubleStyle.setDataFormat(format.getFormat("#,##0"));

			CellStyle dateStyle = workbook.createCellStyle();
			dateStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));

			CellStyle dateTimeStyle = workbook.createCellStyle();
			dateTimeStyle.setDataFormat(format.getFormat("dd/MM/yyyy HH:mm:ss"));

			Row headerRow = sheet.createRow(0);
			CellStyle headerStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setBold(true);
			headerStyle.setFont(font);

			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(headerStyle);
			}

			int rowNum = 1;
			for (Object[] rowData : data) {
				Row row = sheet.createRow(rowNum++);
				int colNum = 0;

				for (Object field : rowData) {
					Cell cell = row.createCell(colNum++);

					if (field == null) {
						cell.setCellValue("");
					} else if (field instanceof Double) {
						cell.setCellValue((Double) field);
						cell.setCellStyle(doubleStyle);
					} else if (field instanceof Integer) {
						cell.setCellValue((Integer) field);
					} else if (field instanceof LocalDate) {
						cell.setCellValue((LocalDate) field);
						cell.setCellStyle(dateStyle);
					} else if (field instanceof LocalDateTime) {
						cell.setCellValue((LocalDateTime) field);
						cell.setCellStyle(dateTimeStyle);
					} else {
						cell.setCellValue(field.toString());
					}
				}
			}

			for (int i = 0; i < headers.length; i++) {
				sheet.autoSizeColumn(i);
			}

			try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
				workbook.write(fileOut);
			}

		} catch (IOException e) {
			log.error("Failed to export data to Excel file at path: {}", filePath, e);
		} finally {
			try {
				workbook.close();
			} catch (IOException e) {
				log.error("Failed to close Excel workbook resource", e);
			}
		}
	}
}