package View;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import Controller.LoginController;
import Controller.RegisterController;
import Model.AccountModel;
import Utils.SessionManager;

public class LoginForm extends JFrame {

    // ===== Panel màu bên trái =====
    private JPanel pnlLeft;

    private JPanel pnlMain;
    private JPanel pnlTab;
    private JPanel pnlContent; // CardLayout chứa 2 trang
    private JLabel lblDangNhap;
    private JLabel lblDangKy;

    // === Form Đăng Nhập ===
    private JPanel pnlLogin;
    private JLabel lblUsername;
    private JTextField txtUsername;
    private JLabel lblPassword;
    private JPasswordField txtPassword;
    private JLabel lblForgot;
    private JButton btnLogin;

    // === Form Đăng Ký ===
    private JPanel pnlRegister;
    private JLabel lblRegUser;
    private JTextField txtRegUser;
    private JLabel lblRegEmail;
    private JTextField txtRegEmail;
    private JLabel lblRegPass;
    private JPasswordField txtRegPass;
    private JLabel lblRegConfirm;
    private JPasswordField txtRegConfirm;
    private JLabel lblRegReferral;
    private JTextField txtRegReferral;
    private JButton btnRegister;

    // === Màu sắc (chuẩn FlexInvest) ===
    private static final Color NAVY      = UIUtils.PRIMARY;
    private static final Color BLUE      = UIUtils.PRIMARY;
    private static final Color RED        = new Color(239, 68, 68);
    private static final Color TEXT_MUTED = new Color(110, 115, 130);
    private static final Color BORDER_C  = new Color(210, 220, 235);
    private static final Color PH_COLOR  = new Color(180, 185, 195);

    // === Placeholder texts ===
    private static final String PH_USERNAME = "Nhập tên đăng nhập";
    private static final String PH_PASSWORD = "Nhập mật khẩu";
    private static final String PH_EMAIL    = "Nhập email của bạn";
    private static final String PH_CONFIRM  = "Nhập lại mật khẩu";
    private static final String PH_REFERRAL = "Nhập mã giới thiệu (nếu có)";

    // === Controllers ===
    private final LoginController    loginController    = new LoginController();
    private final RegisterController registerController = new RegisterController();

    private CardLayout cardLayout;

    public LoginForm() {
        initComponents();
        styleComponents();
        addListeners();
        setVisible(true);
    }

