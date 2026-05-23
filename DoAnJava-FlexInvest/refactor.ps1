$files = @(
    "src\View\permission\AccountPermissionPanel.java",
    "src\View\permission\RoleGroupPanel.java",
    "src\View\permission\SysFunctionPanel.java",
    "src\View\permission\SysRolePanel.java"
)

foreach ($file in $files) {
    $content = Get-Content $file -Raw
    
    # 1. Thay thế phương thức btn(String text, Color bg)
    $content = $content -replace '(?s)private JButton btn\(String text, Color bg\)\s*\{.*?return b;\s*\}', 
    'private JButton btn(String text, java.awt.Color bg) {
        javax.swing.JButton b = new javax.swing.JButton(text);
        b.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        b.setForeground(View.UIUtils.TEXT_DARK);
        b.setBackground(View.UIUtils.PRIMARY);
        b.putClientProperty("FlatLaf.style", "arc: 999; borderWidth: 0; focusWidth: 0;");
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return b;
    }'

    # 2. Xóa các màu cứng
    $content = $content -replace '(?m)^\s*private static final Color\s+(NAVY|BLUE|GREEN|RED|BG|TEXT_MUTED|BORDER_C|ROW_ALT)\s*=.*$', ''

    # 3. Thay thế các table settings
    # Match từ table.setFont(...) đến table.getTableHeader().setReorderingAllowed(false);
    $content = $content -replace '(?s)(\w+)\.setFont\(new Font\("Segoe UI", Font\.PLAIN, 13\)\);.*?\1\.getTableHeader\(\)\.setReorderingAllowed\(false\);', 'View.UIUtils.styleTable($1);'
    
    # 4. Xóa setDefaultRenderer
    $content = $content -replace '(?s)\w+\.setDefaultRenderer\(Object\.class, new DefaultTableCellRenderer\(\)\s*\{.*?\border\(new EmptyBorder\(0, 8, 0, 8\)\);\s*\}\s*\}\);', ''
    
    # 5. Fix title fonts
    $content = $content -replace 'title\.setForeground\(Color\.WHITE\);', 'title.setForeground(View.UIUtils.TEXT_DARK);'
    
    Set-Content -Path $file -Value $content -Encoding UTF8
}
