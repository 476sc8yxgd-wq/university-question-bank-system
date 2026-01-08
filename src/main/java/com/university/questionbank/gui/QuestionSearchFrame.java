package com.university.questionbank.gui;

import com.university.questionbank.model.*;
import com.university.questionbank.service.QuestionService;
import com.university.questionbank.gui.UIStyle;
import com.university.questionbank.gui.UIComponentFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class QuestionSearchFrame extends JInternalFrame {
    private static final Logger logger = Logger.getLogger(QuestionSearchFrame.class.getName());
    private User currentUser;
    private QuestionService questionService;
    
    // 缓存以提高性能
    private Map<Integer, String> categoryNameCache = null;
    private Map<Integer, String> difficultyNameCache = null;
    private Map<Integer, String> userNameCache = null;
    
    // 界面组件
    private JTextField keywordField;
    private JComboBox<QuestionCategory> categoryComboBox;
    private JComboBox<QuestionDifficulty> difficultyComboBox;
    private JComboBox<String> questionTypeComboBox;
    private JButton searchButton;
    private JButton clearButton;
    
    private JTable questionTable;
    private DefaultTableModel tableModel;
    private JButton viewDetailButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public QuestionSearchFrame(User currentUser) {
        super("题目检索", true, true, true, true);
        this.currentUser = currentUser;
        this.questionService = new QuestionService();
        initUI();
        loadCategoriesAndDifficulties();
    }

    private void initUI() {
        setSize(850, 600);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
        
        // 创建搜索面板
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(UIStyle.PANEL_COLOR);
        searchPanel.setBorder(BorderFactory.createTitledBorder("搜索条件"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 关键字搜索
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(UIComponentFactory.createLabel("关键字:"), gbc);
        
        gbc.gridx = 1;
        keywordField = UIComponentFactory.createTextField();
        searchPanel.add(keywordField, gbc);
        
        // 题目分类
        gbc.gridx = 0;
        gbc.gridy = 1;
        searchPanel.add(UIComponentFactory.createLabel("分类:"), gbc);
        
        gbc.gridx = 1;
        categoryComboBox = UIComponentFactory.createComboBox();
        // 设置分类下拉框的自定义渲染器，只显示分类名称
        categoryComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof QuestionCategory) {
                    QuestionCategory category = (QuestionCategory) value;
                    if (category.getCategoryId() == -1) {
                        setText("全部");
                    } else {
                        setText(category.getCategoryName());
                    }
                } else {
                    setText(value == null ? "" : value.toString());
                }
                return this;
            }
        });
        searchPanel.add(categoryComboBox, gbc);
        
        // 题目难度
        gbc.gridx = 2;
        gbc.gridy = 0;
        searchPanel.add(UIComponentFactory.createLabel("难度:"), gbc);
        
        gbc.gridx = 3;
        difficultyComboBox = UIComponentFactory.createComboBox();
        // 设置难度下拉框的自定义渲染器，只显示难度等级
        difficultyComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof QuestionDifficulty) {
                    QuestionDifficulty difficulty = (QuestionDifficulty) value;
                    if (difficulty.getDifficultyId() == -1) {
                        setText("全部");
                    } else {
                        setText(difficulty.getDifficultyLevel());
                    }
                } else {
                    setText(value == null ? "" : value.toString());
                }
                return this;
            }
        });
        searchPanel.add(difficultyComboBox, gbc);
        
        // 题目类型
        gbc.gridx = 2;
        gbc.gridy = 1;
        searchPanel.add(UIComponentFactory.createLabel("类型:"), gbc);
        
        gbc.gridx = 3;
        String[] questionTypes = {"", "单选题", "多选题", "判断题", "填空题", "简答题"};
        questionTypeComboBox = UIComponentFactory.createComboBox(questionTypes);
        searchPanel.add(questionTypeComboBox, gbc);
        
        // 搜索按钮
        gbc.gridx = 4;
        gbc.gridy = 0;
        searchButton = UIComponentFactory.createPrimaryButton("搜索");
        searchButton.addActionListener(new SearchButtonListener());
        searchPanel.add(searchButton, gbc);
        
        // 清空按钮
        gbc.gridx = 4;
        gbc.gridy = 1;
        clearButton = UIComponentFactory.createSecondaryButton("清空");
        clearButton.addActionListener(new ClearButtonListener());
        searchPanel.add(clearButton, gbc);
        
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        
        // 创建结果表格
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("搜索结果"));
        
        String[] columnNames = {"题目ID", "题目内容", "类型", "分类", "难度", "创建者", "创建时间"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        questionTable = UIComponentFactory.createTable(tableModel);
        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionTable.setRowHeight(25);
        
        // 设置表格列宽
        questionTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        questionTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        questionTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        questionTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        questionTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        questionTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        questionTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(questionTable);
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 结果操作按钮
        JPanel resultButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
        viewDetailButton = UIComponentFactory.createPrimaryButton("查看详情");
        viewDetailButton.addActionListener(e -> viewQuestionDetail());
        resultButtonPanel.add(viewDetailButton);

        resultPanel.add(resultButtonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(resultPanel, BorderLayout.CENTER);
        
        // 创建状态面板（用于显示加载进度）
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_NORMAL, 0, 0, 0));
        
        statusLabel = UIComponentFactory.createLabel("就绪");
        statusLabel.setForeground(UIStyle.TEXT_SECONDARY);
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        statusPanel.add(progressBar, BorderLayout.CENTER);
        
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    // 加载分类和难度数据
    private void loadCategoriesAndDifficulties() {
        try {
            // 添加空白选项
            categoryComboBox.addItem(new QuestionCategory(-1, "", ""));
            difficultyComboBox.addItem(new QuestionDifficulty(-1, "", ""));
            
            // 加载分类
            List<QuestionCategory> categories = questionService.getAllCategories();
            for (QuestionCategory category : categories) {
                categoryComboBox.addItem(category);
            }
            
            // 加载难度
            List<QuestionDifficulty> difficulties = questionService.getAllDifficulties();
            for (QuestionDifficulty difficulty : difficulties) {
                difficultyComboBox.addItem(difficulty);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "加载分类和难度数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 搜索按钮监听器
    private class SearchButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 获取搜索条件
            final String keyword = keywordField.getText();
            QuestionCategory selectedCategory = (QuestionCategory) categoryComboBox.getSelectedItem();
            final Integer categoryId = selectedCategory.getCategoryId() == -1 ? null : selectedCategory.getCategoryId();

            QuestionDifficulty selectedDifficulty = (QuestionDifficulty) difficultyComboBox.getSelectedItem();
            final Integer difficultyId = selectedDifficulty.getDifficultyId() == -1 ? null : selectedDifficulty.getDifficultyId();

            String questionType = (String) questionTypeComboBox.getSelectedItem();
            final String finalQuestionType = questionType.isEmpty() ? null : questionType;
            
            // 显示加载状态
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            statusLabel.setText("正在搜索题目...");
            searchButton.setEnabled(false);
            clearButton.setEnabled(false);
            
            // 使用SwingWorker异步执行搜索
            SwingWorker<List<Question>, Void> worker = new SwingWorker<List<Question>, Void>() {
                @Override
                protected List<Question> doInBackground() throws Exception {
                    // 在后台线程中初始化缓存（如果尚未初始化）
                    if (categoryNameCache == null) {
                        try {
                            List<QuestionCategory> categories = questionService.getAllCategories();
                            categoryNameCache = new HashMap<>();
                            for (QuestionCategory category : categories) {
                                categoryNameCache.put(category.getCategoryId(), category.getCategoryName());
                            }
                        } catch (Exception e) {
                            categoryNameCache = new HashMap<>();
                            logger.warning("Failed to load category cache: " + e.getMessage());
                        }
                    }
                    
                    if (difficultyNameCache == null) {
                        try {
                            List<QuestionDifficulty> difficulties = questionService.getAllDifficulties();
                            difficultyNameCache = new HashMap<>();
                            for (QuestionDifficulty difficulty : difficulties) {
                                difficultyNameCache.put(difficulty.getDifficultyId(), difficulty.getDifficultyLevel());
                            }
                        } catch (Exception e) {
                            difficultyNameCache = new HashMap<>();
                            logger.warning("Failed to load difficulty cache: " + e.getMessage());
                        }
                    }
                    
                    if (userNameCache == null) {
                        try {
                            List<User> users = questionService.getAllUsers();
                            userNameCache = new HashMap<>();
                            for (User user : users) {
                                userNameCache.put(user.getUserId(), user.getRealName());
                            }
                        } catch (Exception e) {
                            userNameCache = new HashMap<>();
                            logger.warning("Failed to load user cache: " + e.getMessage());
                        }
                    }
                    
                    // 执行搜索，默认限制100条记录
                    return questionService.searchQuestions(keyword, categoryId, difficultyId, finalQuestionType);
                }
                
                @Override
                protected void done() {
                    try {
                        List<Question> questions = get();
                        // 更新表格数据
                        updateSearchResults(questions);
                        
                        // 更新状态信息
                        statusLabel.setText("搜索完成，共找到 " + questions.size() + " 道题目");
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(QuestionSearchFrame.this, 
                            "搜索失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        statusLabel.setText("搜索失败");
                    } finally {
                        // 恢复UI状态
                        progressBar.setVisible(false);
                        progressBar.setIndeterminate(false);
                        searchButton.setEnabled(true);
                        clearButton.setEnabled(true);
                    }
                }
            };
            
            worker.execute();
        }
    }

    // 清空按钮监听器
    private class ClearButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            keywordField.setText("");
            categoryComboBox.setSelectedIndex(0);
            difficultyComboBox.setSelectedIndex(0);
            questionTypeComboBox.setSelectedIndex(0);
            tableModel.setRowCount(0);
        }
    }

    // 更新搜索结果表格
    private void updateSearchResults(List<Question> questions) {
        tableModel.setRowCount(0);
        
        // 批量收集数据，减少UI重绘次数
        List<Object[]> rows = new ArrayList<>(questions.size());
        
        // 性能优化：使用缓存，避免重复查询service方法
        for (Question question : questions) {
            // 优先使用已填充的对象，否则从缓存获取
            String categoryName = "";
            if (question.getCategory() != null) {
                categoryName = question.getCategory().getCategoryName();
            } else if (question.getCategoryId() > 0 && categoryNameCache != null) {
                // 从缓存查找分类名称
                categoryName = categoryNameCache.get(question.getCategoryId());
                if (categoryName == null) {
                    categoryName = "分类ID:" + question.getCategoryId();
                }
            } else if (question.getCategoryId() > 0) {
                categoryName = "分类ID:" + question.getCategoryId();
            }

            String difficultyLevel = "";
            if (question.getDifficulty() != null) {
                difficultyLevel = question.getDifficulty().getDifficultyLevel();
            } else if (question.getDifficultyId() > 0 && difficultyNameCache != null) {
                // 从缓存查找难度名称
                difficultyLevel = difficultyNameCache.get(question.getDifficultyId());
                if (difficultyLevel == null) {
                    difficultyLevel = "难度ID:" + question.getDifficultyId();
                }
            } else if (question.getDifficultyId() > 0) {
                difficultyLevel = "难度ID:" + question.getDifficultyId();
            }

            String creatorName = "";
            if (question.getCreator() != null) {
                creatorName = question.getCreator().getRealName();
            } else if (question.getCreatorId() > 0 && userNameCache != null) {
                // 从缓存查找用户名称
                creatorName = userNameCache.get(question.getCreatorId());
                if (creatorName == null) {
                    creatorName = "用户ID:" + question.getCreatorId();
                }
            } else if (question.getCreatorId() > 0) {
                creatorName = "用户ID:" + question.getCreatorId();
            }

            Object[] rowData = {
                    question.getQuestionId(),
                    question.getQuestionContent(),
                    question.getQuestionType(),
                    categoryName,
                    difficultyLevel,
                    creatorName,
                    question.getCreatedAt()
            };
            rows.add(rowData);
        }
        
        // 批量添加数据到表格，减少UI重绘次数
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
    }

    // 查看题目详情
    private void viewQuestionDetail() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要查看的题目", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int questionId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            Question question = questionService.getQuestionById(questionId);

            // 创建题目详情对话框 - 增加尺寸
            JDialog detailDialog = new JDialog((Frame) null, "题目详情", true);
            detailDialog.setSize(700, 600);  // 增加尺寸以显示更多内容
            detailDialog.setLocationRelativeTo(this);
            detailDialog.setMinimumSize(new Dimension(600, 500));  // 设置最小尺寸

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
            mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);

            // 题目内容 - 放在CENTER并设置权重
            JTextArea contentArea = new JTextArea(question.getQuestionContent());
            contentArea.setEditable(false);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setFont(UIStyle.NORMAL_FONT);
            contentArea.setBackground(UIStyle.PANEL_COLOR);
            JScrollPane contentScrollPane = new JScrollPane(contentArea);
            contentScrollPane.setBorder(BorderFactory.createTitledBorder("题目内容"));
            contentScrollPane.setPreferredSize(new Dimension(650, 150));  // 设置初始大小
            mainPanel.add(contentScrollPane, BorderLayout.NORTH);

            // 题目信息
            JPanel infoPanel = new JPanel(new GridBagLayout());
            infoPanel.setBackground(UIStyle.PANEL_COLOR);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            int row = 0;

            // 题目类型
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            infoPanel.add(UIComponentFactory.createLabel("题目类型:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            infoPanel.add(UIComponentFactory.createLabel(question.getQuestionType()), gbc);
            row++;

            // 安全获取分类名称
            String categoryName = "";
            if (question.getCategory() != null) {
                categoryName = question.getCategory().getCategoryName();
            } else if (question.getCategoryId() > 0) {
                // 从缓存查找分类名称
                if (categoryNameCache != null) {
                    categoryName = categoryNameCache.get(question.getCategoryId());
                    if (categoryName == null) {
                        categoryName = "分类ID:" + question.getCategoryId();
                    }
                } else {
                    categoryName = "分类ID:" + question.getCategoryId();
                }
            }
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            infoPanel.add(UIComponentFactory.createLabel("分类:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            infoPanel.add(UIComponentFactory.createLabel(categoryName), gbc);
            row++;

            // 安全获取难度名称
            String difficultyLevel = "";
            if (question.getDifficulty() != null) {
                difficultyLevel = question.getDifficulty().getDifficultyLevel();
            } else if (question.getDifficultyId() > 0) {
                // 从缓存查找难度名称
                if (difficultyNameCache != null) {
                    difficultyLevel = difficultyNameCache.get(question.getDifficultyId());
                    if (difficultyLevel == null) {
                        difficultyLevel = "难度ID:" + question.getDifficultyId();
                    }
                } else {
                    difficultyLevel = "难度ID:" + question.getDifficultyId();
                }
            }
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            infoPanel.add(UIComponentFactory.createLabel("难度:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            infoPanel.add(UIComponentFactory.createLabel(difficultyLevel), gbc);
            row++;

            // 安全获取创建者名称
            String creatorName = "";
            if (question.getCreator() != null) {
                creatorName = question.getCreator().getRealName();
            } else if (question.getCreatorId() > 0) {
                // 从缓存查找用户名称
                if (userNameCache != null) {
                    creatorName = userNameCache.get(question.getCreatorId());
                    if (creatorName == null) {
                        creatorName = "用户ID:" + question.getCreatorId();
                    }
                } else {
                    creatorName = "用户ID:" + question.getCreatorId();
                }
            }
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            infoPanel.add(UIComponentFactory.createLabel("创建者:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            infoPanel.add(UIComponentFactory.createLabel(creatorName), gbc);
            row++;

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            infoPanel.add(UIComponentFactory.createLabel("创建时间:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            infoPanel.add(UIComponentFactory.createLabel(question.getCreatedAt()), gbc);
            row++;

            // 选项（如果有）
            String questionType = question.getQuestionType();
            if (questionType.equals("单选题") || questionType.equals("多选题") || questionType.equals("判断题")) {
                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.weightx = 0;
                infoPanel.add(UIComponentFactory.createLabel("选项A:"), gbc);
                gbc.gridx = 1;
                gbc.weightx = 1;
                infoPanel.add(UIComponentFactory.createLabel(question.getOptionA()), gbc);
                row++;

                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.weightx = 0;
                infoPanel.add(UIComponentFactory.createLabel("选项B:"), gbc);
                gbc.gridx = 1;
                gbc.weightx = 1;
                infoPanel.add(UIComponentFactory.createLabel(question.getOptionB()), gbc);
                row++;

                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.weightx = 0;
                infoPanel.add(UIComponentFactory.createLabel("选项C:"), gbc);
                gbc.gridx = 1;
                gbc.weightx = 1;
                infoPanel.add(UIComponentFactory.createLabel(question.getOptionC()), gbc);
                row++;

                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.weightx = 0;
                infoPanel.add(UIComponentFactory.createLabel("选项D:"), gbc);
                gbc.gridx = 1;
                gbc.weightx = 1;
                infoPanel.add(UIComponentFactory.createLabel(question.getOptionD()), gbc);
                row++;
            }

            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            infoPanel.add(UIComponentFactory.createLabel("正确答案:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            infoPanel.add(UIComponentFactory.createLabel(question.getCorrectAnswer()), gbc);
            row++;

            // 解析
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            infoPanel.add(UIComponentFactory.createLabel("解析:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.BOTH;

            JTextArea explanationArea = new JTextArea(question.getExplanation());
            explanationArea.setEditable(false);
            explanationArea.setLineWrap(true);
            explanationArea.setWrapStyleWord(true);
            explanationArea.setRows(6);  // 增加行数
            explanationArea.setFont(UIStyle.NORMAL_FONT);
            explanationArea.setBackground(UIStyle.PANEL_COLOR);
            JScrollPane explanationScrollPane = new JScrollPane(explanationArea);
            explanationScrollPane.setPreferredSize(new Dimension(550, 120));  // 增加尺寸
            explanationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            infoPanel.add(explanationScrollPane, gbc);

            infoPanel.setBorder(BorderFactory.createTitledBorder("题目信息"));

            // 将infoPanel放在scrollPane中，确保可以滚动查看所有内容
            JScrollPane infoScrollPane = new JScrollPane(infoPanel);
            infoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            infoScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            mainPanel.add(infoScrollPane, BorderLayout.CENTER);
            
            // 关闭按钮
            JButton closeButton = UIComponentFactory.createPrimaryButton("关闭");
            closeButton.addActionListener(ev -> detailDialog.dispose());
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
            buttonPanel.setBackground(UIStyle.BACKGROUND_COLOR);
            buttonPanel.add(closeButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            detailDialog.add(mainPanel);
            detailDialog.setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载题目详情失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}