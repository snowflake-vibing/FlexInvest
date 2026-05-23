package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class UIUtils {

    // Màu theo mẫu Alpha Logic Center nhưng tông Cam-Vàng (Amber)
    public static final Color PRIMARY = new Color(245, 158, 11); // Amber #F59E0B
    public static final Color INDIGO = new Color(245, 158, 11); // Đổi biến INDIGO thành Amber để khỏi lỗi reference
    public static final Color NAVY = new Color(15, 40, 80);
    public static final Color TEXT_DARK = new Color(30, 30, 40);
    public static final Color TEXT_MUTED = new Color(100, 110, 120);
    public static final Color BORDER_COLOR = new Color(220, 230, 240);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color APP_BG = new Color(245, 247, 250); 
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
        table.getTableHeader().setBackground(PRIMARY); 
        table.getTableHeader().setForeground(TEXT_DARK); // Chữ đen trên nền vàng cam
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, BORDER_COLOR));
        table.setGridColor(BORDER_COLOR);
        table.setShowVerticalLines(false); // Tắt viền dọc để giảm ngộp
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(16, 0)); // Tạo padding trái/phải 8px
        table.setSelectionBackground(new Color(254, 243, 199)); // Màu cam cực nhạt khi select
        table.setSelectionForeground(TEXT_DARK);
        table.setBackground(CARD_BG);
        table.setFillsViewportHeight(true);
        table.putClientProperty("JTable.showAlternateRowColor", true);
        table.putClientProperty("FlatLaf.style", "alternateRowColor: #F9FAFB;");
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
