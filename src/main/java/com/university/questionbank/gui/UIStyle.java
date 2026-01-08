package com.university.questionbank.gui;

import java.awt.Color;
import java.awt.Font;

/**
 * 统一UI样式类，定义应用的颜色方案和排版规则
 * 采用现代Material Design设计风格
 */
public class UIStyle {
    // ============ 现代配色方案 ============
    // 主色调 - 深蓝色，专业沉稳
    public static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    public static final Color PRIMARY_DARK = new Color(37, 99, 235);
    public static final Color PRIMARY_LIGHT = new Color(96, 165, 250);

    // 辅助色 - 优雅的紫色
    public static final Color SECONDARY_COLOR = new Color(139, 92, 246);
    public static final Color SECONDARY_DARK = new Color(124, 58, 237);

    // 背景色 - 温暖的灰白
    public static final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    public static final Color BACKGROUND_ALT = new Color(241, 245, 249);

    // 面板色 - 纯白，带微弱阴影
    public static final Color PANEL_COLOR = new Color(255, 255, 255);
    public static final Color PANEL_HOVER = new Color(249, 250, 251);

    // 文本颜色 - 层次分明
    public static final Color TEXT_PRIMARY = new Color(30, 41, 59);      // 深色主要文本
    public static final Color TEXT_SECONDARY = new Color(71, 85, 105);   // 次要文本
    public static final Color TEXT_TERTIARY = new Color(148, 163, 184); // 辅助文本
    public static final Color TEXT_DISABLED = new Color(203, 213, 225);

    // 边框和分隔线
    public static final Color BORDER_COLOR = new Color(226, 232, 240);
    public static final Color DIVIDER_COLOR = new Color(242, 244, 247);

    // 状态颜色
    public static final Color SUCCESS_COLOR = new Color(16, 185, 129);     // 绿色
    public static final Color SUCCESS_LIGHT = new Color(209, 250, 229);
    public static final Color WARNING_COLOR = new Color(245, 158, 11);     // 橙色
    public static final Color WARNING_LIGHT = new Color(255, 247, 237);
    public static final Color ERROR_COLOR = new Color(239, 68, 68);       // 红色
    public static final Color ERROR_LIGHT = new Color(254, 226, 226);

    // 表格颜色
    public static final Color TABLE_HEADER_BG = new Color(248, 250, 252);
    public static final Color TABLE_HEADER_TEXT = new Color(30, 41, 59);
    public static final Color TABLE_ROW_EVEN = new Color(255, 255, 255);
    public static final Color TABLE_ROW_ODD = new Color(249, 250, 252);
    public static final Color TABLE_SELECTION = new Color(59, 130, 246, 30);
    public static final Color TABLE_SELECTION_TEXT = new Color(255, 255, 255);

    // ============ 现代字体系统 ============
    // 使用系统默认中文字体，保持清晰易读
    public static final String FONT_FAMILY = "微软雅黑";

    // 标题系统
    public static final Font TITLE_LARGE = new Font(FONT_FAMILY, Font.BOLD, 28);
    public static final Font TITLE_MEDIUM = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font TITLE_SMALL = new Font(FONT_FAMILY, Font.BOLD, 18);

    // 正文系统
    public static final Font BODY_LARGE = new Font(FONT_FAMILY, Font.PLAIN, 15);
    public static final Font BODY_MEDIUM = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font BODY_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 13);

    // 标签和按钮
    public static final Font LABEL_FONT = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font CAPTION_FONT = new Font(FONT_FAMILY, Font.PLAIN, 12);

    // 代码和表格
    public static final Font MONOSPACE_FONT = new Font("Consolas", Font.PLAIN, 13);

    // 兼容旧代码（保留原有名称）
    public static final Font HEADING_FONT = TITLE_MEDIUM;
    public static final Font SUBHEADING_FONT = TITLE_SMALL;
    public static final Font NORMAL_FONT = BODY_MEDIUM;
    public static final Font SMALL_FONT = BODY_SMALL;

    // ============ 间距系统 ============
    // 统一的间距，基于8的倍数
    public static final int SPACING_XS = 4;
    public static final int SPACING_SM = 8;
    public static final int SPACING_MD = 16;
    public static final int SPACING_LG = 24;
    public static final int SPACING_XL = 32;

    // 兼容旧代码（保留原有名称）
    public static final int PADDING_SMALL = SPACING_SM;
    public static final int PADDING_NORMAL = SPACING_MD;
    public static final int PADDING_LARGE = SPACING_LG;

    // ============ 圆角和阴影 ============
    public static final int BORDER_RADIUS_SM = 6;
    public static final int BORDER_RADIUS_MD = 10;
    public static final int BORDER_RADIUS_LG = 16;

    public static final int BORDER_RADIUS = BORDER_RADIUS_MD;

    // 阴影颜色
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 8);
    public static final Color SHADOW_LIGHT = new Color(0, 0, 0, 5);

    // ============ 尺寸常量 ============
    public static final int COMPONENT_HEIGHT = 40;
    public static final int BUTTON_HEIGHT = 36;
    public static final int TABLE_ROW_HEIGHT = 44;
    public static final int ICON_SIZE = 20;

    // ============ 动画 ============
    public static final int ANIMATION_DURATION = 200; // ms
}
