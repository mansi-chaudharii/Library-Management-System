package com.library.ui.theme;

import java.awt.Color;
import java.awt.Font;

/**
 * Defines the central visual theme for the Library Management System.
 * Light-blue and white modern palette, clean typography, and UI constants.
 */
public class UITheme {

    // --- Color Palette: White & Light Blue Theme ---
    public static final Color PRIMARY = new Color(2, 132, 199);         // Sky Blue #0284C7
    public static final Color PRIMARY_DARK = new Color(3, 105, 161);    // Sky Dark #0369A1
    public static final Color PRIMARY_LIGHT = new Color(224, 242, 254); // Very soft Ice Blue #E0F2FE
    public static final Color PRIMARY_HOVER = new Color(14, 165, 233);  // Sky Light Hover #0EA5E9

    public static final Color BG_APP = new Color(248, 250, 252);        // Slate 50 #F8FAFC
    public static final Color BG_CARD = Color.WHITE;                    // Pure White #FFFFFF
    public static final Color BG_SIDEBAR = new Color(240, 247, 255);    // Light Blue Ice #F0F7FF
    public static final Color BG_HEADER = Color.WHITE;

    public static final Color TEXT_MAIN = new Color(15, 23, 42);        // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);    // Slate 500
    public static final Color TEXT_LIGHT = new Color(148, 163, 184);    // Slate 400
    public static final Color TEXT_WHITE = Color.WHITE;

    public static final Color BORDER = new Color(226, 232, 240);        // Slate 200 #E2E8F0
    public static final Color BORDER_FOCUS = new Color(56, 189, 248);   // Sky Focus #38BDF8

    // Status & Badge Colors
    public static final Color SUCCESS = new Color(16, 185, 129);        // Emerald #10B981
    public static final Color SUCCESS_BG = new Color(209, 250, 229);     // Emerald Light #D1FAE5
    public static final Color WARNING = new Color(245, 158, 11);        // Amber #F59E0B
    public static final Color WARNING_BG = new Color(254, 243, 199);     // Amber Light #FEF3C7
    public static final Color DANGER = new Color(239, 68, 68);          // Red #EF4444
    public static final Color DANGER_BG = new Color(254, 226, 226);      // Red Light #FEE2E2
    public static final Color INFO = new Color(59, 130, 246);           // Blue #3B82F6
    public static final Color INFO_BG = new Color(219, 234, 254);        // Blue Light #DBEAFE

    // --- Modern Typography (Segoe UI is clean and standard on Windows) ---
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 11);

    // Private constructor to prevent instantiation
    private UITheme() {}
}
