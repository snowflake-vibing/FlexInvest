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
import View.shared.ProfilePanel;
import View.shared.SettingsPanel;
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
    private static final Color PRIMARY        = UIUtils.INDIGO;
    private static final Color BLUE           = new Color(0,  162, 232);
    private static final Color SIDEBAR        = new Color(15, 23, 42);
    private static final Color SIDEBAR_ACTIVE = new Color(30, 41, 59);
    private static final Color BG             = new Color(248, 250, 252);
    private static final Color CARD_BG   = Color.WHITE;
    private static final Color RED       = new Color(239, 68,  68);
    private static final Color TEXT_DARK = new Color(30,  30,  40);
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
    private ProfilePanel            profilePanel;
    private SettingsPanel           settingsPanel;

    // Lazy-init panels — Staff
    private StaffDashboardPanel      staffDashboardPanel;
    private DepositApprovalPanel     depositApprovalPanel;
    private WithdrawApprovalPanel    withdrawApprovalPanel;
    private EkycApprovalPanel        ekycApprovalPanel;

    // Panel ID constants — Customer
    private static final String CARD_DASHBOARD   = "DASHBOARD";
    private static final String CARD_PRODUCTS    = "PRODUCTS";
    private static final String CARD_INVESTMENTS = "INVESTMENTS";
    private static final String CARD_WALLET      = "WALLET";
    private static final String CARD_MONEY       = "MONEY";
    private static final String CARD_MISSION     = "MISSION";
    private static final String CARD_PROFILE     = "PROFILE";
    private static final String CARD_SETTINGS    = "SETTINGS";

    // Notification
    private final NotificationController notifCtrl = new NotificationController();
    private JButton              bellBtn;
    private JLabel               badgeLbl;
    private NotificationDialog   currentNotifDialog;

    // Lazy-init panels — Admin
    private AdminDashboardPanel      adminDashboardPanel;
    private SavingsProductPanel      adminProductsPanel;
    private UserManagementPanel      adminUsersPanel;
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

    // Sidebar item references for staff navigation
    private JPanel staffItemDashboard;
    private JPanel staffItemDeposit;
    private JPanel staffItemWithdraw;
    private JPanel staffItemKyc;

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
        contentPane.add(new JPanel(), CARD_PROFILE);
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
        btnUser.setBackground(new Color(254, 243, 199));
        btnUser.putClientProperty("FlatLaf.style", "arc: 999;");
        btnUser.setBorderPainted(false);
        btnUser.setFocusPainted(false);
        btnUser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnUser.addActionListener(e -> showProfileDialog());

        // ── Bell button with badge ──────────────────────────────────────────
        JPanel bellWrapper = new JPanel(null);
        bellWrapper.setOpaque(false);
        bellWrapper.setPreferredSize(new java.awt.Dimension(42, 42));

        bellBtn = new JButton("\uf0f3");
        bellBtn.setFont(FA_FONT.deriveFont(18f));
        bellBtn.setBackground(new java.awt.Color(245, 247, 250));
        bellBtn.setForeground(TEXT_DARK);
        bellBtn.setBorderPainted(false);
        bellBtn.setFocusPainted(false);
        bellBtn.setOpaque(true);
        bellBtn.setBounds(0, 8, 36, 36);
        bellBtn.putClientProperty("FlatLaf.style", "arc: 999;");
        bellBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        bellBtn.addActionListener(e -> openNotificationDialog());

                badgeLbl = new JLabel("") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badgeLbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        badgeLbl.setForeground(java.awt.Color.WHITE);
        badgeLbl.setBackground(RED);
        badgeLbl.setOpaque(false); // MUST be false so JLabel doesn't paint the sharp background
        badgeLbl.setHorizontalAlignment(SwingConstants.CENTER);
        badgeLbl.setBounds(22, 2, 18, 18);
        badgeLbl.setVisible(false);

        bellWrapper.add(badgeLbl); // Add badge FIRST so it sits on top
        bellWrapper.add(bellBtn);

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
        if (currentNotifDialog != null && currentNotifDialog.isVisible()) {
            currentNotifDialog.dispose();
            currentNotifDialog = null;
            return;
        }
        currentNotifDialog = NotificationDialog.open(
            this, bellBtn,
            currentAccount.getUser().getUserId(),
            notifCtrl,
            this::refreshNotificationBadge
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

    private static java.awt.Font FA_FONT;
    static {
        try {
            FA_FONT = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new java.io.File("src/Resources/fa-solid-900.ttf")).deriveFont(15f);
        } catch (Exception e) {
            FA_FONT = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15);
        }
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(SIDEBAR);
        side.setPreferredSize(new Dimension(210, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(20, 0, 20, 0));

        JPanel activeItemRef;

        if (isStaff) {
            // ── Staff sidebar ─────────────────────────────────────────────
            // SỬA: Gán trực tiếp vào thuộc tính class thay vì khai báo lại biến cục bộ
            staffItemDashboard = buildMenuItem("Dashboard", "\uf015", CARD_STAFF_DASH, true);
            staffItemDeposit   = buildMenuItem("Duyệt Nạp Tiền", "\uf019", CARD_STAFF_DEPOSIT, false);
            staffItemWithdraw  = buildMenuItem("Duyệt Rút Tiền", "\uf093", CARD_STAFF_WITHDRAW, false);
            staffItemKyc       = buildMenuItem("Duyệt eKYC", "\uf3ed", CARD_STAFF_KYC, false);

            side.add(staffItemDashboard);
            side.add(Box.createVerticalStrut(2));
            side.add(staffItemDeposit);
            side.add(Box.createVerticalStrut(2));
            side.add(staffItemWithdraw);
            side.add(Box.createVerticalStrut(2));
            side.add(staffItemKyc);

            activeItemRef = staffItemDashboard;
            setActiveStyle(staffItemDashboard, true);
        } else {
            // ── Customer sidebar ──────────────────────────────────────────
            JPanel itemDashboard   = buildMenuItem("Dashboard", "\uf015", CARD_DASHBOARD, true);
            JPanel itemProducts    = buildMenuItem("Gói Đầu Tư", "\uf466", CARD_PRODUCTS, false);
            JPanel itemInvestments = buildMenuItem("Khoản Của Tôi", "\uf201", CARD_INVESTMENTS, false);
            JPanel itemWallet      = buildMenuItem("Ví của tôi", "\uf555", CARD_WALLET, false);
            JPanel itemMoney       = buildMenuItem("Quản lý Tiền", "\uf51e", CARD_MONEY, false);
            JPanel itemMission     = buildMenuItem("Nhiệm vụ & Token", "\uf091", CARD_MISSION, false);

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

            activeItemRef = itemDashboard;
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
                lbl.setForeground(new Color(148, 163, 184));
                adminLbl.add(lbl, BorderLayout.CENTER);
                side.add(adminLbl);

                JPanel iAdminDash   = buildMenuItem("Tổng quan (Admin)", "\uf085", CARD_ADMIN_DASH, false);
                JPanel iAdminProd   = buildMenuItem("Quản lý Gói", "\uf466", CARD_ADMIN_PRODUCTS, false);
                JPanel iAdminUser   = buildMenuItem("Quản lý User", "\uf0c0", CARD_ADMIN_USERS, false);
                JPanel iAdminTx     = buildMenuItem("Lịch sử GD hệ thống", "\uf1da", CARD_ADMIN_TX, false);
                JPanel iAdminMission= buildMenuItem("Quản lý Nhiệm vụ", "\uf0ae", CARD_ADMIN_MISSION, false);
                JPanel itemPerm     = buildMenuItemCustom("Phân Quyền", "\uf084", e -> new PermissionManagementView());

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

                activeItemRef = iAdminDash;
                setActiveStyle(iAdminDash, true);
                setActiveStyle(itemDashboard, false);
            }
        }

        side.add(Box.createVerticalGlue());

        JPanel[] profileRef = new JPanel[1];
        JPanel itemProfile = buildMenuItemCustom("Hồ sơ cá nhân", "", e -> {
            if (activeItem != null) setActiveStyle(activeItem, false);
            activeItem = profileRef[0];
            setActiveStyle(profileRef[0], true);
            showPanel(CARD_PROFILE);
        });
        profileRef[0] = itemProfile;
        side.add(itemProfile);

        JPanel[] settingsRef = new JPanel[1];
        JPanel itemSettings = buildMenuItemCustom("Cài đặt", "", e -> {
            if (activeItem != null) setActiveStyle(activeItem, false);
            activeItem = settingsRef[0];
            setActiveStyle(settingsRef[0], true);
            showPanel(CARD_SETTINGS);
        });
        settingsRef[0] = itemSettings;
        side.add(itemSettings);

        this.activeItem = activeItemRef;
        return side;
    }

    private JPanel buildMenuItem(String title, String iconCode, String cardId, boolean active) {
        JPanel item = new JPanel(new BorderLayout(14, 0));
        item.setMaximumSize(new Dimension(190, 42));
        item.setBackground(SIDEBAR);
        item.setBorder(new EmptyBorder(5, 12, 5, 8));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setOpaque(true);
        item.putClientProperty("FlatLaf.style", "arc: 16;");

        JLabel iconLbl = new JLabel(iconCode, SwingConstants.CENTER);
        iconLbl.setFont(FA_FONT);
        iconLbl.setPreferredSize(new Dimension(28, 28));
        iconLbl.setOpaque(true);

        JLabel lbl = new JLabel(title);

        item.add(iconLbl, BorderLayout.WEST);
        item.add(lbl, BorderLayout.CENTER);
        setActiveStyle(item, active);

        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (activeItem != item) item.setBackground(SIDEBAR_ACTIVE);
            }
            public void mouseExited(MouseEvent e) {
                if (activeItem != item) item.setBackground(SIDEBAR);
            }
            public void mouseClicked(MouseEvent e) {
                if (activeItem != null) setActiveStyle(activeItem, false);
                activeItem = item;
                setActiveStyle(item, true);
                showPanel(cardId);
            }
        });
        return item;
    }

    private JPanel buildMenuItemCustom(String title, String iconCode, ActionListener onClick) {
        JPanel item = new JPanel(new BorderLayout(14, 0));
        item.setMaximumSize(new Dimension(190, 42));
        item.setBackground(SIDEBAR);
        item.setBorder(new EmptyBorder(5, 12, 5, 8));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setOpaque(true);
        item.putClientProperty("FlatLaf.style", "arc: 16;");

        JLabel iconLbl = new JLabel(iconCode, SwingConstants.CENTER);
        iconLbl.setFont(FA_FONT);
        iconLbl.setPreferredSize(new Dimension(28, 28));
        iconLbl.setOpaque(true);
        iconLbl.setBackground(SIDEBAR);
        iconLbl.setForeground(new Color(148, 163, 184));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(148, 163, 184));

        item.add(iconLbl, BorderLayout.WEST);
        item.add(lbl, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { item.setBackground(SIDEBAR_ACTIVE); }
            public void mouseExited(MouseEvent e)  { item.setBackground(SIDEBAR); }
            public void mouseClicked(MouseEvent e) { if (onClick != null) onClick.actionPerformed(null); }
        });
        return item;
    }

    private void setActiveStyle(JPanel item, boolean active) {
        if (item == null) return;
        item.setBackground(active ? SIDEBAR_ACTIVE : SIDEBAR);
        item.setBorder(active
            ? BorderFactory.createCompoundBorder(
                  BorderFactory.createMatteBorder(0, 3, 0, 0, PRIMARY),
                  new EmptyBorder(5, 9, 5, 8))
            : new EmptyBorder(5, 12, 5, 8));

        if (item.getComponentCount() > 1
                && item.getComponent(0) instanceof JLabel iconLbl
                && item.getComponent(1) instanceof JLabel lbl) {
            iconLbl.setBackground(active ? PRIMARY : SIDEBAR);
            iconLbl.setForeground(active ? Color.WHITE : new Color(148, 163, 184));

            lbl.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
            lbl.setForeground(active ? Color.WHITE : new Color(148, 163, 184));
        }
    }

    private JPanel buildSeparator() {
        JPanel sep = new JPanel();
        sep.setBackground(new Color(51, 65, 85));
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
            case CARD_PROFILE -> {
                if (profilePanel == null) {
                    profilePanel = new ProfilePanel(currentAccount);
                    contentPane.add(profilePanel, CARD_PROFILE);
                }
            }
            case CARD_SETTINGS -> {
                if (settingsPanel == null) {
                    settingsPanel = new SettingsPanel(currentAccount, this);
                    contentPane.add(settingsPanel, CARD_SETTINGS);
                }
            }

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
                    // Thống nhất dùng this::switchCard làm callback điều hướng từ Dashboard sang panel duyệt lệnh
                    staffDashboardPanel = new StaffDashboardPanel(currentAccount, this::switchCard);
                    contentPane.add(staffDashboardPanel, CARD_STAFF_DASH);
                } else staffDashboardPanel.loadData();
                setActiveSidebarItem(CARD_STAFF_DASH);
            }
            case CARD_STAFF_DEPOSIT -> {
                if (depositApprovalPanel == null) {
                    depositApprovalPanel = new DepositApprovalPanel();
                    contentPane.add(depositApprovalPanel, CARD_STAFF_DEPOSIT);
                } else depositApprovalPanel.loadData();
                setActiveSidebarItem(CARD_STAFF_DEPOSIT);
            }
            case CARD_STAFF_WITHDRAW -> {
                if (withdrawApprovalPanel == null) {
                    withdrawApprovalPanel = new WithdrawApprovalPanel();
                    contentPane.add(withdrawApprovalPanel, CARD_STAFF_WITHDRAW);
                } else withdrawApprovalPanel.loadData();
                setActiveSidebarItem(CARD_STAFF_WITHDRAW);
            }
            case CARD_STAFF_KYC -> {
                if (ekycApprovalPanel == null) {
                    ekycApprovalPanel = new EkycApprovalPanel();
                    contentPane.add(ekycApprovalPanel, CARD_STAFF_KYC);
                } else ekycApprovalPanel.loadData();
                setActiveSidebarItem(CARD_STAFF_KYC);
            }
        }
        cardLayout.show(contentPane, cardId);
        contentPane.revalidate();
        contentPane.repaint();
    }

    private void setActiveSidebarItem(String cardId) {
        JPanel target = switch (cardId) {
            case CARD_STAFF_DASH     -> staffItemDashboard;
            case CARD_STAFF_DEPOSIT  -> staffItemDeposit;
            case CARD_STAFF_WITHDRAW -> staffItemWithdraw;
            case CARD_STAFF_KYC      -> staffItemKyc;
            default                  -> null;
        };
        if (target != null && target != activeItem) {
            setActiveStyle(activeItem, false);
            setActiveStyle(target, true);
            activeItem = target;
        }
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