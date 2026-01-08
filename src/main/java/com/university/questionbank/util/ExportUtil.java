package com.university.questionbank.util;

import com.university.questionbank.model.Question;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExportUtil {
    
    // 导出为Word文档
    public static void exportToWord(List<Question> questions, String filePath) throws IOException {
        XWPFDocument document = new XWPFDocument();
        
        // 设置标题
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText("题目导出");
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        
        // 设置正文样式
        XWPFParagraph paragraph;
        XWPFRun run;
        
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            
            // 题目编号和内容
            paragraph = document.createParagraph();
            run = paragraph.createRun();
            run.setText((i + 1) + ". " + question.getQuestionContent());
            run.setFontSize(12);
            
            // 题目类型、分类和难度
            paragraph = document.createParagraph();
            run = paragraph.createRun();
            run.setText("类型：" + question.getQuestionType() + " | 分类：" + question.getCategory().getCategoryName() + " | 难度：" + question.getDifficulty().getDifficultyLevel());
            run.setFontSize(10);
            run.setColor("757575");
            
            // 选项（如果有）
            String questionType = question.getQuestionType();
            if (questionType.equals("单选题") || questionType.equals("多选题") || questionType.equals("判断题")) {
                paragraph = document.createParagraph();
                run = paragraph.createRun();
                run.setText("A. " + question.getOptionA());
                run.setFontSize(12);
                
                paragraph = document.createParagraph();
                run = paragraph.createRun();
                run.setText("B. " + question.getOptionB());
                run.setFontSize(12);
                
                paragraph = document.createParagraph();
                run = paragraph.createRun();
                run.setText("C. " + question.getOptionC());
                run.setFontSize(12);
                
                paragraph = document.createParagraph();
                run = paragraph.createRun();
                run.setText("D. " + question.getOptionD());
                run.setFontSize(12);
            }
            
            // 正确答案
            paragraph = document.createParagraph();
            run = paragraph.createRun();
            run.setText("正确答案：" + question.getCorrectAnswer());
            run.setFontSize(12);
            run.setBold(true);
            
            // 解析
            if (question.getExplanation() != null && !question.getExplanation().isEmpty()) {
                paragraph = document.createParagraph();
                run = paragraph.createRun();
                run.setText("解析：" + question.getExplanation());
                run.setFontSize(12);
            }
            
            // 添加分隔线
            paragraph = document.createParagraph();
            run = paragraph.createRun();
            run.setText("----------------------------------------");
            run.setFontSize(10);
        }
        
        // 保存文档
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            document.write(out);
        } finally {
            document.close();
        }
    }
    
    // 导出为Excel文档
    public static void exportToExcel(List<Question> questions, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("题目");
        
        // 设置标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"编号", "题目内容", "类型", "分类", "难度", "选项A", "选项B", "选项C", "选项D", "正确答案", "解析"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            
            // 设置标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据行
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            Row dataRow = sheet.createRow(i + 1);
            
            dataRow.createCell(0).setCellValue(i + 1);
            dataRow.createCell(1).setCellValue(question.getQuestionContent());
            dataRow.createCell(2).setCellValue(question.getQuestionType());
            dataRow.createCell(3).setCellValue(question.getCategory().getCategoryName());
            dataRow.createCell(4).setCellValue(question.getDifficulty().getDifficultyLevel());
            
            String questionType = question.getQuestionType();
            if (questionType.equals("单选题") || questionType.equals("多选题") || questionType.equals("判断题")) {
                dataRow.createCell(5).setCellValue(question.getOptionA());
                dataRow.createCell(6).setCellValue(question.getOptionB());
                dataRow.createCell(7).setCellValue(question.getOptionC());
                dataRow.createCell(8).setCellValue(question.getOptionD());
            }
            
            dataRow.createCell(9).setCellValue(question.getCorrectAnswer());
            dataRow.createCell(10).setCellValue(question.getExplanation());
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // 保存文档
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            workbook.write(out);
        } finally {
            workbook.close();
        }
    }
    
    // 导出为TXT文件
    public static void exportToTxt(List<Question> questions, String filePath) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            writer.write("题目导出\n");
            writer.write("============================\n\n");
            
            for (int i = 0; i < questions.size(); i++) {
                Question question = questions.get(i);
                
                writer.write((i + 1) + ". " + question.getQuestionContent() + "\n");
                writer.write("类型：" + question.getQuestionType() + " | 分类：" + question.getCategory().getCategoryName() + " | 难度：" + question.getDifficulty().getDifficultyLevel() + "\n");
                
                String questionType = question.getQuestionType();
                if (questionType.equals("单选题") || questionType.equals("多选题") || questionType.equals("判断题")) {
                    writer.write("A. " + question.getOptionA() + "\n");
                    writer.write("B. " + question.getOptionB() + "\n");
                    writer.write("C. " + question.getOptionC() + "\n");
                    writer.write("D. " + question.getOptionD() + "\n");
                }
                
                writer.write("正确答案：" + question.getCorrectAnswer() + "\n");
                
                if (question.getExplanation() != null && !question.getExplanation().isEmpty()) {
                    writer.write("解析：" + question.getExplanation() + "\n");
                }
                
                writer.write("----------------------------------------\n\n");
            }
        }
    }
}