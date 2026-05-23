import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class UpdateIcons {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/View/MainPage.java");
        String c = Files.readString(p, StandardCharsets.UTF_8);

        // 1. Add FA_FONT static block
        String fontBlock = "    private static java.awt.Font FA_FONT;\n" +
                           "    static {\n" +
                           "        try {\n" +
                           "            FA_FONT = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new java.io.File(\"src/Resources/fa-solid-900.ttf\")).deriveFont(15f);\n" +
                           "        } catch (Exception e) {\n" +
                           "            FA_FONT = new java.awt.Font(\"Segoe UI\", java.awt.Font.PLAIN, 15);\n" +
                           "        }\n" +
                           "    }\n\n" +
                           "    private JPanel buildSidebar() {";
        c = c.replace("    private JPanel buildSidebar() {", fontBlock);

        // 2. Replace calls in buildSidebar (matching the ORIGINAL text exactly using regex)
        c = c.replaceAll("buildMenuItem\\(\"Dashboard\",\\s*CARD_STAFF_DASH,\\s*true\\)", "buildMenuItem(\"Dashboard\", \"\\\\uf015\", CARD_STAFF_DASH, true)");
        c = c.replaceAll("buildMenuItem\\(\"Duyệt Nạp Tiền\",\\s*CARD_STAFF_DEPOSIT,\\s*false\\)", "buildMenuItem(\"Duyệt Nạp Tiền\", \"\\\\uf019\", CARD_STAFF_DEPOSIT, false)");
        c = c.replaceAll("buildMenuItem\\(\"Duyệt Rút Tiền\",\\s*CARD_STAFF_WITHDRAW,\\s*false\\)", "buildMenuItem(\"Duyệt Rút Tiền\", \"\\\\uf093\", CARD_STAFF_WITHDRAW, false)");
        c = c.replaceAll("buildMenuItem\\(\"Duyệt eKYC\",\\s*CARD_STAFF_KYC,\\s*false\\)", "buildMenuItem(\"Duyệt eKYC\", \"\\\\uf3ed\", CARD_STAFF_KYC, false)");

        c = c.replaceAll("buildMenuItem\\(\"Dashboard\",\\s*CARD_DASHBOARD,\\s*true\\)", "buildMenuItem(\"Dashboard\", \"\\\\uf015\", CARD_DASHBOARD, true)");
        c = c.replaceAll("buildMenuItem\\(\"Gói Đầu Tư\",\\s*CARD_PRODUCTS,\\s*false\\)", "buildMenuItem(\"Gói Đầu Tư\", \"\\\\uf466\", CARD_PRODUCTS, false)");
        c = c.replaceAll("buildMenuItem\\(\"Khoản Của Tôi\",\\s*CARD_INVESTMENTS,\\s*false\\)", "buildMenuItem(\"Khoản Của Tôi\", \"\\\\uf201\", CARD_INVESTMENTS, false)");
        c = c.replaceAll("buildMenuItem\\(\"Ví của tôi\",\\s*CARD_WALLET,\\s*false\\)", "buildMenuItem(\"Ví của tôi\", \"\\\\uf555\", CARD_WALLET, false)");
        c = c.replaceAll("buildMenuItem\\(\"Quản lý Tiền\",\\s*CARD_MONEY,\\s*false\\)", "buildMenuItem(\"Quản lý Tiền\", \"\\\\uf51e\", CARD_MONEY, false)");
        c = c.replaceAll("buildMenuItem\\(\"Nhiệm vụ & Token\",\\s*CARD_MISSION,\\s*false\\)", "buildMenuItem(\"Nhiệm vụ & Token\", \"\\\\uf091\", CARD_MISSION, false)");

        c = c.replaceAll("buildMenuItem\\(\"Tổng quan \\(Admin\\)\",\\s*CARD_ADMIN_DASH,\\s*false\\)", "buildMenuItem(\"Tổng quan (Admin)\", \"\\\\uf085\", CARD_ADMIN_DASH, false)");
        c = c.replaceAll("buildMenuItem\\(\"Quản lý Gói\",\\s*CARD_ADMIN_PRODUCTS,\\s*false\\)", "buildMenuItem(\"Quản lý Gói\", \"\\\\uf466\", CARD_ADMIN_PRODUCTS, false)");
        c = c.replaceAll("buildMenuItem\\(\"Quản lý User\",\\s*CARD_ADMIN_USERS,\\s*false\\)", "buildMenuItem(\"Quản lý User\", \"\\\\uf0c0\", CARD_ADMIN_USERS, false)");
        c = c.replaceAll("buildMenuItem\\(\"Lịch sử GD hệ thống\",\\s*CARD_ADMIN_TX,\\s*false\\)", "buildMenuItem(\"Lịch sử GD hệ thống\", \"\\\\uf1da\", CARD_ADMIN_TX, false)");
        c = c.replaceAll("buildMenuItem\\(\"Quản lý Nhiệm vụ\",\\s*CARD_ADMIN_MISSION,\\s*false\\)", "buildMenuItem(\"Quản lý Nhiệm vụ\", \"\\\\uf0ae\", CARD_ADMIN_MISSION, false)");
        c = c.replaceAll("buildMenuItemCustom\\(\"Phân Quyền\",\\s*e -> new PermissionManagementView\\(\\)\\)", "buildMenuItemCustom(\"Phân Quyền\", \"\\\\uf084\", e -> new PermissionManagementView())");

        // 3. Replace buildMenuItem
        String newBuildMenuItem = 
            "    private JPanel buildMenuItem(String title, String iconCode, String cardId, boolean active) {\n" +
            "        JPanel item = new JPanel(new BorderLayout(14, 0));\n" +
            "        item.setMaximumSize(new Dimension(190, 42));\n" +
            "        item.setBackground(SIDEBAR);\n" +
            "        item.setBorder(new EmptyBorder(5, 12, 5, 8));\n" +
            "        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));\n" +
            "        item.setOpaque(true);\n" +
            "        item.putClientProperty(\"FlatLaf.style\", \"arc: 16;\");\n" +
            "\n" +
            "        JLabel iconLbl = new JLabel(iconCode, SwingConstants.CENTER);\n" +
            "        iconLbl.setFont(FA_FONT);\n" +
            "        iconLbl.setPreferredSize(new Dimension(32, 32));\n" +
            "        iconLbl.setOpaque(true);\n" +
            "        iconLbl.putClientProperty(\"FlatLaf.style\", \"arc: 12;\");\n" +
            "        \n" +
            "        JLabel lbl = new JLabel(title);\n" +
            "\n" +
            "        item.add(iconLbl, BorderLayout.WEST);\n" +
            "        item.add(lbl, BorderLayout.CENTER);\n" +
            "        setActiveStyle(item, active);\n" +
            "\n" +
            "        item.addMouseListener(new MouseAdapter() {\n" +
            "            public void mouseEntered(MouseEvent e) {\n" +
            "                if (activeItem != item) item.setBackground(new Color(251, 191, 36));\n" +
            "            }\n" +
            "            public void mouseExited(MouseEvent e) {\n" +
            "                if (activeItem != item) item.setBackground(SIDEBAR);\n" +
            "            }\n" +
            "            public void mouseClicked(MouseEvent e) {\n" +
            "                if (activeItem != null) setActiveStyle(activeItem, false);\n" +
            "                activeItem = item;\n" +
            "                setActiveStyle(item, true);\n" +
            "                showPanel(cardId);\n" +
            "            }\n" +
            "        });\n" +
            "        return item;\n" +
            "    }";
        
        c = c.replaceAll("(?s)private JPanel buildMenuItem\\(String title, String cardId, boolean active\\) \\{.*?return item;\\s*\\}", newBuildMenuItem);

        // 4. Replace buildMenuItemCustom
        String newBuildMenuItemCustom = 
            "    private JPanel buildMenuItemCustom(String title, String iconCode, ActionListener onClick) {\n" +
            "        JPanel item = new JPanel(new BorderLayout(14, 0));\n" +
            "        item.setMaximumSize(new Dimension(190, 42));\n" +
            "        item.setBackground(SIDEBAR);\n" +
            "        item.setBorder(new EmptyBorder(5, 12, 5, 8));\n" +
            "        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));\n" +
            "        item.setOpaque(true);\n" +
            "        item.putClientProperty(\"FlatLaf.style\", \"arc: 16;\");\n" +
            "\n" +
            "        JLabel iconLbl = new JLabel(iconCode, SwingConstants.CENTER);\n" +
            "        iconLbl.setFont(FA_FONT);\n" +
            "        iconLbl.setPreferredSize(new Dimension(32, 32));\n" +
            "        iconLbl.setOpaque(true);\n" +
            "        iconLbl.putClientProperty(\"FlatLaf.style\", \"arc: 12;\");\n" +
            "        iconLbl.setBackground(new Color(255, 255, 255, 80));\n" +
            "        iconLbl.setForeground(TEXT_DARK);\n" +
            "\n" +
            "        JLabel lbl = new JLabel(title);\n" +
            "        lbl.setFont(new Font(\"Segoe UI\", Font.PLAIN, 13));\n" +
            "        lbl.setForeground(TEXT_DARK);\n" +
            "\n" +
            "        item.add(iconLbl, BorderLayout.WEST);\n" +
            "        item.add(lbl, BorderLayout.CENTER);\n" +
            "\n" +
            "        item.addMouseListener(new MouseAdapter() {\n" +
            "            public void mouseEntered(MouseEvent e) { item.setBackground(new Color(251, 191, 36)); }\n" +
            "            public void mouseExited(MouseEvent e)  { item.setBackground(SIDEBAR); }\n" +
            "            public void mouseClicked(MouseEvent e) { if (onClick != null) onClick.actionPerformed(null); }\n" +
            "        });\n" +
            "        return item;\n" +
            "    }";

        c = c.replaceAll("(?s)private JPanel buildMenuItemCustom\\(String title, ActionListener onClick\\) \\{.*?return item;\\s*\\}", newBuildMenuItemCustom);

        // 5. Replace setActiveStyle
        String newSetActiveStyle = 
            "    private void setActiveStyle(JPanel item, boolean active) {\n" +
            "        if (item == null) return;\n" +
            "        item.setBackground(active ? Color.WHITE : SIDEBAR);\n" +
            "        if (item.getComponentCount() > 1 && item.getComponent(0) instanceof JLabel && item.getComponent(1) instanceof JLabel) {\n" +
            "            JLabel iconLbl = (JLabel) item.getComponent(0);\n" +
            "            iconLbl.setBackground(active ? View.UIUtils.PRIMARY : new Color(255, 255, 255, 80));\n" +
            "            iconLbl.setForeground(active ? Color.WHITE : TEXT_DARK);\n" +
            "\n" +
            "            JLabel lbl = (JLabel) item.getComponent(1);\n" +
            "            lbl.setFont(new Font(\"Segoe UI\", active ? Font.BOLD : Font.PLAIN, 13));\n" +
            "            lbl.setForeground(TEXT_DARK);\n" +
            "        }\n" +
            "    }";
            
        c = c.replaceAll("(?s)private void setActiveStyle\\(JPanel item, boolean active\\) \\{.*?\\}\\s*\\}", newSetActiveStyle);

        Files.writeString(p, c, StandardCharsets.UTF_8);
    }
}
