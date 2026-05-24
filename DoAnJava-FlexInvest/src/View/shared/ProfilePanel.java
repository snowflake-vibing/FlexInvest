package View.shared;

import DAO.EkycDAO;
import Model.AccountModel;
import Model.Ekyc;
import View.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.text.SimpleDateFormat;

public class ProfilePanel extends JPanel {

    private final AccountModel account;
    private final EkycDAO ekycDAO = new EkycDAO();
    private Ekyc kyc;
    private Font faFont;

    public ProfilePanel(AccountModel account) {
        this.account = account;
        try {
            faFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/Resources/fa-solid-900.ttf")).deriveFont(16f);
        } catch (Exception e) {
            faFont = new Font("Segoe UI", Font.PLAIN, 16);
        }
        kyc = ekycDAO.getLatestByUserId(account.getUser().getUserId());

        setLayout(new BorderLayout());
        setBackground(UIUtils.APP_BG);
        buildUI();
    }

    private void buildUI() {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(UIUtils.APP_BG);
        inner.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = UIUtils.createHeading1("Hồ sơ cá nhân");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(title);
        inner.add(Box.createVerticalStrut(25));

        inner.add(buildProfileHeaderCard());
        inner.add(Box.createVerticalStrut(20));
        inner.add(buildDetailsCard());

        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIUtils.APP_BG);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildProfileHeaderCard() {
        JPanel card = createCardPanel("");
        card.setLayout(new BorderLayout(30, 0));

        String displayName = (kyc != null && kyc.isApproved()) ? kyc.getFullName() : account.getAccount().getUsername();
        String initial = displayName.substring(0, 1).toUpperCase();

        JLabel lblAvatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 240, 255));
                g2.fill(new Ellipse2D.Double(0, 0, 100, 100));
                g2.setColor(UIUtils.PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 42));
                FontMetrics fm = g2.getFontMetrics();
                int x = (100 - fm.stringWidth(initial)) / 2;
                int y = ((100 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(initial, x, y);
                g2.dispose();
            }
        };
        lblAvatar.setPreferredSize(new Dimension(100, 100));
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel avatarWrap = new JPanel(new BorderLayout());
        avatarWrap.setOpaque(false);
        avatarWrap.add(lblAvatar, BorderLayout.CENTER);
        card.add(avatarWrap, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel lblName = UIUtils.createHeading1(displayName);
        lblName.setForeground(UIUtils.PRIMARY);

        String roleStr = account.getUser().getRoleId() == 1 ? "Quản trị viên hệ thống" :
                         account.getUser().getRoleId() == 2 ? "Nhân viên (Staff)" : "Khách hàng (Customer)";
        JLabel lblRole = new JLabel(roleStr);
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblRole.setForeground(Color.DARK_GRAY);

        JButton btnEdit = new JButton("Chỉnh sửa hồ sơ");
        btnEdit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setBackground(UIUtils.PRIMARY);
        btnEdit.putClientProperty("FlatLaf.style", "arc: 8;");
        btnEdit.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            EditProfileDialog dialog = new EditProfileDialog(parent, account, this);
            dialog.setVisible(true);
        });

        infoPanel.add(lblName);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(lblRole);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(btnEdit);

        card.add(infoPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildDetailsCard() {
        JPanel card = createCardPanel("Chi tiết hồ sơ");

        JPanel grid = new JPanel(new GridLayout(4, 2, 20, 20));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        grid.add(buildInfoRow("Email liên lạc:", account.getUser().getEmail()));

        String phoneStr = account.getUser().getPhone();
        grid.add(buildInfoRow("Số điện thoại:", (phoneStr == null || phoneStr.trim().isEmpty()) ? "Chưa cập nhật" : phoneStr));

        String idMask = "Chưa cập nhật";
        if (kyc != null && kyc.getIdNumber() != null && kyc.getIdNumber().length() > 6) {
            String idn = kyc.getIdNumber();
            idMask = idn.substring(0, 3) + "******" + idn.substring(idn.length() - 3);
        }
        grid.add(buildInfoRow("Số CCCD:", idMask));

        String dobStr = (kyc != null && kyc.getDateOfBirth() != null)
                ? new SimpleDateFormat("dd/MM/yyyy").format(kyc.getDateOfBirth())
                : "Chưa cập nhật";
        grid.add(buildInfoRow("Ngày sinh:", dobStr));

        String genderStr = (kyc != null && kyc.getGender() != null) ? kyc.getGender() : "Chưa cập nhật";
        grid.add(buildInfoRow("Giới tính:", genderStr));

        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(account.getUser().getCreatedAt());
        grid.add(buildInfoRow("Ngày tham gia:", dateStr));

        String refCode = account.getUser().getReferralCode() != null ? account.getUser().getReferralCode() : "Chưa có";
        grid.add(buildReferralRow(refCode));

        grid.add(buildEkycRow());

        card.add(grid);
        return card;
    }

    private JPanel buildInfoRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(UIUtils.TEXT_MUTED);

        JLabel val = new JLabel("<html><b>" + value + "</b></html>");
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        val.setForeground(UIUtils.TEXT_DARK);

        row.add(lbl);
        row.add(val);
        return row;
    }

    private JPanel buildReferralRow(String refCode) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel("Mã giới thiệu:");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(UIUtils.TEXT_MUTED);

        JLabel val = new JLabel("<html><b>" + refCode + "</b></html>");
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        val.setForeground(UIUtils.TEXT_DARK);

        row.add(lbl);
        row.add(val);

        if (!"Chưa có".equals(refCode)) {
            JButton btnCopy = new JButton("Sao chép");
            btnCopy.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnCopy.setForeground(UIUtils.PRIMARY);
            btnCopy.putClientProperty("FlatLaf.style", "arc: 8;");
            btnCopy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnCopy.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(refCode), null);
                    JOptionPane.showMessageDialog(ProfilePanel.this, "Đã sao chép mã giới thiệu: " + refCode, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            row.add(btnCopy);
        }
        return row;
    }

    private JPanel buildEkycRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel("Trạng thái eKYC:");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(UIUtils.TEXT_MUTED);

        String kycStatusStr = "Chưa xác minh";
        Color kycColor = new Color(239, 68, 68);
        if (kyc != null) {
            if (kyc.isApproved()) {
                kycStatusStr = "Đã xác minh";
                kycColor = new Color(16, 185, 129);
            } else if (kyc.isPending()) {
                kycStatusStr = "Đang chờ duyệt";
                kycColor = new Color(245, 158, 11);
            } else {
                kycStatusStr = "Bị từ chối";
            }
        }

        JLabel val = new JLabel("<html><b>" + kycStatusStr + "</b></html>");
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        val.setForeground(kycColor);

        row.add(lbl);
        row.add(val);
        return row;
    }

    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 220, 235), 1, true),
            new EmptyBorder(25, 30, 25, 30)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));

        if (!title.isEmpty()) {
            JLabel lblTitle = UIUtils.createHeading2(title);
            lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(lblTitle);
            card.add(Box.createVerticalStrut(20));
        }

        return card;
    }
}
