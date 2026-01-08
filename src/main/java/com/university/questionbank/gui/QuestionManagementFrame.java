package com.university.questionbank.gui;

import com.university.questionbank.model.Question;
import com.university.questionbank.model.QuestionCategory;
import com.university.questionbank.model.QuestionDifficulty;
import com.university.questionbank.model.User;
import com.university.questionbank.service.QuestionService;
import com.university.questionbank.gui.UIStyle;
import com.university.questionbank.gui.UIComponentFactory;
import com.university.questionbank.util.DocxQuestionParser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.awt.Dialog;

public class QuestionManagementFrame extends JInternalFrame {
    private static final Logger logger = Logger.getLogger(QuestionManagementFrame.class.getName());
    private User currentUser;
    private QuestionService questionService;
    
    // 缓存以提高性能
    private Map<Integer, String> categoryNameCache = null;
    private Map<Integer, String> difficultyNameCache = null;
    private Map<Integer, String> userNameCache = null;
    
    // 界面组件
    private JTable questionTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton importButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    // 分页相关
    private int currentPage = 1;
    private int pageSize = 20;
    private JButton prevPageButton;
    private JButton nextPageButton;
    private JLabel pageInfoLabel;

    public QuestionManagementFrame(User currentUser) {
        super("题目管理", true, true, true, true);
        this.currentUser = currentUser;
        this.questionService = new QuestionService();
        initUI();
        loadQuestionData();
    }

