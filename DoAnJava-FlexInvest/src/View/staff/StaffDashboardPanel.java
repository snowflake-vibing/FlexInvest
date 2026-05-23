package View.staff;

import Controller.EkycController;
import Controller.WalletController;
import Model.AccountModel;
import View.UIUtils;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

/**
 * StaffDashboardPanel — màn hình tổng quan cho Staff.
 *
 * Hiển thị số lệnh đang chờ duyệt:
 *   - Nạp tiền PENDING
 *   - Rút tiền PENDING
 *   - eKYC PENDING
 *
 * Tự động refresh mỗi khi panel được mở (gọi loadData()).
 */
public class StaffDashboardPanel extends JPanel {

    private static final Color NAVY       = new Color(15, 40, 80);
    private static final Color BLUE       = new Color(0, 162, 232);
    private static final Color GREEN      = new Color(16, 185, 129);
    private static final Color ORANGE     = new Color(245, 158, 11);
    private static final Color YELLOW     = new Color(255, 193, 7);
    private static final Color RED        = new Color(239, 68, 68);
    private static final Color BG         = UIUtils.APP_BG;
    private static final Color CARD_BG    = UIUtils.CARD_BG;
    private static final Color TEXT_DARK  = UIUtils.TEXT_DARK;
    private static final Color TEXT_MUTED = UIUtils.TEXT_MUTED;

    private final AccountModel   account;
    private final WalletController walletCtrl = new WalletController();
    private final EkycController   ekycCtrl   = new EkycController();

    // Counter labels
    private JLabel lblDeposit;
    private JLabel lblWithdraw;
    private JLabel lblKyc;
    private JLabel lblSubDeposit;
    private JLabel lblSubWithdraw;
    private JLabel lblSubKyc;
    private JButton btnRefresh;
    
    private final int userId;
    private final Consumer<String> onNavigate; // Thống nhất dùng onNavigate của master

    public StaffDashboardPanel(AccountModel account, Consumer<String> onNavigate) {
        this.account = account;
        this.userId = (account != null && account.getUser() != null) ? account.getUser().getUserId() : 0;
        this.onNavigate = onNavigate != null ? onNavigate : id -> {}; // Tránh NullPointerException
        
        setLayout(new BorderLayout());
        setBackground(BG);
        build();
        loadData();
    }

    // =========================================================================
    //  Build UI
    // =========================================================================

