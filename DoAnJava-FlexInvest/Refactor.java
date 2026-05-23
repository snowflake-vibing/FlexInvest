import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class Refactor {
    public static void main(String[] args) throws Exception {
        String[] files = {
            "src/View/permission/AccountPermissionPanel.java",
            "src/View/permission/RoleGroupPanel.java",
            "src/View/permission/SysFunctionPanel.java",
            "src/View/permission/SysRolePanel.java"
        };
        String colors = "    private static final java.awt.Color NAVY      = View.UIUtils.TEXT_DARK;\n" +
                        "    private static final java.awt.Color BLUE      = View.UIUtils.PRIMARY;\n" +
                        "    private static final java.awt.Color GREEN     = View.UIUtils.PRIMARY;\n" +
                        "    private static final java.awt.Color RED       = new java.awt.Color(239, 68, 68);\n" +
                        "    private static final java.awt.Color BG        = java.awt.Color.WHITE;\n" +
                        "    private static final java.awt.Color TEXT_MUTED = View.UIUtils.TEXT_MUTED;\n" +
                        "    private static final java.awt.Color BORDER_C  = View.UIUtils.BORDER_COLOR;\n" +
                        "    private static final java.awt.Color ROW_ALT   = new java.awt.Color(248, 249, 250);\n";
                        
        for (String f : files) {
            String c = Files.readString(Paths.get(f), StandardCharsets.UTF_8);
            
            c = c.replaceAll("(?s)private JButton btn\\(String text, Color bg\\)\\s*\\{.*?return b;\\s*\\}", 
                "private JButton btn(String text, java.awt.Color bg) {\n" +
                "        javax.swing.JButton b = new javax.swing.JButton(text);\n" +
                "        b.setFont(new java.awt.Font(\"Segoe UI\", java.awt.Font.BOLD, 12));\n" +
                "        b.setForeground(View.UIUtils.TEXT_DARK);\n" +
                "        b.setBackground(View.UIUtils.PRIMARY);\n" +
                "        b.putClientProperty(\"FlatLaf.style\", \"arc: 999; borderWidth: 0; focusWidth: 0;\");\n" +
                "        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));\n" +
                "        return b;\n" +
                "    }");
            
            c = c.replaceAll("(?m)^\\s*private static final Color\\s+(NAVY|BLUE|GREEN|RED|BG|TEXT_MUTED|BORDER_C|ROW_ALT)\\s*=.*$", "");
            
            c = c.replaceAll("(?s)(\\w+)\\.setFont\\(new Font\\(\"Segoe UI\", Font\\.PLAIN, 13\\)\\);.*?\\1\\.getTableHeader\\(\\)\\.setReorderingAllowed\\(false\\);", "View.UIUtils.styleTable($1);");
            
            c = c.replaceAll("(?s)\\w+\\.setDefaultRenderer\\(Object\\.class, new DefaultTableCellRenderer\\(\\)\\s*\\{.*?\\border\\(new EmptyBorder\\(0, 8, 0, 8\\)\\);\\s*\\}\\s*\\}\\);", "");
            
            c = c.replace("title.setForeground(Color.WHITE);", "title.setForeground(View.UIUtils.TEXT_DARK);");
            
            c = c.replaceFirst("(public class \\w+ extends JPanel \\{)", "$1\n" + colors);
            
            Files.writeString(Paths.get(f), c, StandardCharsets.UTF_8);
        }
    }
}