    private void initUI() {
        setSize(850, 650);
        setLocation(150, 100);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
        
        // 创建表格模型
        String[] columnNames = {"题目ID", "题目内容", "类型", "分类", "难度", "创建者", "创建时间"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // 创建表格
        questionTable = UIComponentFactory.createTable(tableModel);
        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionTable.setRowHeight(25);
        
        // 设置表格列宽
        questionTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        questionTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        questionTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        questionTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        questionTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        questionTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        questionTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        
        // 设置自定义表格渲染器以优化性能
        questionTable.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        
        // 添加滚动面板
        JScrollPane scrollPane = UIComponentFactory.createScrollPane(questionTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 创建状态面板（用于显示加载进度）
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, UIStyle.PADDING_NORMAL, 0));
        
        statusLabel = UIComponentFactory.createLabel("就绪");
        statusLabel.setForeground(UIStyle.TEXT_SECONDARY);
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        statusPanel.add(progressBar, BorderLayout.CENTER);
        
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // 创建分页面板
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL));
        paginationPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        prevPageButton = UIComponentFactory.createSecondaryButton("上一页");
        prevPageButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadQuestionData();
            }
        });
        paginationPanel.add(prevPageButton);
        
        pageInfoLabel = UIComponentFactory.createLabel("第 1 页");
        paginationPanel.add(pageInfoLabel);
        
        nextPageButton = UIComponentFactory.createSecondaryButton("下一页");
        nextPageButton.addActionListener(e -> {
            currentPage++;
            loadQuestionData();
        });
        paginationPanel.add(nextPageButton);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        buttonPanel.add(paginationPanel, BorderLayout.NORTH);
        
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
        actionButtonPanel.setBackground(UIStyle.BACKGROUND_COLOR);

        // 导入题目：只有管理员和教师可以导入
        if (currentUser.getRole().getRoleId() <= 2) {
            importButton = UIComponentFactory.createPrimaryButton("导入题目");
            importButton.addActionListener(e -> showImportDialog());
            actionButtonPanel.add(importButton);
        }

        refreshButton = UIComponentFactory.createSecondaryButton("刷新");
        refreshButton.addActionListener(e -> loadQuestionData());
        actionButtonPanel.add(refreshButton);

        editButton = UIComponentFactory.createPrimaryButton("编辑");
        editButton.addActionListener(e -> editSelectedQuestion());
        actionButtonPanel.add(editButton);

        deleteButton = UIComponentFactory.createSecondaryButton("删除");
        deleteButton.addActionListener(e -> deleteSelectedQuestion());
        actionButtonPanel.add(deleteButton);
        
        buttonPanel.add(actionButtonPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    // 加载题目数据
    private void loadQuestionData() {
        loadQuestionData(currentPage, pageSize);
    }
    
    // 加载题目数据（分页）
    private void loadQuestionData(int page, int limit) {
        // 显示加载状态
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLabel.setText("正在加载题目数据...");
        refreshButton.setEnabled(false);
        prevPageButton.setEnabled(false);
        nextPageButton.setEnabled(false);
        
        // 计算偏移量
        int offset = (page - 1) * limit;
        
        // 使用SwingWorker异步加载数据
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
                        // 如果加载失败，使用空缓存
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
                        // 如果加载失败，使用空缓存
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
                        // 如果加载失败，使用空缓存
                        userNameCache = new HashMap<>();
                        logger.warning("Failed to load user cache: " + e.getMessage());
                    }
                }
                
                // 获取题目数据，支持分页
                return questionService.getAllQuestions(offset, limit);
            }
            
            @Override
            protected void done() {
                try {
                    List<Question> questions = get();
                    tableModel.setRowCount(0);
                    
                    // 批量收集数据，减少UI重绘次数
                    List<Object[]> rows = new ArrayList<>(questions.size());
                    for (Question question : questions) {
                        // 安全获取分类名称（category 对象可能为 null）
                        String categoryName = "";
                        if (question.getCategory() != null) {
                            categoryName = question.getCategory().getCategoryName();
                        } else if (question.getCategoryId() > 0) {
                            // 根据 category_id 从缓存查找分类名称
                            categoryName = categoryNameCache.get(question.getCategoryId());
                            if (categoryName == null) {
                                // 缓存中不存在，使用ID作为后备
                                categoryName = "分类ID:" + question.getCategoryId();
                            }
                        }

                        // 安全获取难度名称
                        String difficultyLevel = "";
                        if (question.getDifficulty() != null) {
                            difficultyLevel = question.getDifficulty().getDifficultyLevel();
                        } else if (question.getDifficultyId() > 0) {
                            // 根据 difficulty_id 从缓存查找难度名称
                            difficultyLevel = difficultyNameCache.get(question.getDifficultyId());
                            if (difficultyLevel == null) {
                                // 缓存中不存在，使用ID作为后备
                                difficultyLevel = "难度ID:" + question.getDifficultyId();
                            }
                        }

                        // 安全获取创建者名称
                        String creatorName = "";
                        if (question.getCreator() != null) {
                            creatorName = question.getCreator().getRealName();
                        } else if (question.getCreatorId() > 0) {
                            // 根据 creator_id 从缓存查找用户名称
                            creatorName = userNameCache.get(question.getCreatorId());
                            if (creatorName == null) {
                                // 缓存中不存在，使用ID作为后备
                                creatorName = "用户ID:" + question.getCreatorId();
                            }
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
                    
                    // 更新状态信息
                    statusLabel.setText("共加载 " + questions.size() + " 道题目");
                    pageInfoLabel.setText("第 " + page + " 页");
                    
                    // 更新分页按钮状态
                    updatePaginationButtons(page, questions.size(), limit);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(QuestionManagementFrame.this, 
                        "加载题目数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("加载失败");
                } finally {
                    // 恢复UI状态
                    progressBar.setVisible(false);
                    progressBar.setIndeterminate(false);
                    refreshButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    // 更新分页按钮状态
    private void updatePaginationButtons(int currentPage, int currentItems, int pageSize) {
        // 更新上一页按钮状态
        prevPageButton.setEnabled(currentPage > 1);
        
        // 更新下一页按钮状态
        // 如果当前页返回的项目数等于pageSize，则假设还有更多数据
        nextPageButton.setEnabled(currentItems >= pageSize);
    }

    // 编辑选中的题目
    private void editSelectedQuestion() {
        // 权限检查：学生不能编辑题目
        if (currentUser.getRole().getRoleId() > 2) {
            JOptionPane.showMessageDialog(this,
                    "您没有编辑题目的权限！\n只有管理员和教师可以编辑题目。",
                    "权限不足", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的题目", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int questionId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            Question question = questionService.getQuestionById(questionId);

            // 权限检查：
            // - 管理员可以编辑所有题目
            // - 教师只能编辑自己创建的题目
            if (currentUser.getRole().getRoleId() != 1 && question.getCreator().getUserId() != currentUser.getUserId()) {
                JOptionPane.showMessageDialog(this,
                        "您没有权限编辑此题目！\n教师只能编辑自己创建的题目。",
                        "权限不足", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 打开编辑窗口
            EditQuestionFrame editFrame = new EditQuestionFrame(question, currentUser);
            getDesktopPane().add(editFrame);
            editFrame.setVisible(true);

            // 监听编辑窗口关闭事件，刷新数据
            editFrame.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
                @Override
                public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
                    loadQuestionData();
                }
            });
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "获取题目数据失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 删除选中的题目
    private void deleteSelectedQuestion() {
        // 权限检查：学生不能删除题目
        if (currentUser.getRole().getRoleId() > 2) {
            JOptionPane.showMessageDialog(this,
                    "您没有删除题目的权限！\n只有管理员和教师可以删除题目。",
                    "权限不足", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的题目", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int questionId = (int) tableModel.getValueAt(selectedRow, 0);

        // 确认删除
        int result = JOptionPane.showConfirmDialog(this, "确定要删除选中的题目吗？", "确认删除", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                Question question = questionService.getQuestionById(questionId);

                // 权限检查：
                // - 管理员可以删除所有题目
                // - 教师只能删除自己创建的题目
                if (currentUser.getRole().getRoleId() != 1 && question.getCreator().getUserId() != currentUser.getUserId()) {
                    JOptionPane.showMessageDialog(this,
                            "您没有权限删除此题目！\n教师只能删除自己创建的题目。",
                            "权限不足", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 删除题目
                questionService.deleteQuestion(questionId);
                JOptionPane.showMessageDialog(this, "题目删除成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                loadQuestionData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "删除题目失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 显示导入对话框
    private void showImportDialog() {
        // 获取顶层窗口
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        ImportQuestionsDialog dialog = new ImportQuestionsDialog(parentWindow, currentUser);
        dialog.setVisible(true);
        // 监听对话框关闭，刷新数据
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                loadQuestionData();
            }
        });
    }

    // 导入题目对话框
    private class ImportQuestionsDialog extends JDialog {
        private JTextField filePathField;
        private JComboBox<QuestionCategory> categoryComboBox;
        private JComboBox<QuestionDifficulty> difficultyComboBox;
        private JButton browseButton;
        private JButton importButton;
        private JButton cancelButton;
        private JProgressBar progressBar;
        private JLabel statusLabel;
        private User currentUser;

        public ImportQuestionsDialog(Window parent, User currentUser) {
            super(parent, "导入题目", Dialog.ModalityType.APPLICATION_MODAL);
            this.currentUser = currentUser;
            initUI();
            setLocationRelativeTo(parent);
        }

        private void initUI() {
            setSize(550, 350);

            JPanel mainPanel = new JPanel(new GridBagLayout());
            mainPanel.setBackground(UIStyle.PANEL_COLOR);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // 文件选择
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 15, 20);
            gbc.weightx = 0.3;
            JLabel fileLabel = UIComponentFactory.createLabel("选择文件:");
            mainPanel.add(fileLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 0.7;
            JPanel filePanel = new JPanel(new BorderLayout());
            filePanel.setBackground(UIStyle.PANEL_COLOR);
            filePathField = UIComponentFactory.createTextField();
            filePathField.setPreferredSize(new Dimension(250, 32));
            filePathField.setEditable(false);
            filePanel.add(filePathField, BorderLayout.CENTER);
            browseButton = UIComponentFactory.createSecondaryButton("浏览");
            browseButton.setPreferredSize(new Dimension(80, 32));
            browseButton.addActionListener(e -> browseFile());
            filePanel.add(browseButton, BorderLayout.EAST);
            mainPanel.add(filePanel, gbc);

            // 分类选择
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 15, 20);
            gbc.weightx = 0.3;
            JLabel categoryLabel = UIComponentFactory.createLabel("题目分类*:");
            mainPanel.add(categoryLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 0.7;
            categoryComboBox = UIComponentFactory.createComboBox();
            categoryComboBox.setPreferredSize(new Dimension(250, 32));
            loadCategories();
            mainPanel.add(categoryComboBox, gbc);

            // 难度选择
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.insets = new Insets(0, 0, 15, 20);
            gbc.weightx = 0.3;
            JLabel difficultyLabel = UIComponentFactory.createLabel("题目难度*:");
            mainPanel.add(difficultyLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 0.7;
            difficultyComboBox = UIComponentFactory.createComboBox();
            difficultyComboBox.setPreferredSize(new Dimension(250, 32));
            loadDifficulties();
            mainPanel.add(difficultyComboBox, gbc);

            // 进度条和状态
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(0, 0, 15, 0);
            gbc.weightx = 1.0;
            statusLabel = UIComponentFactory.createLabel("");
            mainPanel.add(statusLabel, gbc);

            gbc.gridy = 4;
            gbc.insets = new Insets(0, 0, 25, 0);
            progressBar = new JProgressBar();
            progressBar.setStringPainted(true);
            progressBar.setVisible(false);
            mainPanel.add(progressBar, gbc);

            // 按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            buttonPanel.setBackground(UIStyle.PANEL_COLOR);

            importButton = UIComponentFactory.createPrimaryButton("导入");
            importButton.setPreferredSize(new Dimension(100, 32));
            importButton.addActionListener(e -> importQuestions());
            buttonPanel.add(importButton);

            cancelButton = UIComponentFactory.createSecondaryButton("取消");
            cancelButton.setPreferredSize(new Dimension(100, 32));
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(cancelButton);

            gbc.gridy = 5;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.anchor = GridBagConstraints.EAST;
            mainPanel.add(buttonPanel, gbc);

            add(mainPanel);
        }

        private void loadCategories() {
            try {
                List<QuestionCategory> categories = questionService.getAllCategories();
                categoryComboBox.removeAllItems();
                for (QuestionCategory category : categories) {
                    categoryComboBox.addItem(category);
                }
                // 设置自定义渲染器
                categoryComboBox.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        if (value instanceof QuestionCategory) {
                            setText(((QuestionCategory) value).getCategoryName());
                        }
                        return this;
                    }
                });
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "加载分类失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void loadDifficulties() {
            try {
                List<QuestionDifficulty> difficulties = questionService.getAllDifficulties();
                difficultyComboBox.removeAllItems();
                for (QuestionDifficulty difficulty : difficulties) {
                    difficultyComboBox.addItem(difficulty);
                }
                // 设置自定义渲染器
                difficultyComboBox.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        if (value instanceof QuestionDifficulty) {
                            setText(((QuestionDifficulty) value).getDifficultyLevel());
                        }
                        return this;
                    }
                });
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "加载难度失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void browseFile() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Word文档 (*.docx)", "docx"));
            fileChooser.setAcceptAllFileFilterUsed(false);

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        }

        private void importQuestions() {
            String filePath = filePathField.getText();
            if (filePath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请选择要导入的文件", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            QuestionCategory selectedCategory = (QuestionCategory) categoryComboBox.getSelectedItem();
            if (selectedCategory == null) {
                JOptionPane.showMessageDialog(this, "请选择题目分类", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            QuestionDifficulty selectedDifficulty = (QuestionDifficulty) difficultyComboBox.getSelectedItem();
            if (selectedDifficulty == null) {
                JOptionPane.showMessageDialog(this, "请选择题目难度", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 禁用按钮，显示进度条
            setComponentsEnabled(false);
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            statusLabel.setText("正在解析文件...");

            // 使用SwingWorker在后台执行导入
            new SwingWorker<Integer, Void>() {
                private List<Question> importedQuestions;
                private String errorMessage;

                @Override
                protected Integer doInBackground() throws Exception {
                    try {
                        // 解析题目
                        publish();
                        importedQuestions = DocxQuestionParser.parseAndPrepareQuestions(
                                filePath,
                                selectedCategory.getCategoryId(),
                                selectedDifficulty.getDifficultyId(),
                                currentUser.getUserId()
                        );

                        // 导入题目
                        statusLabel.setText("正在导入 " + importedQuestions.size() + " 道题目...");
                        publish();
                        questionService.importQuestions(importedQuestions);

                        return importedQuestions.size();
                    } catch (Exception e) {
                        errorMessage = e.getMessage();
                        e.printStackTrace();
                        return -1;
                    }
                }

                @Override
                protected void done() {
                    try {
                        int count = get();
                        if (count >= 0) {
                            JOptionPane.showMessageDialog(ImportQuestionsDialog.this,
                                    "成功导入 " + count + " 道题目！",
                                    "导入成功",
                                    JOptionPane.INFORMATION_MESSAGE);
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(ImportQuestionsDialog.this,
                                    "导入失败: " + errorMessage,
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(ImportQuestionsDialog.this,
                                "导入过程中出现错误: " + e.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        setComponentsEnabled(true);
                        progressBar.setVisible(false);
                        statusLabel.setText("");
                    }
                }
            }.execute();
        }

        private void setComponentsEnabled(boolean enabled) {
            filePathField.setEnabled(enabled);
            browseButton.setEnabled(enabled);
            categoryComboBox.setEnabled(enabled);
            difficultyComboBox.setEnabled(enabled);
            importButton.setEnabled(enabled);
            cancelButton.setEnabled(enabled);
        }
    }
    
    // 自定义表格单元格渲染器 - 用于性能优化
    private static class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // 对题目内容列（列索引1）启用自动换行
            if (column == 1 && value instanceof String) {
                // 设置多行文本支持
                ((JLabel) c).setVerticalAlignment(JLabel.TOP);
                // 优化性能：仅对可见行进行复杂渲染
                if (table.isVisible()) {
                    // 可以添加缓存机制，但当前简单实现
                    String text = (String) value;
                    if (text.length() > 100) {
                        // 截断显示长文本，提高渲染性能
                        ((JLabel) c).setText(text.substring(0, 100) + "...");
                        ((JLabel) c).setToolTipText(text);
                    } else {
                        ((JLabel) c).setText(text);
                        ((JLabel) c).setToolTipText(null);
                    }
                }
            }
            
            return c;
        }
    }
}