import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

public class UpdateBell {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/View/MainPage.java");
        String c = Files.readString(p, StandardCharsets.UTF_8);

        String newBellBlock = 
        "        // ── Bell button with badge ──────────────────────────────────────\n" +
        "        JPanel bellWrapper = new JPanel(null);\n" +
        "        bellWrapper.setOpaque(false);\n" +
        "        bellWrapper.setPreferredSize(new java.awt.Dimension(42, 42));\n\n" +
        "        bellBtn = new JButton(\"\\uf0f3\");\n" +
        "        bellBtn.setFont(FA_FONT.deriveFont(18f));\n" +
        "        bellBtn.setBackground(new java.awt.Color(245, 247, 250));\n" +
        "        bellBtn.setForeground(TEXT_DARK);\n" +
        "        bellBtn.setBorderPainted(false);\n" +
        "        bellBtn.setFocusPainted(false);\n" +
        "        bellBtn.setOpaque(true);\n" +
        "        bellBtn.setBounds(0, 8, 36, 36);\n" +
        "        bellBtn.putClientProperty(\"FlatLaf.style\", \"arc: 999;\");\n" +
        "        bellBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));\n" +
        "        bellBtn.addActionListener(e -> openNotificationDialog());\n\n" +
        "        badgeLbl = new JLabel(\"\");\n" +
        "        badgeLbl.setFont(new java.awt.Font(\"Segoe UI\", java.awt.Font.BOLD, 9));\n" +
        "        badgeLbl.setForeground(java.awt.Color.WHITE);\n" +
        "        badgeLbl.setBackground(RED);\n" +
        "        badgeLbl.setOpaque(true);\n" +
        "        badgeLbl.setHorizontalAlignment(SwingConstants.CENTER);\n" +
        "        badgeLbl.setBounds(20, 4, 18, 18);\n" +
        "        badgeLbl.putClientProperty(\"FlatLaf.style\", \"arc: 999;\");\n" +
        "        badgeLbl.setVisible(false);\n\n" +
        "        bellWrapper.add(badgeLbl); // Add badge FIRST so it sits on top (Z-order in absolute layout)\n" +
        "        bellWrapper.add(bellBtn);\n";

        // We will replace from JPanel bellWrapper to bellWrapper.add(badgeLbl);
        // Using a regex to match the block:
        Pattern pattern = Pattern.compile("JPanel bellWrapper = new JPanel\\(null\\);.*?bellWrapper\\.add\\(badgeLbl\\);", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(c);
        if (matcher.find()) {
            c = matcher.replaceFirst(Matcher.quoteReplacement(
                "JPanel bellWrapper = new JPanel(null);\n" +
                "        bellWrapper.setOpaque(false);\n" +
                "        bellWrapper.setPreferredSize(new java.awt.Dimension(42, 42));\n\n" +
                "        bellBtn = new JButton(\"\\uf0f3\");\n" +
                "        bellBtn.setFont(FA_FONT.deriveFont(18f));\n" +
                "        bellBtn.setBackground(new java.awt.Color(245, 247, 250));\n" +
                "        bellBtn.setForeground(TEXT_DARK);\n" +
                "        bellBtn.setBorderPainted(false);\n" +
                "        bellBtn.setFocusPainted(false);\n" +
                "        bellBtn.setOpaque(true);\n" +
                "        bellBtn.setBounds(0, 8, 36, 36);\n" +
                "        bellBtn.putClientProperty(\"FlatLaf.style\", \"arc: 999;\");\n" +
                "        bellBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));\n" +
                "        bellBtn.addActionListener(e -> openNotificationDialog());\n\n" +
                "        badgeLbl = new JLabel(\"\");\n" +
                "        badgeLbl.setFont(new java.awt.Font(\"Segoe UI\", java.awt.Font.BOLD, 10));\n" +
                "        badgeLbl.setForeground(java.awt.Color.WHITE);\n" +
                "        badgeLbl.setBackground(RED);\n" +
                "        badgeLbl.setOpaque(true);\n" +
                "        badgeLbl.setHorizontalAlignment(SwingConstants.CENTER);\n" +
                "        badgeLbl.setBounds(20, 2, 20, 20);\n" + // Slightly larger and higher
                "        badgeLbl.putClientProperty(\"FlatLaf.style\", \"arc: 999;\");\n" +
                "        badgeLbl.setVisible(false);\n\n" +
                "        bellWrapper.add(badgeLbl); // Add badge FIRST so it sits on top\n" +
                "        bellWrapper.add(bellBtn);"
            ));
            Files.writeString(p, c, StandardCharsets.UTF_8);
            System.out.println("Replaced successfully!");
        } else {
            System.out.println("Could not find the block to replace.");
        }
    }
}
