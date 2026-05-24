package View.shared;

import DAO.AccountDAO;
import Model.AccountModel;
import Utils.PasswordUtils;
import Utils.SessionManager;
import View.LoginForm;
import View.MainPage;
import View.UIUtils;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {

    private final AccountModel account;
    private final JFrame parentFrame;
    private final AccountDAO accountDAO = new AccountDAO();

    public SettingsPanel(AccountModel account, JFrame parentFrame) {
        this.account = account;
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(UIUtils.APP_BG);
        buildUI();
    }

    private void buildUI() {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(UIUtils.APP_BG);
        inner.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = UIUtils.createHeading1("Cài đặt hệ thống");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(title);
        inner.add(Box.createVerticalStrut(30));

        inner.add(buildPasswordCard());
        inner.add(Box.createVerticalStrut(20));
        inner.add(buildThemeAndLogoutCard());

        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIUtils.APP_BG);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildPasswordCard() {
        JPanel card = createCardPanel("Bảo mật - Thay đổi mật khẩu");

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 15));
        form.setOpaque(false);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField txtOld = new JPasswordField();
        JPasswordField txtNew = new JPasswordField();
        JPasswordField txtConfirm = new JPasswordField();

        form.add(new JLabel("Mật khẩu hiện tại:"));
        form.add(txtOld);
        form.add(new JLabel("Mật khẩu mới:"));
        form.add(txtNew);
        form.add(new JLabel("Xác nhận mật khẩu mới:"));
        form.add(txtConfirm);

        card.add(form);
        card.add(Box.createVerticalStrut(15));

        JButton btnUpdate = new JButton("Cập nhật mật khẩu");
        btnUpdate.setBackground(UIUtils.PRIMARY);
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.putClientProperty("FlatLaf.style", "arc: 8;");
        btnUpdate.addActionListener(e -> {
            String oldP = new String(txtOld.getPassword());
            String newP = new String(txtNew.getPassword());
            String confP = new String(txtConfirm.getPassword());

            if (oldP.isEmpty() || newP.isEmpty() || confP.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!account.getAccount().getPasswordHash().equals(PasswordUtils.hash(oldP))) {
                JOptionPane.showMessageDialog(this, "Mật khẩu hiện tại không chính xác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newP.length() < 6) {
                JOptionPane.showMessageDialog(this, "Mật khẩu mới phải có ít nhất 6 ký tự!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newP.equals(confP)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = accountDAO.updatePassword(account.getAccount().getAccountId(), newP);
            if (success) {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                SessionManager.logout();
                new LoginForm();
                parentFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi đổi mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPanel.add(btnUpdate);

        card.add(btnPanel);
        return card;
    }

    private JPanel buildThemeAndLogoutCard() {
        JPanel card = createCardPanel("Hệ thống & Giao diện");

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.add(new JLabel("Chế độ giao diện: "));

        String[] themes = {"Giao diện Sáng (Light)", "Giao diện Tối (Dark)"};
        JComboBox<String> cboTheme = new JComboBox<>(themes);
        boolean isDark = UIManager.getLookAndFeel().getClass().getName().contains("Dark");
        cboTheme.setSelectedIndex(isDark ? 1 : 0);

        cboTheme.addActionListener(e -> {
            try {
                if (cboTheme.getSelectedIndex() == 0) {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    UIUtils.updateTheme(false);
                } else {
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    UIUtils.updateTheme(true);
                }
                parentFrame.dispose();
                new MainPage(account).setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        row1.add(cboTheme);
        card.add(row1);
        card.add(Box.createVerticalStrut(10));

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnLogout = new JButton("Đăng xuất tài khoản");
        btnLogout.setBackground(new Color(239, 68, 68));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.putClientProperty("FlatLaf.style", "arc: 8;");
        btnLogout.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                SessionManager.logout();
                new LoginForm();
                parentFrame.dispose();
            }
        });
        row2.add(btnLogout);
        card.add(row2);

        return card;
    }

    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 220, 235), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(800, 300));

        JLabel lblTitle = UIUtils.createHeading2(title);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(15));

        return card;
    }
}
