package com.university.questionbank.gui;

import com.university.questionbank.model.User;
import com.university.questionbank.model.QuestionCategory;
import com.university.questionbank.service.QuestionService;
import com.university.questionbank.gui.UIStyle;
import com.university.questionbank.gui.UIComponentFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class CategoryManagementFrame extends JInternalFrame {
    private User currentUser;
    private QuestionService questionService;

    // 界面组件
    private JTable categoryTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;

    public CategoryManagementFrame(User currentUser) {
        super("分类管理", true, true, true, true);
        this.currentUser = currentUser;
        this.questionService = new QuestionService();
        initUI();
        loadCategoryData();
    }

    private void initUI() {
        setSize(700, 500);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));

        // 创建表格
        String[] columnNames = {"分类ID", "分类名称", "描述"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        categoryTable = UIComponentFactory.createTable(tableModel);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 设置表格列宽
        categoryTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        categoryTable.getColumnModel().getColumn(2).setPreferredWidth(300);

        JScrollPane scrollPane = new JScrollPane(categoryTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
        buttonPanel.setBackground(UIStyle.BACKGROUND_COLOR);

        addButton = UIComponentFactory.createPrimaryButton("添加分类");
        addButton.addActionListener(e -> addCategory());
        buttonPanel.add(addButton);

        editButton = UIComponentFactory.createSecondaryButton("编辑分类");
        editButton.addActionListener(e -> editCategory());
        buttonPanel.add(editButton);

        deleteButton = UIComponentFactory.createSecondaryButton("删除分类");
        deleteButton.addActionListener(e -> deleteCategory());
        buttonPanel.add(deleteButton);

        refreshButton = UIComponentFactory.createSecondaryButton("刷新");
        refreshButton.addActionListener(e -> loadCategoryData());
        buttonPanel.add(refreshButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // 加载分类数据
    private void loadCategoryData() {
        try {
            tableModel.setRowCount(0);
            List<QuestionCategory> categories = questionService.getAllCategories();
            for (QuestionCategory category : categories) {
                Object[] rowData = {
                    category.getCategoryId(),
                    category.getCategoryName(),
                    category.getDescription()
                };
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "加载分类数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 添加分类
    private void addCategory() {
        JDialog addDialog = new JDialog((Frame) null, "添加分类", true);
        addDialog.setSize(400, 250);
        addDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 分类名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(UIComponentFactory.createLabel("分类名称:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField categoryNameField = UIComponentFactory.createTextField();
        mainPanel.add(categoryNameField, gbc);

        // 描述
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        mainPanel.add(UIComponentFactory.createLabel("描述:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextArea descriptionArea = UIComponentFactory.createTextArea(3);
        mainPanel.add(descriptionArea, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIStyle.BACKGROUND_COLOR);

        JButton saveButton = UIComponentFactory.createPrimaryButton("保存");
        saveButton.addActionListener(e -> {
            String categoryName = categoryNameField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (categoryName.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "请输入分类名称", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                QuestionCategory category = new QuestionCategory(0, categoryName, description);
                questionService.addCategory(category);

                // 验证ID是否正确设置
                if (category.getCategoryId() > 0) {
                    JOptionPane.showMessageDialog(addDialog, "分类添加成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                    addDialog.dispose();
                    loadCategoryData();
                } else {
                    // 如果ID没有正确设置，尝试通过名称重新获取
                    QuestionCategory savedCategory = questionService.getCategoryByName(categoryName);
                    if (savedCategory != null && savedCategory.getCategoryId() > 0) {
                        JOptionPane.showMessageDialog(addDialog, "分类添加成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                        addDialog.dispose();
                        loadCategoryData();
                    } else {
                        JOptionPane.showMessageDialog(addDialog, "分类添加失败：ID未正确设置", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(addDialog, "添加分类失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);

        JButton cancelButton = UIComponentFactory.createSecondaryButton("取消");
        cancelButton.addActionListener(e -> addDialog.dispose());
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(buttonPanel, gbc);

        addDialog.add(mainPanel);
        addDialog.setVisible(true);
    }

    // 编辑分类
    private void editCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的分类", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int categoryId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            QuestionCategory category = questionService.getCategoryById(categoryId);

            JDialog editDialog = new JDialog((Frame) null, "编辑分类", true);
            editDialog.setSize(400, 250);
            editDialog.setLocationRelativeTo(this);

            JPanel mainPanel = new JPanel(new GridBagLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL, UIStyle.PADDING_NORMAL));
            mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL, UIStyle.PADDING_SMALL);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // 分类名称
            gbc.gridx = 0;
            gbc.gridy = 0;
            mainPanel.add(UIComponentFactory.createLabel("分类名称:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            JTextField categoryNameField = UIComponentFactory.createTextField();
            categoryNameField.setText(category.getCategoryName());
            mainPanel.add(categoryNameField, gbc);

            // 描述
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0;
            mainPanel.add(UIComponentFactory.createLabel("描述:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            JTextArea descriptionArea = UIComponentFactory.createTextArea(3);
            descriptionArea.setText(category.getDescription());
            mainPanel.add(descriptionArea, gbc);

            // 按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(UIStyle.BACKGROUND_COLOR);

            JButton saveButton = UIComponentFactory.createPrimaryButton("保存");
            saveButton.addActionListener(e -> {
                String categoryName = categoryNameField.getText().trim();
                String description = descriptionArea.getText().trim();

                if (categoryName.isEmpty()) {
                    JOptionPane.showMessageDialog(editDialog, "请输入分类名称", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    category.setCategoryName(categoryName);
                    category.setDescription(description);
                    questionService.updateCategory(category);
                    JOptionPane.showMessageDialog(editDialog, "分类更新成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                    editDialog.dispose();
                    loadCategoryData();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(editDialog, "更新分类失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            buttonPanel.add(saveButton);

            JButton cancelButton = UIComponentFactory.createSecondaryButton("取消");
            cancelButton.addActionListener(e -> editDialog.dispose());
            buttonPanel.add(cancelButton);

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            mainPanel.add(buttonPanel, gbc);

            editDialog.add(mainPanel);
            editDialog.setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "获取分类数据失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 删除分类
    private void deleteCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的分类", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int categoryId = (int) tableModel.getValueAt(selectedRow, 0);
        String categoryName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要删除分类 \"" + categoryName + "\" 吗？\n注意：如果该分类下有题目，将无法删除。",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                questionService.deleteCategory(categoryId);
                JOptionPane.showMessageDialog(this, "分类删除成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                loadCategoryData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "删除分类失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
