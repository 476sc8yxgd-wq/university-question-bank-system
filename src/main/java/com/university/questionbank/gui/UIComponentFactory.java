package com.university.questionbank.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * UI组件工厂类，用于创建具有统一样式的UI组件
 * 采用现代Material Design设计风格
 */
public class UIComponentFactory {

    // ==================== 按钮组件 ====================

    /**
     * 创建主按钮（使用主色调）- 现代设计
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UIStyle.BUTTON_FONT);
        button.setBackground(UIStyle.PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(6, 14, 6, 14),
            new LineBorder(UIStyle.PRIMARY_COLOR, 1)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, UIStyle.BUTTON_HEIGHT));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(UIStyle.PRIMARY_DARK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(UIStyle.PRIMARY_COLOR);
            }
        });

        return button;
    }

    /**
     * 创建次要按钮（使用辅助色）
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UIStyle.BUTTON_FONT);
        button.setBackground(UIStyle.SECONDARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(6, 14, 6, 14),
            new LineBorder(UIStyle.SECONDARY_COLOR, 1)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, UIStyle.BUTTON_HEIGHT));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(UIStyle.SECONDARY_DARK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(UIStyle.SECONDARY_COLOR);
            }
        });

        return button;
    }

    /**
     * 创建普通按钮（使用白色背景和主色调文字）
     */
    public static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UIStyle.BUTTON_FONT);
        button.setForeground(UIStyle.PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(6, 14, 6, 14),
            new LineBorder(UIStyle.PRIMARY_COLOR, 1)
        ));
        button.setBackground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, UIStyle.BUTTON_HEIGHT));

        return button;
    }

    // ==================== 标签组件 ====================

    /**
     * 创建标签
     */
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIStyle.LABEL_FONT);
        label.setForeground(UIStyle.TEXT_PRIMARY);
        return label;
    }

    /**
     * 创建次要标签（使用次要文本颜色）
     */
    public static JLabel createSecondaryLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIStyle.BODY_SMALL);
        label.setForeground(UIStyle.TEXT_TERTIARY);
        return label;
    }

    /**
     * 创建标题标签
     */
    public static JLabel createTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIStyle.TITLE_SMALL);
        label.setForeground(UIStyle.TEXT_PRIMARY);
        return label;
    }

    // ==================== 输入组件 ====================

    /**
     * 创建文本框 - 现代设计
     */
    public static JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(UIStyle.BODY_MEDIUM);
        textField.setForeground(UIStyle.TEXT_PRIMARY);
        textField.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(6, 10, 6, 10),
            new LineBorder(UIStyle.BORDER_COLOR, 1)
        ));
        textField.setBackground(Color.WHITE);
        textField.setPreferredSize(new Dimension(250, UIStyle.COMPONENT_HEIGHT));
        textField.setCaretColor(UIStyle.PRIMARY_COLOR);

        return textField;
    }

    /**
     * 创建密码框
     */
    public static JPasswordField createPasswordField() {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(UIStyle.BODY_MEDIUM);
        passwordField.setForeground(UIStyle.TEXT_PRIMARY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(6, 10, 6, 10),
            new LineBorder(UIStyle.BORDER_COLOR, 1)
        ));
        passwordField.setBackground(Color.WHITE);
        passwordField.setPreferredSize(new Dimension(250, UIStyle.COMPONENT_HEIGHT));
        passwordField.setCaretColor(UIStyle.PRIMARY_COLOR);
        passwordField.setEchoChar('●');

        return passwordField;
    }

    /**
     * 创建组合框
     */
    public static <T> JComboBox<T> createComboBox() {
        JComboBox<T> comboBox = new JComboBox<>();
        comboBox.setFont(UIStyle.BODY_MEDIUM);
        comboBox.setForeground(UIStyle.TEXT_PRIMARY);
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(6, 10, 6, 10),
            new LineBorder(UIStyle.BORDER_COLOR, 1)
        ));
        comboBox.setPreferredSize(new Dimension(250, UIStyle.COMPONENT_HEIGHT));

        return comboBox;
    }

    /**
     * 创建带有初始选项的组合框
     */
    public static JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(UIStyle.BODY_MEDIUM);
        comboBox.setForeground(UIStyle.TEXT_PRIMARY);
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(6, 10, 6, 10),
            new LineBorder(UIStyle.BORDER_COLOR, 1)
        ));
        comboBox.setPreferredSize(new Dimension(250, UIStyle.COMPONENT_HEIGHT));

        return comboBox;
    }

    /**
     * 创建文本区域
     */
    public static JTextArea createTextArea(int rows) {
        JTextArea textArea = new JTextArea(rows, 0);
        textArea.setFont(UIStyle.BODY_MEDIUM);
        textArea.setForeground(UIStyle.TEXT_PRIMARY);
        textArea.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(6, 10, 6, 10),
            new LineBorder(UIStyle.BORDER_COLOR, 1)
        ));
        textArea.setBackground(Color.WHITE);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setCaretColor(UIStyle.PRIMARY_COLOR);

        return textArea;
    }

    // ==================== 表格组件 ====================

    /**
     * 创建表格 - 现代清晰的设计
     */
    public static JTable createTable(DefaultTableModel tableModel) {
        JTable table = new JTable(tableModel);
        table.setFont(UIStyle.BODY_MEDIUM);
        table.setForeground(UIStyle.TEXT_PRIMARY);
        table.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        table.setGridColor(UIStyle.DIVIDER_COLOR);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, UIStyle.SPACING_SM));
        table.setSelectionBackground(UIStyle.TABLE_SELECTION);
        table.setSelectionForeground(UIStyle.TABLE_SELECTION_TEXT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // 设置表头样式
        JTableHeader header = table.getTableHeader();
        header.setFont(UIStyle.BODY_LARGE);
        header.setForeground(UIStyle.TABLE_HEADER_TEXT);
        header.setBackground(UIStyle.TABLE_HEADER_BG);
        header.setResizingAllowed(true);
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

        // 行背景交替
        table.setDefaultRenderer(Object.class, new ModernTableCellRenderer());

        return table;
    }

    // 自定义表格单元格渲染器
    private static class ModernTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

            // 交替行背景色
            if (isSelected) {
                setBackground(UIStyle.TABLE_SELECTION);
                setForeground(UIStyle.TABLE_SELECTION_TEXT);
            } else {
                if (row % 2 == 0) {
                    setBackground(UIStyle.TABLE_ROW_EVEN);
                } else {
                    setBackground(UIStyle.TABLE_ROW_ODD);
                }
                setForeground(UIStyle.TEXT_PRIMARY);
            }

            return this;
        }
    }

    // ==================== 容器组件 ====================

    /**
     * 创建滚动面板
     */
    public static JScrollPane createScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(UIStyle.SPACING_SM, UIStyle.SPACING_SM,
                UIStyle.SPACING_SM, UIStyle.SPACING_SM));

        // 滚动条样式
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setBlockIncrement(64);
        verticalScrollBar.setPreferredSize(new Dimension(10, 10));

        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        horizontalScrollBar.setUnitIncrement(16);
        horizontalScrollBar.setBlockIncrement(64);
        horizontalScrollBar.setPreferredSize(new Dimension(10, 10));

        return scrollPane;
    }

    /**
     * 创建卡片面板
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BorderLayout(UIStyle.SPACING_MD, UIStyle.SPACING_MD));
        panel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(UIStyle.SPACING_LG, UIStyle.SPACING_LG,
                    UIStyle.SPACING_LG, UIStyle.SPACING_LG),
            new LineBorder(UIStyle.DIVIDER_COLOR, 1)
        ));

        return panel;
    }

    /**
     * 创建普通面板
     */
    public static JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(UIStyle.PANEL_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(UIStyle.SPACING_MD, UIStyle.SPACING_MD,
                UIStyle.SPACING_MD, UIStyle.SPACING_MD));
        return panel;
    }

    /**
     * 创建带有内边距的面板
     */
    public static JPanel createPanelWithPadding(int padding) {
        JPanel panel = new JPanel();
        panel.setBackground(UIStyle.PANEL_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        return panel;
    }
}