    private void initComponents() {
        setTitle("FlexInvest - Đăng Nhập / Đăng Ký");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel chính: split trái/phải
        pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBackground(Color.WHITE);
        setContentPane(pnlMain);

        // ---- Panel màu bên trái ----
        pnlLeft = new JPanel();
        pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
        pnlLeft.setBackground(UIUtils.PRIMARY);
        pnlLeft.setPreferredSize(new Dimension(400, 620));
        
        pnlLeft.add(Box.createVerticalGlue());
        
        JLabel lblWelcome1 = new JLabel("Chào mừng đến với");
        lblWelcome1.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWelcome1.setForeground(Color.WHITE);
        lblWelcome1.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlLeft.add(lblWelcome1);

        pnlLeft.add(Box.createVerticalStrut(10));

        JLabel lblWelcome2 = new JLabel("FlexInvest");
        lblWelcome2.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblWelcome2.setForeground(Color.WHITE);
        lblWelcome2.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlLeft.add(lblWelcome2);
        
        pnlLeft.add(Box.createVerticalGlue());
        
        pnlMain.add(pnlLeft, BorderLayout.WEST);

        // ---- Panel phải: tab + content ----
        JPanel pnlRight = new JPanel(new BorderLayout());
        pnlRight.setBackground(Color.WHITE);

        // ---- Tab header ----
        pnlTab = new JPanel(new GridLayout(1, 2));
        pnlTab.setBackground(Color.WHITE);
        pnlTab.setPreferredSize(new Dimension(500, 55));
        lblDangNhap = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        lblDangKy   = new JLabel("ĐĂNG KÝ",   SwingConstants.CENTER);
        pnlTab.add(lblDangNhap);
        pnlTab.add(lblDangKy);
        pnlRight.add(pnlTab, BorderLayout.NORTH);

        // ---- CardLayout content ----
        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(Color.WHITE);

        // ---- Trang Đăng Nhập ----
        pnlLogin = new JPanel();
        pnlLogin.setBackground(Color.WHITE);
        pnlLogin.setLayout(new BoxLayout(pnlLogin, BoxLayout.Y_AXIS));
        pnlLogin.setBorder(new EmptyBorder(10, 50, 30, 50));

        lblUsername = new JLabel("Tên đăng nhập");
        txtUsername = new JTextField();
        lblPassword = new JLabel("Mật khẩu");
        txtPassword = new JPasswordField();
        lblForgot   = new JLabel("<html><u>Quên mật khẩu</u></html>");
        btnLogin = new JButton("Đăng nhập");

        pnlLogin.add(lblUsername);
        pnlLogin.add(Box.createVerticalStrut(6));
        pnlLogin.add(txtUsername);
        pnlLogin.add(Box.createVerticalStrut(16));
        pnlLogin.add(lblPassword);
        pnlLogin.add(Box.createVerticalStrut(6));
        pnlLogin.add(txtPassword);
        pnlLogin.add(Box.createVerticalStrut(10));
        pnlLogin.add(lblForgot);
        pnlLogin.add(Box.createVerticalStrut(16));
        pnlLogin.add(btnLogin);

        // ---- Trang Đăng Ký ----
        pnlRegister = new JPanel();
        pnlRegister.setBackground(Color.WHITE);
        pnlRegister.setLayout(new BoxLayout(pnlRegister, BoxLayout.Y_AXIS));
        pnlRegister.setBorder(new EmptyBorder(10, 50, 30, 50));

        lblRegUser    = new JLabel("Tên đăng nhập");
        txtRegUser    = new JTextField();
        lblRegEmail   = new JLabel("Email");
        txtRegEmail   = new JTextField();
        lblRegPass    = new JLabel("Mật khẩu");
        txtRegPass    = new JPasswordField();
        lblRegConfirm = new JLabel("Xác nhận mật khẩu");
        txtRegConfirm = new JPasswordField();
        lblRegReferral = new JLabel("Mã giới thiệu (không bắt buộc)");
        txtRegReferral = new JTextField();
        btnRegister   = new JButton("Tạo tài khoản");

        pnlRegister.add(lblRegUser);
        pnlRegister.add(Box.createVerticalStrut(6));
        pnlRegister.add(txtRegUser);
        pnlRegister.add(Box.createVerticalStrut(14));
        pnlRegister.add(lblRegEmail);
        pnlRegister.add(Box.createVerticalStrut(6));
        pnlRegister.add(txtRegEmail);
        pnlRegister.add(Box.createVerticalStrut(14));
        pnlRegister.add(lblRegPass);
        pnlRegister.add(Box.createVerticalStrut(6));
        pnlRegister.add(txtRegPass);
        pnlRegister.add(Box.createVerticalStrut(14));
        pnlRegister.add(lblRegConfirm);
        pnlRegister.add(Box.createVerticalStrut(6));
        pnlRegister.add(txtRegConfirm);
        pnlRegister.add(Box.createVerticalStrut(14));
        pnlRegister.add(lblRegReferral);
        pnlRegister.add(Box.createVerticalStrut(6));
        pnlRegister.add(txtRegReferral);
        pnlRegister.add(Box.createVerticalStrut(20));
        pnlRegister.add(btnRegister);

        // ---- Thêm vào CardLayout ----
        JScrollPane regScroll = new JScrollPane(pnlRegister);
        regScroll.setBorder(null);
        regScroll.getViewport().setBackground(Color.WHITE);
        regScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pnlContent.add(pnlLogin,    "LOGIN");
        pnlContent.add(regScroll,   "REGISTER");

        pnlRight.add(pnlContent, BorderLayout.CENTER);
        pnlMain.add(pnlRight, BorderLayout.CENTER);
    }

