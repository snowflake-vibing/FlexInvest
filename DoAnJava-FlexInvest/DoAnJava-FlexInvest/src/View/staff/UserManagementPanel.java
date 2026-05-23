package View.staff;

import DAO.AccountDAO;
import DAO.UserDAO;
import DAO.WalletDAO;
import Model.Account;
import Model.AccountModel;
import Model.User;
import Model.Wallet;
import View.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import ConnectDB.ConnectionOracle;

/**
 * UserManagementPanel — Admin panel quản lý người dùng.
 *
 * Tính năng:
 *  - Bảng tất cả user với filter theo Role + KYC
 *  - Panel chi tiết user bên phải: thông tin, số dư, giao dịch gần đây
 *  - Nút Khóa/Mở Khóa tài khoản
 */
public class UserManagementPanel extends JPanel {

    // ── Bộ màu chuẩn ──────────────────────────────────────────────────────────
    private static final Color NAVY      = new Color(15, 40, 80);
    private static final Color BLUE      = new Color(0, 162, 232);
    private static final Color GREEN     = new Color(16, 185, 129);
    private static final Color RED       = new Color(239, 68, 68);
    private static final Color BG        = UIUtils.APP_BG;
    private static final Color CARD_BG   = UIUtils.CARD_BG;
    private static final Color TEXT_DARK = UIUtils.TEXT_DARK;
    private static final Color TEXT_MUTED = UIUtils.TEXT_MUTED;
    private static final Color BORDER_C  = UIUtils.BORDER_COLOR;
    private static final NumberFormat VND = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private final AccountModel adminAccount;
    private final UserDAO      userDAO   = new UserDAO();
    private final AccountDAO   accDAO    = new AccountDAO();
    private final WalletDAO    walletDAO = new WalletDAO();

    // ── Left panel controls ──────────────────────────────────────────────────
    private DefaultTableModel    tableModel;
    private JTable               table;
    private JComboBox<String>    cbRoleFilter;
    private JComboBox<String>    cbKycFilter;
    private JButton              btnRefresh;

    // ── Right panel (detail) ─────────────────────────────────────────────────
    private JPanel               detailPanel;
    private JLabel               lblDetailName;
    private JLabel               lblDetailEmail;
    private JLabel               lblDetailStatus;
    private JLabel               lblDetailKyc;
    private JLabel               lblDetailWallet;
    private DefaultTableModel    historyTableModel;
    private User                 selectedUser;
    private JButton              btnToggleStatus;

    private List<User> allUsers;

    public UserManagementPanel(AccountModel adminAccount) {
        this.adminAccount = adminAccount;
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        build();
        loadData();
    }

    // =========================================================================
    //  Build UI
    // =========================================================================

    private void build() {
        JPanel inner = new JPanel(new BorderLayout(16, 0));
        inner.setBackground(BG);
        inner.setBorder(new EmptyBorder(24, 28, 24, 28));

        inner.add(buildLeft(),   BorderLayout.CENTER);
        inner.add(buildRight(),  BorderLayout.EAST);

        add(inner, BorderLayout.CENTER);
    }

    private JPanel buildLeft() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        // ── Header ─────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleGroup = new JPanel();
        titleGroup.setOpaque(false);
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Quản lý Người Dùng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(NAVY);
        JLabel sub = new JLabel("Xem, khóa/mở khóa và lọc tài khoản hệ thống");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        titleGroup.add(title);
        titleGroup.add(sub);

        btnRefresh = outlineBtn("Làm mới");
        btnRefresh.addActionListener(e -> loadData());

