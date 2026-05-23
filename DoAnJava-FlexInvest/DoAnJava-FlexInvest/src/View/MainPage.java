package View;

import Controller.NotificationController;
import Model.AccountModel;
import Utils.SessionManager;
import View.customer.CustomerDashboardPanel;
import View.customer.MissionPanel;
import View.customer.MoneyManagementPanel;
import View.customer.MyInvestmentsPanel;
import View.customer.SavingsProductListPanel;
import View.customer.WalletPanel;
import View.permission.PermissionManagementView;
import View.staff.AdminDashboardPanel;
import View.staff.DepositApprovalPanel;
import View.staff.EkycApprovalPanel;
import View.staff.MissionManagementPanel;
import View.staff.SavingsProductPanel;
import View.staff.StaffDashboardPanel;
import View.staff.TransactionHistoryPanel;
import View.staff.UserManagementPanel;
import View.staff.WithdrawApprovalPanel;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MainPage — Cửa sổ chính sau khi đăng nhập thành công.
 *
 * Cấu trúc:
 *   TopBar   — logo, thông tin user, nút logout
 *   Sidebar  — navigation theo role:
 *              Customer: Dashboard / Gói Đầu Tư / Khoản Của Tôi
 *              Admin: (kế thừa) + Phân Quyền
 *   Content  — CardLayout chứa các panel, lazy-init khi lần đầu click
 */
public class MainPage extends JFrame {

    private final AccountModel currentAccount;
    private final boolean      isAdmin;   // roleId == 1
    private final boolean      isStaff;   // roleId == 2

    // ── Màu ─────────────────────────────────────────────────────────────────────────
    private static final Color PRIMARY    = UIUtils.PRIMARY;            // #34D399 light green
    private static final Color SIDEBAR    = new Color(6, 78, 59);       // #064E3B dark emerald
    private static final Color SIDEBAR_HOVER = new Color(16, 185, 129); // #10B981 hover
    private static final Color BG         = UIUtils.APP_BG;             // #F0FDF4 green tint
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color RED        = new Color(239, 68,  68);
    private static final Color TEXT_DARK  = new Color(30,  30,  40);
    private static final Color TEXT_MUTED = new Color(110, 115, 130);

    // ── Content area ─────────────────────────────────────────────────────────
    private final CardLayout  cardLayout = new CardLayout();
    private JPanel            contentPane;

    // Lazy-init panels — Customer
    private CustomerDashboardPanel  dashboardPanel;
    private SavingsProductListPanel productsPanel;
    private MyInvestmentsPanel      myInvestmentsPanel;
    private WalletPanel             walletPanel;
    private MoneyManagementPanel    moneyPanel;
    private MissionPanel            missionPanel;

    // Lazy-init panels — Staff
    private StaffDashboardPanel     staffDashboardPanel;
    private DepositApprovalPanel    depositApprovalPanel;
    private WithdrawApprovalPanel   withdrawApprovalPanel;
    private EkycApprovalPanel       ekycApprovalPanel;

    // Panel ID constants — Customer
    private static final String CARD_DASHBOARD   = "DASHBOARD";
    private static final String CARD_PRODUCTS    = "PRODUCTS";
    private static final String CARD_INVESTMENTS = "INVESTMENTS";
    private static final String CARD_WALLET      = "WALLET";
    private static final String CARD_MONEY       = "MONEY";
    private static final String CARD_MISSION     = "MISSION";
    private static final String CARD_SETTINGS    = "SETTINGS";

    // Notification
    private final NotificationController notifCtrl = new NotificationController();
    private JButton  bellBtn;
    private JLabel   badgeLbl;

    // Lazy-init panels — Admin
    private AdminDashboardPanel     adminDashboardPanel;
    private SavingsProductPanel     adminProductsPanel;
    private UserManagementPanel     adminUsersPanel;
    private TransactionHistoryPanel txHistoryPanel;
    private MissionManagementPanel  missionMgmtPanel;

    // Panel ID constants — Admin
    private static final String CARD_ADMIN_DASH     = "ADMIN_DASH";
    private static final String CARD_ADMIN_PRODUCTS = "ADMIN_PRODUCTS";
    private static final String CARD_ADMIN_USERS    = "ADMIN_USERS";
    private static final String CARD_ADMIN_TX       = "ADMIN_TX";
    private static final String CARD_ADMIN_MISSION  = "ADMIN_MISSION";

