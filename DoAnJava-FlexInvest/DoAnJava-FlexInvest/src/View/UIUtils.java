package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class UIUtils {

    // Bảng màu xanh lá hiện đại (Emerald Green)
    public static final Color PRIMARY = new Color(52, 211, 153);  // Emerald Light #34D399
    public static final Color PRIMARY_DARK = new Color(5, 150, 105); // Emerald Dark #059669
    public static final Color INDIGO  = PRIMARY;
    public static final Color NAVY = new Color(6, 78, 59);        // Dark Emerald #064E3B
    public static final Color TEXT_DARK = new Color(30, 30, 40);
    public static final Color TEXT_MUTED = new Color(100, 110, 120);
    public static final Color BORDER_COLOR = new Color(167, 243, 208); // Emerald border #A7F3D0
    public static final Color CARD_BG = Color.WHITE;
    public static final Color APP_BG = new Color(240, 253, 244);  // Green-tinted bg #F0FDF4
    public static final Color ACCENT = PRIMARY;

    /**
     * Chuyển đổi một JPanel thành "Card" thông thường.
     */
    public static void styleCard(JPanel panel) {
        panel.setBackground(CARD_BG);
        panel.putClientProperty("FlatLaf.style", "arc: 16;");
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
    }

    /**
     * Chuyển đổi một JPanel thành "Summary Card" có viền màu phía trên (giống ảnh mẫu).
     */
    public static void styleSummaryCard(JPanel panel, Color topBorderColor) {
        panel.setBackground(CARD_BG);
        panel.putClientProperty("FlatLaf.style", "arc: 0;"); // Bỏ bo góc để viền trên đẹp hơn, hoặc giữ bo tròn.
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new MatteBorder(4, 0, 0, 0, topBorderColor)
            ),
            new EmptyBorder(20, 20, 20, 20)
        ));
    }

    /**
     * Styling chung cho Bảng (Table) chuẩn Web (Header màu PRIMARY, chữ Trắng).
     */
    public static void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY_DARK);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, BORDER_COLOR));
        table.setGridColor(BORDER_COLOR);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(16, 0));
        table.setSelectionBackground(new Color(209, 250, 229)); // Emerald 100 #D1FAE5
        table.setSelectionForeground(TEXT_DARK);
        table.setBackground(CARD_BG);
        table.setFillsViewportHeight(true);
        table.putClientProperty("JTable.showAlternateRowColor", true);
        table.putClientProperty("FlatLaf.style", "alternateRowColor: #F0FDF4;");
    }

    /**
     * Tạo Heading chuẩn H1
     */
    public static JLabel createHeading1(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lbl.setForeground(TEXT_DARK); // Chữ đen thay vì màu nhấn để dễ đọc trên mọi nền
        return lbl;
    }

    /**
     * Tạo Heading chuẩn H2
     */
    public static JLabel createHeading2(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    /**
     * Tạo Text mờ (Subtext)
     */
    public static JLabel createMutedText(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }
}