        header.add(titleGroup, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        // ── Filter bar ─────────────────────────────────────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        UIUtils.styleCard(filterBar);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C, 1),
            new EmptyBorder(0, 8, 0, 8)));

        JLabel lRole = new JLabel("Role:");
        lRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lRole.setForeground(TEXT_MUTED);
        filterBar.add(lRole);

        cbRoleFilter = new JComboBox<>(new String[]{"All", "Admin", "Staff", "Customer"});
        cbRoleFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbRoleFilter.addActionListener(e -> applyFilters());
        filterBar.add(cbRoleFilter);

        JLabel lKyc = new JLabel("  KYC:");
        lKyc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lKyc.setForeground(TEXT_MUTED);
        filterBar.add(lKyc);

        cbKycFilter = new JComboBox<>(new String[]{"All", "APPROVED", "PENDING", "REJECTED", "UNSUBMITTED"});
        cbKycFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKycFilter.addActionListener(e -> applyFilters());
        filterBar.add(cbKycFilter);

        p.add(filterBar, BorderLayout.CENTER);

        // ── Table ──────────────────────────────────────────────────────────
        String[] cols = {"ID", "Email", "Role", "Trạng thái", "KYC"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showUserDetails();
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_C, 1));
        scroll.getViewport().setBackground(CARD_BG);
        p.add(scroll, BorderLayout.SOUTH);

        // Dùng BorderLayout.SOUTH tạm — layout lại bằng BoxLayout
        JPanel tableWrap = new JPanel(new BorderLayout(0, 12));
        tableWrap.setOpaque(false);
        tableWrap.add(filterBar, BorderLayout.NORTH);
        tableWrap.add(scroll,    BorderLayout.CENTER);

        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setOpaque(false);
        left.add(header,    BorderLayout.NORTH);
        left.add(tableWrap, BorderLayout.CENTER);
        return left;
    }

    private JPanel buildRight() {
        detailPanel = new JPanel(new BorderLayout(0, 12));
        UIUtils.styleCard(detailPanel);
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C, 1),
            new EmptyBorder(20, 20, 20, 20)));
        detailPanel.setPreferredSize(new Dimension(340, 0));

        // ── Info section ───────────────────────────────────────────────────
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel detailHeader = new JLabel("Chi tiết người dùng");
        detailHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        detailHeader.setForeground(NAVY);
        infoPanel.add(detailHeader);
        infoPanel.add(Box.createVerticalStrut(16));

        lblDetailName   = infoLabel("Chọn một user để xem", Font.BOLD, 16, TEXT_DARK);
        lblDetailEmail  = infoLabel("Email: —",   Font.PLAIN, 12, TEXT_MUTED);
        lblDetailStatus = infoLabel("Status: —",  Font.PLAIN, 12, TEXT_MUTED);
        lblDetailKyc    = infoLabel("KYC: —",     Font.PLAIN, 12, TEXT_MUTED);
        lblDetailWallet = infoLabel("Số dư: —",   Font.BOLD,  13, BLUE);

        for (JLabel l : new JLabel[]{lblDetailName, lblDetailEmail, lblDetailStatus, lblDetailKyc, lblDetailWallet}) {
            infoPanel.add(l);
            infoPanel.add(Box.createVerticalStrut(8));
        }

        infoPanel.add(Box.createVerticalStrut(8));

        btnToggleStatus = new JButton("Khóa / Mở Khóa");
        btnToggleStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggleStatus.setBackground(RED);
        btnToggleStatus.setForeground(Color.WHITE);
        btnToggleStatus.setBorderPainted(false);
        btnToggleStatus.setFocusPainted(false);
        btnToggleStatus.setOpaque(true);
        btnToggleStatus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggleStatus.setEnabled(false);
        btnToggleStatus.addActionListener(e -> toggleUserStatus());
        infoPanel.add(btnToggleStatus);

        detailPanel.add(infoPanel, BorderLayout.NORTH);

        // ── Recent transactions ────────────────────────────────────────────
        JPanel historyPanel = new JPanel(new BorderLayout(0, 8));
        historyPanel.setOpaque(false);

        JLabel histTitle = new JLabel("Giao dịch gần đây");
        histTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        histTitle.setForeground(NAVY);
        historyPanel.add(histTitle, BorderLayout.NORTH);

        historyTableModel = new DefaultTableModel(new String[]{"Ngày", "Loại", "Số tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable historyTable = new JTable(historyTableModel);
        UIUtils.styleTable(historyTable);

        JScrollPane histScroll = new JScrollPane(historyTable);
        histScroll.setBorder(BorderFactory.createLineBorder(BORDER_C, 1));
        histScroll.setPreferredSize(new Dimension(0, 200));
        historyPanel.add(histScroll, BorderLayout.CENTER);

        detailPanel.add(historyPanel, BorderLayout.CENTER);

        return detailPanel;
    }

    // =========================================================================
    //  Data Loading
    // =========================================================================

    public void loadData() {
        btnRefresh.setEnabled(false);
        btnRefresh.setText("Đang tải...");
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override protected List<User> doInBackground() {
                return userDAO.getAllUsers();
            }
            @Override protected void done() {
                try {
                    allUsers = get();
                    applyFilters();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    btnRefresh.setEnabled(true);
                    btnRefresh.setText("Làm mới");
                }
            }
        };
        worker.execute();
    }

    private void applyFilters() {
        if (allUsers == null) return;
        tableModel.setRowCount(0);

        String roleFilter = (String) cbRoleFilter.getSelectedItem();
        String kycFilter  = (String) cbKycFilter.getSelectedItem();

        for (User u : allUsers) {
            String roleName = u.getRoleId() == 1 ? "Admin"
                            : u.getRoleId() == 2 ? "Staff" : "Customer";
            if (!"All".equals(roleFilter) && !roleName.equals(roleFilter)) continue;

            String kycStatus = getKycStatus(u.getUserId());
            if (!"All".equals(kycFilter) && !kycStatus.equals(kycFilter)) continue;

            tableModel.addRow(new Object[]{u.getUserId(), u.getEmail(), roleName, u.getStatus(), kycStatus});
        }
    }

    /**
     * Lấy trạng thái KYC của user.
     * TODO: Chuyển query này sang EkycDAO hoặc UserDAO.
     */
    private String getKycStatus(int userId) {
        String status = "UNSUBMITTED";
        try (Connection con = ConnectionOracle.getOracleConnection();
             var ps = con.prepareStatement(
                 "SELECT verified_status FROM (" +
                 "  SELECT verified_status FROM EKYC WHERE user_id = ? AND is_deleted = 0" +
                 "  ORDER BY created_at DESC" +
                 ") WHERE ROWNUM = 1")) {
            ps.setInt(1, userId);
            var rs = ps.executeQuery();
            if (rs.next()) status = rs.getString(1);
        } catch (Exception e) {
            System.err.println("[UserManagementPanel.getKycStatus] " + e.getMessage());
        }
        return status;
    }

    private void showUserDetails() {
        int r = table.getSelectedRow();
        if (r < 0) { btnToggleStatus.setEnabled(false); return; }

        int userId = (int) tableModel.getValueAt(r, 0);
        selectedUser = allUsers.stream()
            .filter(u -> u.getUserId() == userId).findFirst().orElse(null);
        if (selectedUser == null) return;

        Account acc = accDAO.getByUserId(userId);
        lblDetailName.setText(acc != null ? acc.getUsername() : "Unknown");
        lblDetailEmail.setText("Email: " + selectedUser.getEmail());
        lblDetailStatus.setText("Trạng thái: " + selectedUser.getStatus());
        lblDetailKyc.setText("KYC: " + getKycStatus(userId));

        Wallet w = walletDAO.getByUserId(userId);
        lblDetailWallet.setText("Số dư ví: " + (w != null
            ? VND.format(w.getAvailableBalance()) + " VNĐ" : "0 VNĐ"));

        btnToggleStatus.setEnabled(true);
        boolean active = "ACTIVE".equals(selectedUser.getStatus());
        btnToggleStatus.setText(active ? "Khóa Tài Khoản" : "Mở Khóa");
        btnToggleStatus.setBackground(active ? RED : GREEN);

        // Load recent transactions
        // TODO: Chuyển query này sang WalletDAO.getRecentTransactions(walletId, limit)
        historyTableModel.setRowCount(0);
        if (w != null) {
            try (Connection con = ConnectionOracle.getOracleConnection();
                 var ps = con.prepareStatement(
                     "SELECT created_at, type_code, amount FROM (" +
                     "  SELECT created_at, type_code, amount FROM TRANSACTION" +
                     "  WHERE wallet_id = ? AND is_deleted = 0" +
                     "  ORDER BY created_at DESC" +
                     ") WHERE ROWNUM <= 5")) {
                ps.setInt(1, w.getWalletId());
                var rs = ps.executeQuery();
                while (rs.next()) {
                    historyTableModel.addRow(new Object[]{
                        rs.getTimestamp(1),
                        rs.getString(2),
                        VND.format(rs.getBigDecimal(3)) + " đ"
                    });
                }
                if (historyTableModel.getRowCount() == 0) {
                    historyTableModel.addRow(new Object[]{"—", "Chưa có giao dịch", "—"});
                }
            } catch (Exception e) {
                System.err.println("[UserManagementPanel.showUserDetails] " + e.getMessage());
            }
        }
    }

    private void toggleUserStatus() {
        if (selectedUser == null) return;
        String newStatus = "ACTIVE".equals(selectedUser.getStatus()) ? "LOCKED" : "ACTIVE";
        if (userDAO.updateStatus(selectedUser.getUserId(), newStatus)) {
            selectedUser.setStatus(newStatus);
            lblDetailStatus.setText("Trạng thái: " + newStatus);
            boolean active = "ACTIVE".equals(newStatus);
            btnToggleStatus.setText(active ? "Khóa Tài Khoản" : "Mở Khóa");
            btnToggleStatus.setBackground(active ? RED : GREEN);
            int rowIdx = table.getSelectedRow();
            if (rowIdx >= 0) tableModel.setValueAt(newStatus, rowIdx, 3);
            JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái tài khoản thành công!");
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi cập nhật trạng thái.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    //  UI helpers
    // =========================================================================

    private JButton outlineBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(BLUE);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C, 1, true),
            new EmptyBorder(6, 16, 6, 16)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel infoLabel(String text, int style, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(color);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }
}
