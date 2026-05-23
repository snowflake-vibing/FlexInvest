package View.customer;

import Controller.InvestmentController;
import DAO.EkycDAO;
import DAO.InvestmentDAO;
import DAO.WalletDAO;
import Model.*;
import Utils.DateUtils;
import Utils.InterestCalculator;
import View.OtpDialog;
import View.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * CustomerDashboardPanel — Màn hình chính sau khi login.
 *
 * Hiển thị:
 *  1. Cards tóm tắt: số dư ví, tổng đang tích lũy, số gói sắp đáo hạn
 *  2. Banner trạng thái eKYC với nút nộp hồ sơ nếu chưa KYC
 *  3. Bảng gói sắp đáo hạn trong 7 ngày tới (data thật từ DB)
 */
public class CustomerDashboardPanel extends JPanel {

    // ── Màu sắc ─────────────────────────────────────────────────────────────
    private static final Color NAVY        = new Color(15, 40, 80);
    private static final Color BLUE        = new Color(0, 162, 232);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color GREEN       = new Color(16, 185, 129);
    private static final Color YELLOW      = new Color(245, 158, 11);
    private static final Color ORANGE      = new Color(249, 115, 22);
    private static final Color RED         = new Color(239, 68, 68);
    private static final Color TEXT_DARK   = new Color(30, 30, 40);
    private static final Color TEXT_MUTED  = new Color(110, 115, 130);
    private static final Color BORDER_C    = new Color(210, 220, 235);
    private static final NumberFormat VND  = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // ── DAOs ─────────────────────────────────────────────────────────────────
    private final WalletDAO     walletDAO     = new WalletDAO();
    private final InvestmentDAO investDAO     = new InvestmentDAO();
    private final EkycDAO       ekycDAO       = new EkycDAO();

    private final AccountModel  account;
    private final int           userId;

    // ── UI components cần cập nhật ────────────────────────────────────────────
    private JLabel lblBalance;
    private JLabel lblTotalInvested;
    private JLabel lblMaturityCount;
    private JPanel kycBanner;
    private DefaultTableModel maturityTableModel;
    private JButton btnRefresh;

    public CustomerDashboardPanel(AccountModel account) {
        this.account = account;
        this.userId  = account.getUser().getUserId();
        // System.out.println("[DEBUG] CustomerDashboardPanel.init for userId=" + this.userId + " username=" + account.getAccount().getUsername());
        setLayout(new BorderLayout());
        setBackground(UIUtils.APP_BG);
        build();
        loadData();
    }

    // =========================================================================
    //  Build UI
    // =========================================================================

    private void build() {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(UIUtils.APP_BG);
        inner.setBorder(new EmptyBorder(24, 28, 24, 28));

        // ── Tiêu đề + Refresh ──────────────────────────────────────────────
        JPanel header = buildHeader();
        header.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(header);
        inner.add(Box.createVerticalStrut(20));

        // ── 3 Cards tóm tắt ────────────────────────────────────────────────
        inner.add(buildSummaryCards());
        inner.add(Box.createVerticalStrut(20));

        // ── Banner KYC ─────────────────────────────────────────────────────
        kycBanner = new JPanel(new BorderLayout());
        kycBanner.setOpaque(false);
        kycBanner.setAlignmentX(LEFT_ALIGNMENT);
        kycBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        inner.add(kycBanner);
        inner.add(Box.createVerticalStrut(20));

        // ── Bảng gói sắp đáo hạn ───────────────────────────────────────────
        inner.add(buildMaturitySection());

        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIUtils.APP_BG);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = UIUtils.createHeading1("Tổng quan tài khoản");
        JLabel sub = new JLabel("Xin chào, " + account.getAccount().getUsername() + " · " + LocalDate.now());
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        left.add(title);
        left.add(sub);

