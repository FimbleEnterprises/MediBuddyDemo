package com.fimbleenterprises.medimileage.objects_and_containers;

import android.content.Context;
import android.os.Environment;

import com.fimbleenterprises.medimileage.Helpers;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ExcelSpreadsheet {

    public File file;
    public File directory;
    public WritableWorkbook workbook;


    public ExcelSpreadsheet(String filename) throws IOException {
        this.directory = Helpers.Files.ExcelTempFiles.getDirectory();
        this.file = new File(directory, filename);

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale(Locale.US.getLanguage(), Locale.US.getCountry()));

        this.workbook = Workbook.createWorkbook(file, wbSettings);
    }

    public void createSheet(String sheetName, int position) {
        try {
            WritableSheet sheet = this.workbook.createSheet(sheetName, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WritableSheet getSheet(int pos) {
        return this.workbook.getSheet(pos);
    }

    public void addCell(int sheet, int column, int row, String value) {
        try {
            WritableSheet sheet1 = this.workbook.getSheet(sheet);
            sheet1.addCell(new Label(column, row, value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCell(int sheet, int column, int row, String value, WritableCellFormat format) {
        try {
            WritableSheet sheet1 = this.workbook.getSheet(sheet);
            sheet1.addCell(new Label(column, row, value, format));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            this.workbook.write();
            this.workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void share(Context context) {
        Helpers.Files.shareFile(context, this.file, "Share mileage stats");
    }

    public static void test() {
        File sd = Helpers.Files.ExcelTempFiles.getDirectory();
        String csvFile = "yourFile.xls";

        File directory = new File(sd.getAbsolutePath());

        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        try {

            //file path
            File file = new File(directory, csvFile);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale(Locale.US.getLanguage(), Locale.US.getCountry()));
            WritableWorkbook workbook;
            workbook = Workbook.createWorkbook(file, wbSettings);

            //Excel sheetA first sheetA
            WritableSheet sheetA = workbook.createSheet("sheet A", 0);

            // column and row titles
            sheetA.addCell(new Label(0, 0, "sheet A 1"));
            sheetA.addCell(new Label(1, 0, "sheet A 2"));
            sheetA.addCell(new Label(0, 1, "sheet A 3"));
            sheetA.addCell(new Label(1, 1, "sheet A 4"));

            //Excel sheetB represents second sheet
            WritableSheet sheetB = workbook.createSheet("sheet B", 1);

            // column and row titles
            sheetB.addCell(new Label(0, 0, "sheet B 1"));
            sheetB.addCell(new Label(1, 0, "sheet B 2"));
            sheetB.addCell(new Label(0, 1, "sheet B 3"));
            sheetB.addCell(new Label(1, 1, "sheet B 4"));

            // close workbook
            workbook.write();
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}