    private void build() {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(BG);
        inner.setBorder(new EmptyBorder(28, 32, 28, 32));

        // ── Header ─────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Staff Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(NAVY);
        JLabel sub = new JLabel("Quản lý phê duyệt nạp · rút · eKYC");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        titles.add(title);
        titles.add(sub);

        btnRefresh = makeBtn("  Làm mới", BLUE, "\uf021");
        btnRefresh.addActionListener(e -> loadData());
        header.add(titles, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);
        inner.add(header);
        inner.add(Box.createVerticalStrut(28));

        // ── 3 Counter cards ─────────────────────────────────────────────────
        JPanel cards = new JPanel(new GridLayout(1, 3, 20, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        cards.setAlignmentX(LEFT_ALIGNMENT);

        lblDeposit  = new JLabel("—");
        lblWithdraw = new JLabel("—");
        lblKyc      = new JLabel("—");
        lblSubDeposit  = new JLabel("Đang tải…");
        lblSubWithdraw = new JLabel("Đang tải…");
        lblSubKyc      = new JLabel("Đang tải…");

        cards.add(buildCountCard("Nạp tiền",  lblDeposit,  lblSubDeposit,  BLUE));
        cards.add(buildCountCard("Rút tiền",  lblWithdraw, lblSubWithdraw, YELLOW));
        cards.add(buildCountCard("eKYC",      lblKyc,      lblSubKyc,      GREEN));
        inner.add(cards);
        inner.add(Box.createVerticalStrut(28));

        // ── Quick links ─────────────────────────────────────────────────────
        JLabel quickTitle = new JLabel("Truy cập nhanh");
        quickTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        quickTitle.setForeground(NAVY);
        quickTitle.setAlignmentX(LEFT_ALIGNMENT);
        inner.add(quickTitle);
        inner.add(Box.createVerticalStrut(12));

        JPanel quickRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        quickRow.setOpaque(false);
        quickRow.setAlignmentX(LEFT_ALIGNMENT);
        
        // Sử dụng text đẹp kèm mũi tên và truyền action an toàn
        quickRow.add(makeLinkBtn("Duyệt Nạp →", new Color(0, 120, 212), e -> onNavigate.accept("STAFF_DEPOSIT")));
        quickRow.add(makeLinkBtn("Duyệt Rút →", new Color(245, 100, 0),  e -> onNavigate.accept("STAFF_WITHDRAW")));
        quickRow.add(makeLinkBtn("Duyệt KYC →", new Color(16, 185, 129), e -> onNavigate.accept("STAFF_KYC")));
        inner.add(quickRow);
        
        inner.add(Box.createVerticalStrut(40));
        
        JPanel welcomePanel = new JPanel(new BorderLayout());
        UIUtils.styleCard(welcomePanel);
        welcomePanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        welcomePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        JLabel lblWelcomeTitle = new JLabel("Chào mừng Nhân viên Hệ thống");
        lblWelcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcomeTitle.setForeground(NAVY);
        lblWelcomeTitle.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel lblWelcomeSub = new JLabel("Hãy kiểm tra và duyệt các yêu cầu Nạp tiền, Rút tiền, và KYC từ khách hàng.");
        lblWelcomeSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblWelcomeSub.setForeground(new Color(100, 110, 120));
        lblWelcomeSub.setHorizontalAlignment(SwingConstants.CENTER);
        
        welcomePanel.add(lblWelcomeTitle, BorderLayout.NORTH);
        welcomePanel.add(lblWelcomeSub, BorderLayout.CENTER);
        inner.add(welcomePanel);

        // ScrollPane wrapper
        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildCountCard(String title, JLabel numLabel, JLabel subLabel, Color accent) {
        JPanel card = new JPanel();
        UIUtils.styleSummaryCard(card, accent);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        numLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        numLabel.setForeground(accent);
        numLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        subLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        subLabel.setForeground(TEXT_MUTED);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lbl);
        card.add(Box.createVerticalStrut(6));
        card.add(numLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subLabel);
        return card;
    }

    // =========================================================================
    //  Data Loading
    // =========================================================================

    public void loadData() {
        btnRefresh.setEnabled(false);
        btnRefresh.setText("⟳  Đang tải…");
        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() {
                int dep = walletCtrl.getPendingDeposits().size();
                int wit = walletCtrl.getPendingWithdraws().size();
                int kyc = ekycCtrl.getPendingKyc().size();
                return new int[]{dep, wit, kyc};
            }
            @Override
            protected void done() {
                try {
                    int[] counts = get();
                    lblDeposit.setText(String.valueOf(counts[0]));
                    lblWithdraw.setText(String.valueOf(counts[1]));
                    lblKyc.setText(String.valueOf(counts[2]));
                    lblSubDeposit.setText(counts[0] > 0 ? "cần xử lý ngay" : "Không có lệnh mới");
                    lblSubWithdraw.setText(counts[1] > 0 ? "cần xử lý ngay" : "Không có lệnh mới");
                    lblSubKyc.setText(counts[2] > 0 ? "hồ sơ chờ duyệt" : "Không có hồ sơ mới");
                    
                    // Đổi màu số khi có lệnh chờ
                    lblDeposit.setForeground(counts[0] > 0 ? RED : new Color(100, 150, 200));
                    lblWithdraw.setForeground(counts[1] > 0 ? RED : ORANGE);
                    lblKyc.setForeground(counts[2] > 0 ? RED : GREEN);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    btnRefresh.setEnabled(true);
                    btnRefresh.setText("  Làm mới");
                }
            }
        }.execute();
    }

    // =========================================================================
    //  Helpers
    // =========================================================================

    private JButton makeBtn(String text, Color bg, String faIcon) {
        // Load FontAwesome for icon
        java.awt.Font faFont;
        try {
            faFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
                new java.io.File("src/Resources/fa-solid-900.ttf")).deriveFont(13f);
        } catch (Exception ex) {
            faFont = new Font("Segoe UI", Font.PLAIN, 13);
        }

        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(bg);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 225, 245), 1, true),
            new EmptyBorder(6, 16, 6, 16)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Prepend FA icon using HTML
        if (faIcon != null && !faIcon.isEmpty()) {
            JLabel iconLbl = new JLabel(faIcon);
            iconLbl.setFont(faFont);
            iconLbl.setForeground(bg);
            b.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 0));
            b.add(iconLbl);
        }
        return b;
    }

    private JButton makeBtn(String text, Color bg) {
        return makeBtn(text, bg, null);
    }

    private JButton makeLinkBtn(String text, Color fg, ActionListener action) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(fg);
        b.setBackground(CARD_BG);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) {
            b.addActionListener(action);
        }
        return b;
    }
}