        btnRefresh = new JButton("Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setForeground(BLUE);
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 225, 245), 1, true),
            new EmptyBorder(6, 16, 6, 16)));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadData());

        p.add(left, BorderLayout.WEST);
        p.add(btnRefresh, BorderLayout.EAST);
        return p;
    }

    private JPanel buildSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        row.setAlignmentX(LEFT_ALIGNMENT);

        lblBalance      = new JLabel("—");
        lblTotalInvested = new JLabel("—");
        lblMaturityCount = new JLabel("—");

        row.add(buildCard("Số dư ví",          lblBalance,       BLUE,   "Số dư khả dụng hiện tại"));
        row.add(buildCard("Đang tích lũy",       lblTotalInvested, GREEN,  "Tổng gốc đang đầu tư ACTIVE"));
        row.add(buildCard("Sắp đáo hạn (7 ngày)", lblMaturityCount, YELLOW, "Số gói đáo hạn trong 7 ngày tới"));
        return row;
    }

    private JPanel buildCard(String title, JLabel valueLabel, Color accent, String tooltip) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        UIUtils.styleSummaryCard(card, accent);
        card.setToolTipText(tooltip);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(TEXT_DARK);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lbl);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        return card;
    }

    private JPanel buildMaturitySection() {
        JPanel section = new JPanel(new BorderLayout(0, 10));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = UIUtils.createHeading2("Gói sắp đáo hạn trong 7 ngày tới");
        section.add(title, BorderLayout.NORTH);

        String[] cols = {"Tên gói", "Số tiền gốc", "Ngày đáo hạn", "Còn lại"};
        maturityTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(maturityTableModel);
        UIUtils.styleTable(table);

        // Căn phải các cột tiền
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(right);

        // Highlight màu vàng nếu còn ≤ 3 ngày
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String remaining = (String) t.getValueAt(row, 3);
                try {
                    int days = Integer.parseInt(remaining.replaceAll("[^0-9]", ""));
                    c.setBackground(sel ? new Color(180, 210, 250) : (days <= 3 ? new Color(255, 245, 210) : Color.WHITE));
                } catch (Exception ex) {
                    c.setBackground(sel ? new Color(180, 210, 250) : Color.WHITE);
                }
                if (col == 1) ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 235), 1));
        sp.setPreferredSize(new Dimension(600, 160));
        section.add(sp, BorderLayout.CENTER);
        return section;
    }

    // =========================================================================
    //  Data Loading (SwingWorker để không block EDT)
    // =========================================================================

    public void loadData() {
        btnRefresh.setEnabled(false);
        btnRefresh.setText("Đang tải...");

        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() {
                Wallet          wallet   = walletDAO.getByUserId(userId);
                BigDecimal      total    = investDAO.getTotalActiveAmount(userId);
                List<Investment> soon    = investDAO.getMaturitySoon(userId, 7);
                Ekyc            kyc      = ekycDAO.getLatestByUserId(userId);
                return new Object[]{wallet, total, soon, kyc};
            }

            @Override
            protected void done() {
                try {
                    Object[] data  = get();
                    Wallet          wallet = (Wallet)          data[0];
                    BigDecimal      total  = (BigDecimal)      data[1];
                    @SuppressWarnings("unchecked")
                    List<Investment> soon  = (List<Investment>) data[2];
                    Ekyc            kyc    = (Ekyc)            data[3];

                    // System.out.println("[DEBUG] loadData results -> wallet=" + wallet + ", total=" + total + ", soon.size=" + (soon==null?"null":soon.size()) + ", ekyc=" + kyc);

                    // Cards
                    BigDecimal bal = (wallet != null) ? wallet.getAvailableBalance() : BigDecimal.ZERO;
                    lblBalance.setText(VND.format(bal) + " đ");
                    lblTotalInvested.setText(VND.format(total) + " đ");
                    lblMaturityCount.setText(soon.size() + " gói");

                    // KYC banner
                    updateKycBanner(kyc);

                    // Maturity table
                    maturityTableModel.setRowCount(0);
                    for (Investment inv : soon) {
                        LocalDate mat = DateUtils.toLocalDate(inv.getMaturityDate());
                        long days = (mat != null) ? ChronoUnit.DAYS.between(LocalDate.now(), mat) : -1;
                        String productName = "Gói #" + inv.getProductId();
                        maturityTableModel.addRow(new Object[]{
                            productName,
                            VND.format(inv.getInvestedAmount()) + " đ",
                            mat != null ? mat.toString() : "—",
                            days >= 0 ? days + " ngày" : "—"
                        });
                    }
                    if (soon.isEmpty()) {
                        maturityTableModel.addRow(new Object[]{"Không có gói nào sắp đáo hạn", "", "", ""});
                    }

                    // Debug: print UI-assigned texts and table rows
                    // System.out.println("[DEBUG] UI labels -> balance=" + lblBalance.getText()
                    //     + ", totalInvested=" + lblTotalInvested.getText()
                    //     + ", maturityCount=" + lblMaturityCount.getText()
                    //     + ", tableRows=" + maturityTableModel.getRowCount());

                    // Force full repaint — fix blank dashboard
                    CustomerDashboardPanel.this.revalidate();
                    CustomerDashboardPanel.this.repaint();
                    Container top = CustomerDashboardPanel.this.getTopLevelAncestor();
                    if (top != null) { top.revalidate(); top.repaint(); }

                    // Debug: component sizes
                    // System.out.println("[DEBUG] Panel size=" + CustomerDashboardPanel.this.getSize()
                    //     + ", visible=" + CustomerDashboardPanel.this.isVisible()
                    //     + ", showing=" + CustomerDashboardPanel.this.isShowing()
                    //     + ", parent=" + (CustomerDashboardPanel.this.getParent() != null ? CustomerDashboardPanel.this.getParent().getClass().getSimpleName() + " size=" + CustomerDashboardPanel.this.getParent().getSize() : "null"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    btnRefresh.setEnabled(true);
                    btnRefresh.setText("Làm mới");
                }
            }
        }.execute();
    }

    private void updateKycBanner(Ekyc kyc) {
        kycBanner.removeAll();

        Color   bg;
        Color   fg   = Color.WHITE;
        String  icon;
        String  msg;
        boolean showBtn = false;

        if (kyc == null) {
            bg = new Color(239, 68, 68);
            icon = "⚠";
            msg = "Bạn chưa xác minh danh tính (eKYC). Vui lòng nộp hồ sơ để có thể đầu tư.";
            showBtn = true;
        } else if (kyc.isApproved()) {
            bg = new Color(16, 185, 129);
            icon = "✔";
            msg = "eKYC đã được xác minh · " + kyc.getFullName();
        } else if (kyc.isPending()) {
            bg = new Color(245, 158, 11);
            icon = "⏳";
            msg = "Hồ sơ eKYC đang chờ xét duyệt. Tính năng đầu tư sẽ mở sau khi được phê duyệt.";
        } else {
            bg = new Color(239, 68, 68);
            icon = "✗";
            msg = "eKYC bị từ chối" + (kyc.getNote() != null ? " · " + kyc.getNote() : "") + ". Vui lòng nộp lại.";
            showBtn = true;
        }

        kycBanner.setBackground(bg);
        kycBanner.setOpaque(true); // Quan trọng: bật Opaque để vẽ màu nền
        kycBanner.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lbl = new JLabel(icon + "  " + msg);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(fg);
        kycBanner.add(lbl, BorderLayout.CENTER);

        if (showBtn) {
            JButton btn = new JButton("Nộp hồ sơ KYC");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setBackground(Color.WHITE);
            btn.setForeground(bg);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                    "Chức năng nộp hồ sơ KYC sẽ được tích hợp trong phiên bản tiếp theo.",
                    "Nộp hồ sơ eKYC", JOptionPane.INFORMATION_MESSAGE));
            kycBanner.add(btn, BorderLayout.EAST);
        }

        kycBanner.revalidate();
        kycBanner.repaint();
        
        // Buộc toàn bộ panel vẽ lại để xóa bóng mờ (ghosting artifacts)
        CustomerDashboardPanel.this.revalidate();
        CustomerDashboardPanel.this.repaint();
    }


}
