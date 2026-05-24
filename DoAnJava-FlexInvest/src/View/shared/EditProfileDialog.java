package View.shared;

import DAO.AccountDAO;
import DAO.UserDAO;
import Model.AccountModel;
import View.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditProfileDialog extends JDialog {

    private JTextField txtUsername;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private AccountModel account;
    private ProfilePanel parentPanel;

    private AccountDAO accountDAO = new AccountDAO();
    private UserDAO userDAO = new UserDAO();

    public EditProfileDialog(JFrame parentFrame, AccountModel account, ProfilePanel parentPanel) {
        super(parentFrame, "Chỉnh sửa hồ sơ", true);
        this.account = account;
        this.parentPanel = parentPanel;

        setSize(400, 300);
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.APP_BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIUtils.APP_BG);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblUsername = new JLabel("Tên hiển thị (Username):");
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername = new JTextField(account.getAccount().getUsername());
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtEmail = new JTextField(account.getUser().getEmail());
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel lblPhone = new JLabel("Số điện thoại:");
        lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        String phoneStr = account.getUser().getPhone();
        txtPhone = new JTextField(phoneStr != null ? phoneStr : "");
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        content.add(lblUsername);
        content.add(Box.createVerticalStrut(5));
        content.add(txtUsername);
        content.add(Box.createVerticalStrut(15));
        content.add(lblEmail);
        content.add(Box.createVerticalStrut(5));
        content.add(txtEmail);
        content.add(Box.createVerticalStrut(15));
        content.add(lblPhone);
        content.add(Box.createVerticalStrut(5));
        content.add(txtPhone);

        add(content, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(UIUtils.APP_BG);
        bottom.setBorder(new EmptyBorder(10, 20, 20, 20));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Lưu");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setBackground(UIUtils.PRIMARY);
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(new SaveAction());

        bottom.add(btnCancel);
        bottom.add(btnSave);
        add(bottom, BorderLayout.SOUTH);
    }

    private class SaveAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String newUsername = txtUsername.getText().trim();
            String newEmail = txtEmail.getText().trim();
            String newPhone = txtPhone.getText().trim();

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(EditProfileDialog.this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!newEmail.matches("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")) {
                JOptionPane.showMessageDialog(EditProfileDialog.this, "Email không đúng định dạng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newPhone.isEmpty() && !newPhone.matches("^\\d{10,11}$")) {
                JOptionPane.showMessageDialog(EditProfileDialog.this, "Số điện thoại không hợp lệ (10-11 số)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newUsername.equals(account.getAccount().getUsername())) {
                if (accountDAO.getByUsername(newUsername) != null) {
                    JOptionPane.showMessageDialog(EditProfileDialog.this, "Tên đăng nhập đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (!newEmail.equals(account.getUser().getEmail())) {
                if (userDAO.getUserByEmail(newEmail) != null) {
                    JOptionPane.showMessageDialog(EditProfileDialog.this, "Email đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            account.getAccount().setUsername(newUsername);
            account.getUser().setEmail(newEmail);
            account.getUser().setPhone(newPhone.isEmpty() ? null : newPhone);

            boolean okAcc = accountDAO.update(account.getAccount());
            boolean okUser = userDAO.updateUser(account.getUser());

            if (okAcc && okUser) {
                JOptionPane.showMessageDialog(EditProfileDialog.this, "Cập nhật hồ sơ thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                if (parentPanel != null) {
                    SwingUtilities.getWindowAncestor(parentPanel).dispose();
                    new View.MainPage(account).setVisible(true);
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(EditProfileDialog.this, "Cập nhật thất bại, vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