    private void styleComponents() {
        Font tabFont = new Font("Segoe UI", Font.BOLD, 14);

        lblDangNhap.setFont(tabFont);
        lblDangNhap.setForeground(Color.BLACK);
        lblDangNhap.setBorder(new MatteBorder(0, 0, 3, 0, BLUE));
        lblDangNhap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lblDangKy.setFont(tabFont);
        lblDangKy.setForeground(TEXT_MUTED);
        lblDangKy.setBorder(new EmptyBorder(0, 0, 3, 0));
        lblDangKy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Labels đăng nhập
        Font lf = new Font("Segoe UI", Font.PLAIN, 13);
        styleLabel(lblUsername, lf);
        styleLabel(lblPassword, lf);
        lblForgot.setFont(lf); lblForgot.setForeground(TEXT_MUTED);
        lblForgot.setAlignmentX(LEFT_ALIGNMENT);
        lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblForgot.setBorder(new EmptyBorder(0, 0, 6, 0));

        // Labels đăng ký
        styleLabel(lblRegUser, lf);
        styleLabel(lblRegEmail, lf);
        styleLabel(lblRegPass, lf);
        styleLabel(lblRegConfirm, lf);
        styleLabel(lblRegReferral, lf);

        // Fields đăng nhập
        styleField(txtUsername);
        setPlaceholder(txtUsername, PH_USERNAME);
        styleField(txtPassword);
        setPasswordPlaceholder(txtPassword, PH_PASSWORD);

        // Fields đăng ký
        styleField(txtRegUser);
        setPlaceholder(txtRegUser, PH_USERNAME);
        styleField(txtRegEmail);
        setPlaceholder(txtRegEmail, PH_EMAIL);
        styleField(txtRegPass);
        setPasswordPlaceholder(txtRegPass, PH_PASSWORD);
        styleField(txtRegConfirm);
        setPasswordPlaceholder(txtRegConfirm, PH_CONFIRM);
        styleField(txtRegReferral);
        setPlaceholder(txtRegReferral, PH_REFERRAL);

        // Buttons
        styleButton(btnLogin, BLUE);
        styleButton(btnRegister, BLUE);
    }

    private void styleLabel(JLabel l, Font f) {
        l.setFont(f); l.setForeground(TEXT_MUTED); l.setAlignmentX(LEFT_ALIGNMENT);
    }

    private void styleField(JComponent f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        f.setPreferredSize(new Dimension(400, 44));
        f.setBackground(Color.WHITE);
        f.putClientProperty("JTextField.showClearButton", true);
        f.putClientProperty("JTextField.underline", true);
    }

    private void setPlaceholder(JTextField f, String ph) {
        f.setText(ph); f.setForeground(PH_COLOR);
        f.addFocusListener(new FocusAdapter() {
            boolean s = true;
            public void focusGained(FocusEvent e) { if (s) { f.setText(""); f.setForeground(Color.DARK_GRAY); s = false; } }
            public void focusLost(FocusEvent e)   { if (f.getText().isEmpty()) { f.setText(ph); f.setForeground(PH_COLOR); s = true; } }
        });
    }

    private void setPasswordPlaceholder(JPasswordField f, String ph) {
        f.setEchoChar((char) 0); f.setText(ph); f.setForeground(PH_COLOR);
        f.addFocusListener(new FocusAdapter() {
            boolean s = true;
            public void focusGained(FocusEvent e) {
                if (s) { f.setText(""); f.setForeground(Color.DARK_GRAY); f.setEchoChar('●'); s = false; }
            }
            public void focusLost(FocusEvent e) {
                if (new String(f.getPassword()).isEmpty()) { f.setEchoChar((char)0); f.setText(ph); f.setForeground(PH_COLOR); s = true; }
            }
        });
    }

    private void styleButton(JButton b, Color bg) {
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setPreferredSize(new Dimension(400, 44));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.putClientProperty("FlatLaf.style", "arc: 999; borderWidth: 0; focusWidth: 0;");
    }

    private void switchTab(boolean showLogin) {
        if (showLogin) {
            cardLayout.show(pnlContent, "LOGIN");
            lblDangNhap.setForeground(Color.BLACK);
            lblDangNhap.setBorder(new MatteBorder(0, 0, 3, 0, BLUE));
            lblDangKy.setForeground(TEXT_MUTED);
            lblDangKy.setBorder(new EmptyBorder(0, 0, 3, 0));
        } else {
            cardLayout.show(pnlContent, "REGISTER");
            lblDangKy.setForeground(Color.BLACK);
            lblDangKy.setBorder(new MatteBorder(0, 0, 3, 0, BLUE));
            lblDangNhap.setForeground(TEXT_MUTED);
            lblDangNhap.setBorder(new EmptyBorder(0, 0, 3, 0));
        }
    }

    // Lấy giá trị thực của JTextField (trả về "" nếu đang hiển thị placeholder)
    private String getFieldValue(JTextField f, String placeholder) {
        String val = f.getText().trim();
        return val.equals(placeholder) ? "" : val;
    }

