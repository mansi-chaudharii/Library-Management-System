package com.library.ui.theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Utility functions for creating consistent, beautiful UI components
 * adhering to the light-blue and white modern theme.
 */
public class UIUtils {

    /**
     * Creates a styled modern button with rounded corners and hover effect.
     */
    public static JButton createButton(String text, Color bgColor, Color fgColor, Color hoverColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover() && hoverColor != null) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(fgColor);
        btn.setBackground(bgColor);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, UITheme.PRIMARY, UITheme.TEXT_WHITE, UITheme.PRIMARY_HOVER);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, UITheme.SUCCESS, UITheme.TEXT_WHITE, new Color(5, 150, 105));
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, UITheme.DANGER, UITheme.TEXT_WHITE, new Color(220, 38, 38));
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(UITheme.PRIMARY_LIGHT);
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(UITheme.TEXT_MAIN);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Creates a styled text field with comfortable padding and clean border.
     */
    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(UITheme.FONT_REGULAR);
        tf.setForeground(UITheme.TEXT_MAIN);
        tf.setCaretColor(UITheme.PRIMARY);
        tf.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 36));
        return tf;
    }

    /**
     * Creates a styled password field.
     */
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(UITheme.FONT_REGULAR);
        pf.setForeground(UITheme.TEXT_MAIN);
        pf.setCaretColor(UITheme.PRIMARY);
        pf.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        pf.setPreferredSize(new Dimension(pf.getPreferredSize().width, 36));
        return pf;
    }

    /**
     * Creates a white card container with rounded edges and subtle border.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        return card;
    }

    /**
     * Creates an attractive KPI Statistic Card for dashboard.
     */
    public static JPanel createStatCard(String title, String value, String subtitle, Color accentColor, String emoji) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                // Left accent bar
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 6, getHeight(), 6, 6);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(10, 5));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Center: Title, Value, Subtitle
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL_BOLD);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valLbl.setForeground(UITheme.TEXT_MAIN);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(accentColor.darker());

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valLbl);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(subLbl);

        // Right side: Icon badge
        JLabel iconLbl = new JLabel(emoji, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLbl.setPreferredSize(new Dimension(46, 46));

        card.add(textPanel, BorderLayout.CENTER);
        card.add(iconLbl, BorderLayout.EAST);
        return card;
    }

    /**
     * Styles a JTable to match modern, clean aesthetics with proper headings,
     * cell padding, and alternating row colors.
     */
    public static void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249)); // Very subtle grid
        table.setSelectionBackground(new Color(224, 242, 254)); // Soft light-blue selection
        table.setSelectionForeground(UITheme.TEXT_MAIN);
        table.setFont(UITheme.FONT_REGULAR);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(UITheme.FONT_HEADER);
        header.setBackground(UITheme.PRIMARY_LIGHT);
        header.setForeground(UITheme.PRIMARY_DARK);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        // Default Alternating Row Renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10)); // Comfortable horizontal padding
                if (!isSel) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(UITheme.TEXT_MAIN);
                }
                return c;
            }
        });
    }

    /**
     * Table Cell Renderer that displays status badges as rounded colored pills.
     */
    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = (value != null) ? value.toString() : "";
            JPanel pillPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
            pillPanel.setOpaque(true);
            pillPanel.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

            Color bg;
            Color fg;

            if ("Active".equalsIgnoreCase(status) || "Available".equalsIgnoreCase(status)) {
                bg = UITheme.SUCCESS_BG;
                fg = UITheme.SUCCESS.darker();
            } else if ("Issued".equalsIgnoreCase(status)) {
                bg = UITheme.INFO_BG;
                fg = UITheme.INFO.darker();
            } else if ("Overdue".equalsIgnoreCase(status) || "Suspended".equalsIgnoreCase(status)) {
                bg = UITheme.DANGER_BG;
                fg = UITheme.DANGER.darker();
            } else if ("Returned".equalsIgnoreCase(status)) {
                bg = new Color(241, 245, 249);
                fg = new Color(71, 85, 105);
            } else {
                bg = UITheme.WARNING_BG;
                fg = UITheme.WARNING.darker();
            }

            final Color badgeBg = bg;
            final Color badgeFg = fg;

            JLabel label = new JLabel(status) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(badgeBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            label.setFont(UITheme.FONT_SMALL_BOLD);
            label.setForeground(badgeFg);
            label.setBorder(new EmptyBorder(3, 10, 3, 10));

            pillPanel.add(label);
            return pillPanel;
        }
    }
}
