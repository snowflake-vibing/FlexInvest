import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

public class UpdateBadge {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/View/MainPage.java");
        String c = Files.readString(p, StandardCharsets.UTF_8);

        String oldBadge = 
            "        badgeLbl = new JLabel(\"\");\n" +
            "        badgeLbl.setFont(new java.awt.Font(\"Segoe UI\", java.awt.Font.BOLD, 10));\n" +
            "        badgeLbl.setForeground(java.awt.Color.WHITE);\n" +
            "        badgeLbl.setBackground(RED);\n" +
            "        badgeLbl.setOpaque(true);\n" +
            "        badgeLbl.setHorizontalAlignment(SwingConstants.CENTER);\n" +
            "        badgeLbl.setBounds(20, 2, 20, 20);\n" +
            "        badgeLbl.putClientProperty(\"FlatLaf.style\", \"arc: 999;\");\n" +
            "        badgeLbl.setVisible(false);";

        String newBadge = 
            "        badgeLbl = new JLabel(\"\") {\n" +
            "            @Override\n" +
            "            protected void paintComponent(java.awt.Graphics g) {\n" +
            "                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();\n" +
            "                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);\n" +
            "                g2.setColor(getBackground());\n" +
            "                g2.fillOval(0, 0, getWidth(), getHeight());\n" +
            "                g2.dispose();\n" +
            "                super.paintComponent(g);\n" +
            "            }\n" +
            "        };\n" +
            "        badgeLbl.setFont(new java.awt.Font(\"Segoe UI\", java.awt.Font.BOLD, 10));\n" +
            "        badgeLbl.setForeground(java.awt.Color.WHITE);\n" +
            "        badgeLbl.setBackground(RED);\n" +
            "        badgeLbl.setOpaque(false); // MUST be false so JLabel doesn't paint the sharp background\n" +
            "        badgeLbl.setHorizontalAlignment(SwingConstants.CENTER);\n" +
            "        badgeLbl.setBounds(22, 2, 18, 18);\n" + // Adjusted coordinates
            "        badgeLbl.setVisible(false);";

        // Let's use regex in case of slight whitespace differences
        Pattern pattern = Pattern.compile("badgeLbl = new JLabel\\(\"\"\\);.*?badgeLbl\\.setVisible\\(false\\);", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(c);
        if (matcher.find()) {
            c = matcher.replaceFirst(Matcher.quoteReplacement(newBadge));
            Files.writeString(p, c, StandardCharsets.UTF_8);
            System.out.println("Badge replaced successfully!");
        } else {
            System.out.println("Could not find the badgeLbl block.");
        }
    }
}