    // Lấy giá trị thực của JPasswordField (trả về "" nếu đang hiển thị placeholder)
    private String getPasswordValue(JPasswordField f, String placeholder) {
        String val = new String(f.getPassword()).trim();
        return val.equals(placeholder) ? "" : val;
    }

    private void addListeners() {
        // Chuyển tab
        lblDangNhap.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { switchTab(true); }
        });
        lblDangKy.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { switchTab(false); }
        });

        // Quên mật khẩu
        lblForgot.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { new ForgotPassword(); }
        });

        // Đăng nhập
        btnLogin.addActionListener(e -> {
            String user = getFieldValue(txtUsername, PH_USERNAME);
            String pass = getPasswordValue(txtPassword, PH_PASSWORD);

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            btnLogin.setEnabled(false);
            btnLogin.setText("Đang đăng nhập...");

            new SwingWorker<AccountModel, Void>() {
                @Override
                protected AccountModel doInBackground() {
                    return loginController.loginController(user, pass);
                }

                @Override
                protected void done() {
                    try {
                        AccountModel account = get();
                        if (account != null) {
                            SessionManager.login(account);
                            new MainPage(account);
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(LoginForm.this, "Tên đăng nhập hoặc mật khẩu không đúng!", "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(LoginForm.this, "Lỗi đăng nhập: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Đăng nhập");
                    }
                }
            }.execute();
        });

        // Đăng ký
        btnRegister.addActionListener(e -> {
            String user    = getFieldValue(txtRegUser, PH_USERNAME);
            String email   = getFieldValue(txtRegEmail, PH_EMAIL);
            String pass    = getPasswordValue(txtRegPass, PH_PASSWORD);
            String confirm = getPasswordValue(txtRegConfirm, PH_CONFIRM);
            String referral = getFieldValue(txtRegReferral, PH_REFERRAL);

            if (user.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!email.matches("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")) {
                JOptionPane.showMessageDialog(this, "Email không đúng định dạng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (pass.length() < 6) {
                JOptionPane.showMessageDialog(this, "Mật khẩu phải có ít nhất 6 ký tự!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            btnRegister.setEnabled(false);
            btnRegister.setText("Đang xử lý...");

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    return registerController.registerController(user, email, pass, referral);
                }

                @Override
                protected void done() {
                    try {
                        String result = get();
                        switch (result) {
                            case "SUCCESS":
                                JOptionPane.showMessageDialog(LoginForm.this, "Đăng ký thành công! Vui lòng đăng nhập.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                                switchTab(true);
                                break;
                            case "USERNAME_EXISTS":
                                JOptionPane.showMessageDialog(LoginForm.this, "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                break;
                            case "EMAIL_EXISTS":
                                JOptionPane.showMessageDialog(LoginForm.this, "Email này đã được sử dụng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                break;
                            case "REFERRAL_NOT_FOUND":
                                JOptionPane.showMessageDialog(LoginForm.this, "Mã giới thiệu không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                break;
                            default:
                                JOptionPane.showMessageDialog(LoginForm.this, "Đăng ký thất bại, vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                break;
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(LoginForm.this, "Lỗi đăng ký: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Tạo tài khoản");
                    }
                }
            }.execute();
        });
    }

    public static void main(String[] args) {
        try {
            // Cải thiện hiển thị font chữ (Anti-aliasing)
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            
            // Tối ưu cho Windows 11
            javax.swing.UIManager.put("TitlePane.useWindowDecorations", true);
            
            // Thiết lập màu nhấn
            Color accentColor = UIUtils.PRIMARY; 
            javax.swing.UIManager.put("Component.focusColor", accentColor);
            javax.swing.UIManager.put("Component.focusedBorderColor", accentColor);
            javax.swing.UIManager.put("Button.default.background", accentColor);
            javax.swing.UIManager.put("TabbedPane.selectedBackground", new Color(209, 250, 229)); // Emerald nhạt #D1FAE5
            javax.swing.UIManager.put("TabbedPane.underlineColor", accentColor);
            
            com.formdev.flatlaf.FlatLightLaf.setup();
            
            // Tùy chỉnh một chút font chữ mặc định của hệ thống
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
        } catch (Exception ignored) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored2) {}
        }
        SwingUtilities.invokeLater(LoginForm::new);
    }
}