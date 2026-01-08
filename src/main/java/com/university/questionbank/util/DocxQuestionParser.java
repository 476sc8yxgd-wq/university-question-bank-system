package com.university.questionbank.util;

import org.apache.poi.xwpf.usermodel.*;
import com.university.questionbank.model.Question;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * DOCX题目解析器
 * 从Word文档中解析题目，支持以下格式：
 * 1. 题目：以数字开头（如 "1."、"1、" "第1题"等）
 * 2. 类型：选择题、填空题、简答题等
 * 3. 选项：A. B. C. D. 等开头
 * 4. 答案：标注为"答案："、"正确答案："等
 * 5. 解析：标注为"解析："、"解释："等
 */
public class DocxQuestionParser {

    /**
     * 从docx文件中解析题目
     * @param filePath docx文件路径
     * @return 解析出的题目列表
     * @throws IOException 文件读取异常
     */
    public static List<Question> parseQuestions(String filePath) throws IOException {
        List<Question> questions = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            // 获取所有段落
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            StringBuilder contentBuilder = new StringBuilder();
            
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText().trim();
                if (!text.isEmpty()) {
                    contentBuilder.append(text).append("\n");
                }
            }
            
            String content = contentBuilder.toString();
            questions = parseQuestionText(content);
        }
        
        return questions;
    }

    /**
     * 解析题目文本
     * @param text 题目文本
     * @return 题目列表
     */
    private static List<Question> parseQuestionText(String text) {
        List<Question> questions = new ArrayList<>();
        
        // 使用正则表达式分割题目（以数字+点或数字+空格开头）
        Pattern questionPattern = Pattern.compile("(?m)^(\\d+\\s*[.、．]|第\\d+题).+$", Pattern.MULTILINE);
        Matcher questionMatcher = questionPattern.matcher(text);
        
        List<Integer> startIndices = new ArrayList<>();
        while (questionMatcher.find()) {
            startIndices.add(questionMatcher.start());
        }
        
        // 提取每个题目
        for (int i = 0; i < startIndices.size(); i++) {
            int startIndex = startIndices.get(i);
            int endIndex = (i < startIndices.size() - 1) ? startIndices.get(i + 1) : text.length();
            
            String questionText = text.substring(startIndex, endIndex).trim();
            Question question = parseSingleQuestion(questionText);
            
            if (question != null && question.getQuestionContent() != null && !question.getQuestionContent().isEmpty()) {
                questions.add(question);
            }
        }
        
        return questions;
    }

    /**
     * 解析单个题目
     * @param text 题目文本
     * @return Question对象
     */
    private static Question parseSingleQuestion(String text) {
        Question question = new Question();
        
        // 提取题目内容（第一行或第一个完整句子）
        String questionContent = extractQuestionContent(text);
        question.setQuestionContent(questionContent);
        
        // 判断题目类型
        String questionType = detectQuestionType(text);
        question.setQuestionType(questionType);
        
        // 提取选项
        Map<String, String> options = extractOptions(text);
        question.setOptionA(options.getOrDefault("A", ""));
        question.setOptionB(options.getOrDefault("B", ""));
        question.setOptionC(options.getOrDefault("C", ""));
        question.setOptionD(options.getOrDefault("D", ""));
        
        // 提取答案
        String answer = extractAnswer(text);
        question.setCorrectAnswer(answer);
        
        // 提取解析
        String explanation = extractExplanation(text);
        question.setExplanation(explanation);
        
        return question;
    }

    /**
     * 提取题目内容
     */
    private static String extractQuestionContent(String text) {
        // 移除题号
        String content = text.replaceFirst("^(\\d+\\s*[.、．]|第\\d+题)\\s*", "");
        
        // 提取题目主体（到选项或答案之前）
        int optionIndex = findFirstOptionIndex(content);
        int answerIndex = findAnswerIndex(content);
        int explanationIndex = findExplanationIndex(content);
        
        int endIndex = content.length();
        if (optionIndex != -1) {
            endIndex = Math.min(endIndex, optionIndex);
        }
        if (answerIndex != -1) {
            endIndex = Math.min(endIndex, answerIndex);
        }
        if (explanationIndex != -1) {
            endIndex = Math.min(endIndex, explanationIndex);
        }
        
        return content.substring(0, endIndex).trim();
    }

    /**
     * 查找第一个选项的位置
     */
    private static int findFirstOptionIndex(String text) {
        Pattern pattern = Pattern.compile("(?m)^[A-D][.、．]", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.start() : -1;
    }

    /**
     * 查找答案的位置
     */
    private static int findAnswerIndex(String text) {
        Pattern pattern = Pattern.compile("(?m)^(答案|正确答案|标准答案)[:：]", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.start() : -1;
    }

    /**
     * 查找解析的位置
     */
    private static int findExplanationIndex(String text) {
        Pattern pattern = Pattern.compile("(?m)^(解析|解释|说明)[:：]", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.start() : -1;
    }

    /**
     * 检测题目类型
     */
    private static String detectQuestionType(String text) {
        // 检查是否有选项
        Pattern optionPattern = Pattern.compile("(?m)^[A-D][.、．]", Pattern.MULTILINE);
        if (optionPattern.matcher(text).find()) {
            return "选择题";
        }
        
        // 检查题目内容中是否有类型标注
        Pattern typePattern = Pattern.compile("(?i)\\((单选|多选|选择|填空|简答|论述)题\\)");
        Matcher typeMatcher = typePattern.matcher(text);
        if (typeMatcher.find()) {
            return typeMatcher.group(1) + "题";
        }
        
        return "简答题"; // 默认为简答题
    }

    /**
     * 提取选项
     */
    private static Map<String, String> extractOptions(String text) {
        Map<String, String> options = new HashMap<>();
        
        Pattern pattern = Pattern.compile("(?m)^([A-D])[.、．]\\s*(.+?)(?=\\n[A-D][.、．]|$)", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            String optionKey = matcher.group(1);
            String optionValue = matcher.group(2).trim();
            
            // 移除可能的答案标注
            int answerIndex = findAnswerIndex(optionValue);
            if (answerIndex != -1) {
                optionValue = optionValue.substring(0, answerIndex).trim();
            }
            
            options.put(optionKey, optionValue);
        }
        
        return options;
    }

    /**
     * 提取答案
     */
    private static String extractAnswer(String text) {
        Pattern pattern = Pattern.compile("(?m)^(答案|正确答案|标准答案)[:：]\\s*(.+?)(?=\\n解析[:：]|\\n解释[:：]|$)", 
                                         Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group(2).trim();
        }
        
        return "";
    }

    /**
     * 提取解析
     */
    private static String extractExplanation(String text) {
        Pattern pattern = Pattern.compile("(?m)^(解析|解释|说明)[:：]\\s*(.+?)$", 
                                         Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group(2).trim();
        }
        
        return "";
    }

    /**
     * 批量导入题目（带分类和难度）
     * @param filePath 文件路径
     * @param categoryId 分类ID
     * @param difficultyId 难度ID
     * @param creatorId 创建者ID
     * @return 题目列表
     * @throws IOException 文件读取异常
     */
    public static List<Question> parseAndPrepareQuestions(String filePath, int categoryId, int difficultyId, int creatorId) throws IOException {
        List<Question> questions = parseQuestions(filePath);
        
        for (Question question : questions) {
            question.setCategoryId(categoryId);
            question.setDifficultyId(difficultyId);
            question.setCreatorId(creatorId);
        }
        
        return questions;
    }
}