    // Panel ID constants — Staff
    private static final String CARD_STAFF_DASH     = "STAFF_DASH";
    private static final String CARD_STAFF_DEPOSIT  = "STAFF_DEPOSIT";
    private static final String CARD_STAFF_WITHDRAW = "STAFF_WITHDRAW";
    private static final String CARD_STAFF_KYC      = "STAFF_KYC";

    // Currently active menu item panel reference
    private JPanel activeItem;

    public MainPage(AccountModel account) {
        this.currentAccount = account;
        this.isAdmin = (account.getUser().getRoleId() == 1);
        this.isStaff = (account.getUser().getRoleId() == 2);
        initComponents();
        setVisible(true);
    }

    // =========================================================================
    //  Init
    // =========================================================================

    private void initComponents() {
        setTitle("FlexInvest — "
            + (isAdmin ? "Admin" : isStaff ? "Staff" : "")
            + " · " + currentAccount.getAccount().getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(960, 600));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        setContentPane(root);

        // Content CardLayout
        contentPane = new JPanel(cardLayout);
        contentPane.setBackground(BG);

        // Add placeholder panels for cards not yet loaded
        contentPane.add(new JPanel(), CARD_SETTINGS);

        root.add(buildTopBar(),  BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(contentPane,    BorderLayout.CENTER);

        // Open default panel
        if (isAdmin) showPanel(CARD_ADMIN_DASH);
        else if (isStaff) showPanel(CARD_STAFF_DASH);
        else         showPanel(CARD_DASHBOARD);
    }

    // =========================================================================
    //  Top Bar
    // =========================================================================

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setPreferredSize(new Dimension(1200, 58));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_COLOR),
            new EmptyBorder(0, 24, 0, 24)
        ));

        // Logo
        JLabel logo = new JLabel("FLEXINVEST");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(PRIMARY);

        // Right — bell + user info + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        String role = isAdmin ? "Admin" : isStaff ? "Staff" : "Thành viên";
        JButton btnUser = new JButton(" " + currentAccount.getAccount().getUsername() + " (" + role + ") ");
        btnUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUser.setForeground(TEXT_DARK);
        btnUser.setBackground(new Color(209, 250, 229)); // Emerald 100
        btnUser.putClientProperty("FlatLaf.style", "arc: 999;");
        btnUser.setBorderPainted(false);
        btnUser.setFocusPainted(false);
        btnUser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnUser.addActionListener(e -> showProfileDialog());

        // ── Bell button with badge ──────────────────────────────────────────
        JPanel bellWrapper = new JPanel(null);
        bellWrapper.setOpaque(false);
        bellWrapper.setPreferredSize(new Dimension(42, 42));

        bellBtn = new JButton("🔔");
        bellBtn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        bellBtn.setBackground(new Color(245, 247, 250));
        bellBtn.setForeground(TEXT_DARK);
        bellBtn.setBorderPainted(false);
        bellBtn.setFocusPainted(false);
        bellBtn.setOpaque(true);
        bellBtn.setBounds(0, 8, 36, 36);
        bellBtn.putClientProperty("FlatLaf.style", "arc: 999;");
        bellBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bellBtn.addActionListener(e -> openNotificationDialog());

        badgeLbl = new JLabel("");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        badgeLbl.setForeground(Color.WHITE);
        badgeLbl.setBackground(RED);
        badgeLbl.setOpaque(true);
        badgeLbl.setHorizontalAlignment(SwingConstants.CENTER);
        badgeLbl.setBounds(20, 4, 18, 16);
        badgeLbl.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        badgeLbl.setVisible(false);

        bellWrapper.add(bellBtn);
        bellWrapper.add(badgeLbl);

        JButton btnLogout = styledBtn("Đăng xuất", RED);
        btnLogout.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                SessionManager.logout();
                new LoginForm();
                dispose();
            }
        });

        right.add(bellWrapper);
        right.add(btnUser);
        right.add(btnLogout);
        bar.add(logo, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        // Cập nhật badge ngay sau khi build
        refreshNotificationBadge();
        return bar;
    }

    private void refreshNotificationBadge() {
        if (isAdmin || isStaff) return;  // badge chỉ cho Customer
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() {
                return notifCtrl.countUnread(currentAccount.getUser().getUserId());
            }
            @Override protected void done() {
                try {
                    int count = get();
                    if (count > 0) {
                        badgeLbl.setText(count > 99 ? "99+" : String.valueOf(count));
                        badgeLbl.setVisible(true);
                    } else {
                        badgeLbl.setVisible(false);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void openNotificationDialog() {
        NotificationDialog.open(
            this, bellBtn,
            currentAccount.getUser().getUserId(),
            notifCtrl,
            this::refreshNotificationBadge  // callback sau khi đọc
        );
    }

    private void showProfileDialog() {
        JDialog dlg = new JDialog(this, "Thông tin Tài Khoản", true);
        dlg.setSize(380, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(UIUtils.APP_BG);
        
        JPanel pnl = new JPanel(new GridLayout(6, 1, 10, 10));
        UIUtils.styleCard(pnl);
        
        Model.User u = currentAccount.getUser();
        Model.Account a = currentAccount.getAccount();
        
        pnl.add(UIUtils.createHeading2("Username: " + a.getUsername()));
        pnl.add(UIUtils.createMutedText("Email: " + u.getEmail()));
        
        String roleStr = isAdmin ? "Admin" : isStaff ? "Staff" : "Thành viên";
        pnl.add(new JLabel("Vai trò: " + roleStr));
        pnl.add(new JLabel("Mã giới thiệu: " + (u.getReferralCode() != null ? u.getReferralCode() : "Chưa có")));
        pnl.add(new JLabel("Ngày tham gia: " + u.getCreatedAt()));
        
        JButton btnClose = new JButton("Đóng");
        btnClose.putClientProperty("JButton.buttonType", "roundRect");
        btnClose.addActionListener(e -> dlg.dispose());
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));
        wrapper.setBackground(UIUtils.APP_BG);
        wrapper.add(pnl, BorderLayout.CENTER);
        
        dlg.add(wrapper, BorderLayout.CENTER);
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT)); 
        bot.setBackground(UIUtils.APP_BG);
        bot.add(btnClose);
        dlg.add(bot, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // =========================================================================
    //  Sidebar
    // =========================================================================

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(SIDEBAR);
        side.setPreferredSize(new Dimension(210, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(20, 0, 20, 0));

        JPanel activeItem;

        if (isStaff) {
            // ── Staff sidebar ─────────────────────────────────────────────
            JPanel iDash     = buildMenuItem("Dashboard",         CARD_STAFF_DASH,     true);
            JPanel iDeposit  = buildMenuItem("Duyệt Nạp Tiền",   CARD_STAFF_DEPOSIT,  false);
            JPanel iWithdraw = buildMenuItem("Duyệt Rút Tiền",   CARD_STAFF_WITHDRAW, false);
            JPanel iKyc      = buildMenuItem("Duyệt eKYC",       CARD_STAFF_KYC,      false);

            side.add(iDash);
            side.add(Box.createVerticalStrut(2));
            side.add(iDeposit);
            side.add(Box.createVerticalStrut(2));
            side.add(iWithdraw);
            side.add(Box.createVerticalStrut(2));
            side.add(iKyc);

            activeItem = iDash;
            setActiveStyle(iDash, true);
        } else {
            // ── Customer sidebar ──────────────────────────────────────────
            JPanel itemDashboard   = buildMenuItem("Dashboard",         CARD_DASHBOARD,   true);
            JPanel itemProducts    = buildMenuItem("Gói Đầu Tư",        CARD_PRODUCTS,    false);
            JPanel itemInvestments = buildMenuItem("Khoản Của Tôi",     CARD_INVESTMENTS, false);
            JPanel itemWallet      = buildMenuItem("Ví của tôi",         CARD_WALLET,      false);
            JPanel itemMoney       = buildMenuItem("Quản lý Tiền",       CARD_MONEY,       false);
            JPanel itemMission     = buildMenuItem("Nhiệm vụ & Token",  CARD_MISSION,     false);

            side.add(itemDashboard);
            side.add(Box.createVerticalStrut(2));
            side.add(itemProducts);
            side.add(Box.createVerticalStrut(2));
            side.add(itemInvestments);
            side.add(Box.createVerticalStrut(2));
            side.add(itemWallet);
            side.add(Box.createVerticalStrut(2));
            side.add(itemMoney);
            side.add(Box.createVerticalStrut(2));
            side.add(itemMission);

            activeItem = itemDashboard;
            setActiveStyle(itemDashboard, true);

            if (isAdmin) {
                side.add(Box.createVerticalStrut(12));
                side.add(buildSeparator());
                side.add(Box.createVerticalStrut(8));

                JPanel adminLbl = new JPanel(new BorderLayout());
                adminLbl.setOpaque(false);
                adminLbl.setMaximumSize(new Dimension(210, 26));
                JLabel lbl = new JLabel("  QUẢN TRỊ");
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
                lbl.setForeground(new Color(167, 243, 208)); // Emerald 200 trên nền sidebar tối
                adminLbl.add(lbl, BorderLayout.CENTER);
                side.add(adminLbl);

                JPanel iAdminDash   = buildMenuItem("Tổng quan (Admin)",  CARD_ADMIN_DASH,    false);
                JPanel iAdminProd   = buildMenuItem("Quản lý Gói",       CARD_ADMIN_PRODUCTS,false);
                JPanel iAdminUser   = buildMenuItem("Quản lý User",      CARD_ADMIN_USERS,   false);
                JPanel iAdminTx     = buildMenuItem("Lịch sử GD hệ thống",CARD_ADMIN_TX,      false);
                JPanel iAdminMission= buildMenuItem("Quản lý Nhiệm vụ", CARD_ADMIN_MISSION, false);
                JPanel itemPerm     = buildMenuItemCustom("Phân Quyền", e -> new PermissionManagementView());

                side.add(iAdminDash);
                side.add(Box.createVerticalStrut(2));
                side.add(iAdminProd);
                side.add(Box.createVerticalStrut(2));
                side.add(iAdminUser);
                side.add(Box.createVerticalStrut(2));
                side.add(iAdminTx);
                side.add(Box.createVerticalStrut(2));
                side.add(iAdminMission);
                side.add(Box.createVerticalStrut(2));
                side.add(itemPerm);

                // Set default active for admin
                activeItem = iAdminDash;
                setActiveStyle(iAdminDash, true);
                setActiveStyle(itemDashboard, false);
            }
        }

        this.activeItem = activeItem;
        return side;
    }

    private JPanel buildMenuItem(String title, String cardId, boolean active) {
        JPanel item = new JPanel(new BorderLayout());
        item.setMaximumSize(new Dimension(190, 42)); // Thu gọn width để tạo margin 2 bên cho bo góc
        item.setBackground(active ? Color.WHITE : SIDEBAR);
        item.setBorder(new EmptyBorder(0, 16, 0, 8));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setOpaque(true);
        item.putClientProperty("FlatLaf.style", "arc: 12;"); // Pill shape

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
        lbl.setForeground(active ? PRIMARY : Color.WHITE);
        item.add(lbl, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (activeItem != item) item.setBackground(SIDEBAR_HOVER); // Hover màu vàng nhạt hơn
            }
            public void mouseExited(MouseEvent e) {
                if (activeItem != item) item.setBackground(SIDEBAR);
            }
            public void mouseClicked(MouseEvent e) {
                setActiveStyle(activeItem, false);
                activeItem = item;
                setActiveStyle(item, true);
                ((JLabel) item.getComponent(0)).setText(title);
                showPanel(cardId);
            }
        });
        return item;
    }

    private JPanel buildMenuItemCustom(String title, ActionListener onClick) {
        JPanel item = new JPanel(new BorderLayout());
        item.setMaximumSize(new Dimension(190, 42));
        item.setBackground(SIDEBAR);
        item.setBorder(new EmptyBorder(0, 16, 0, 8));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setOpaque(true);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(Color.WHITE);
        item.add(lbl, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { item.setBackground(SIDEBAR_HOVER); }
            public void mouseExited(MouseEvent e)  { item.setBackground(SIDEBAR); }
            public void mouseClicked(MouseEvent e) { if (onClick != null) onClick.actionPerformed(null); }
        });
        return item;
    }

    private void setActiveStyle(JPanel item, boolean active) {
        if (item == null) return;
        item.setBackground(active ? Color.WHITE : SIDEBAR);
        if (item.getComponentCount() > 0 && item.getComponent(0) instanceof JLabel) {
            JLabel lbl = (JLabel) item.getComponent(0);
            lbl.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
            lbl.setForeground(active ? PRIMARY : Color.WHITE);
        }
    }

    private JPanel buildSeparator() {
        JPanel sep = new JPanel();
        sep.setBackground(SIDEBAR_HOVER); // Dải phân cách màu vàng nhạt
        sep.setMaximumSize(new Dimension(180, 1));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        return sep;
    }

    // =========================================================================
    //  Panel Switching (lazy init)
    // =========================================================================

    public void switchCard(String cardId) {
        showPanel(cardId);
    }

    private void showPanel(String cardId) {
        switch (cardId) {
            // ── Customer panels ───────────────────────────────────────────────
            case CARD_DASHBOARD -> {
                if (dashboardPanel == null) {
                    dashboardPanel = new CustomerDashboardPanel(currentAccount);
                    contentPane.add(dashboardPanel, CARD_DASHBOARD);
                } else dashboardPanel.loadData();
            }
            case CARD_PRODUCTS -> {
                if (productsPanel == null) {
                    productsPanel = new SavingsProductListPanel(currentAccount);
                    contentPane.add(productsPanel, CARD_PRODUCTS);
                } else productsPanel.loadData();
            }
            case CARD_INVESTMENTS -> {
                if (myInvestmentsPanel == null) {
                    myInvestmentsPanel = new MyInvestmentsPanel(currentAccount);
                    contentPane.add(myInvestmentsPanel, CARD_INVESTMENTS);
                } else myInvestmentsPanel.loadData();
            }
            case CARD_WALLET -> {
                if (walletPanel == null) {
                    walletPanel = new WalletPanel(currentAccount);
                    contentPane.add(walletPanel, CARD_WALLET);
                } else walletPanel.loadData();
            }
            case CARD_MONEY -> {
                if (moneyPanel == null) {
                    moneyPanel = new MoneyManagementPanel(currentAccount);
                    contentPane.add(moneyPanel, CARD_MONEY);
                }
            }
            case CARD_MISSION -> {
                if (missionPanel == null) {
                    missionPanel = new MissionPanel(currentAccount);
                    contentPane.add(missionPanel, CARD_MISSION);
                } else missionPanel.loadData();
            }
            case CARD_SETTINGS -> { /* placeholder */ }

            // ── Admin panels ──────────────────────────────────────────────────
            case CARD_ADMIN_DASH -> {
                if (adminDashboardPanel == null) {
                    adminDashboardPanel = new AdminDashboardPanel(currentAccount);
                    contentPane.add(adminDashboardPanel, CARD_ADMIN_DASH);
                } else adminDashboardPanel.loadData();
            }
            case CARD_ADMIN_PRODUCTS -> {
                if (adminProductsPanel == null) {
                    adminProductsPanel = new SavingsProductPanel(currentAccount);
                    contentPane.add(adminProductsPanel, CARD_ADMIN_PRODUCTS);
                } else adminProductsPanel.loadData();
            }
            case CARD_ADMIN_USERS -> {
                if (adminUsersPanel == null) {
                    adminUsersPanel = new UserManagementPanel(currentAccount);
                    contentPane.add(adminUsersPanel, CARD_ADMIN_USERS);
                } else adminUsersPanel.loadData();
            }
            case CARD_ADMIN_TX -> {
                if (txHistoryPanel == null) {
                    txHistoryPanel = new TransactionHistoryPanel();
                    contentPane.add(txHistoryPanel, CARD_ADMIN_TX);
                } else txHistoryPanel.loadData();
            }
            case CARD_ADMIN_MISSION -> {
                if (missionMgmtPanel == null) {
                    missionMgmtPanel = new MissionManagementPanel();
                    contentPane.add(missionMgmtPanel, CARD_ADMIN_MISSION);
                } else missionMgmtPanel.loadData();
            }

            // ── Staff panels ──────────────────────────────────────────────────
            case CARD_STAFF_DASH -> {
                if (staffDashboardPanel == null) {
                    staffDashboardPanel = new StaffDashboardPanel(currentAccount, this::switchCard);
                    contentPane.add(staffDashboardPanel, CARD_STAFF_DASH);
                } else staffDashboardPanel.loadData();
            }
            case CARD_STAFF_DEPOSIT -> {
                if (depositApprovalPanel == null) {
                    depositApprovalPanel = new DepositApprovalPanel();
                    contentPane.add(depositApprovalPanel, CARD_STAFF_DEPOSIT);
                } else depositApprovalPanel.loadData();
            }
            case CARD_STAFF_WITHDRAW -> {
                if (withdrawApprovalPanel == null) {
                    withdrawApprovalPanel = new WithdrawApprovalPanel();
                    contentPane.add(withdrawApprovalPanel, CARD_STAFF_WITHDRAW);
                } else withdrawApprovalPanel.loadData();
            }
            case CARD_STAFF_KYC -> {
                if (ekycApprovalPanel == null) {
                    ekycApprovalPanel = new EkycApprovalPanel();
                    contentPane.add(ekycApprovalPanel, CARD_STAFF_KYC);
                } else ekycApprovalPanel.loadData();
            }
        }
        cardLayout.show(contentPane, cardId);
    }

    // =========================================================================
    //  Helpers
    // =========================================================================

    private JButton styledBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e)  { b.setBackground(bg); }
        });
        return b;
    }
}
