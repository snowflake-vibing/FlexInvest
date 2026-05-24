package View.customer;

import Controller.WalletController;
import DAO.BankAccountDAO;
import DAO.InvestmentDAO;
import Model.AccountModel;
import Model.BankAccount;
import Model.SavingsProduct;
import Model.Wallet;
import View.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * MoneyManagementPanel — gộp 3 tab vào một panel:
 *  Tab 1: Nạp tiền (chọn gateway + nhập số tiền)
 *  Tab 2: Rút tiền (nhập số tiền + chọn bank account)
 *  Tab 3: Tài khoản ngân hàng (danh sách + Thêm / Xóa / Đặt mặc định)
 */
public class MoneyManagementPanel extends JPanel {

    // ── Màu sắc ──────────────────────────────────────────────────────────────
    private static final Color NAVY      = new Color(15, 40, 80);
    private static final Color BLUE      = new Color(0, 162, 232);
    private static final Color GREEN     = new Color(16, 185, 129);
    private static final Color YELLOW    = new Color(245, 158, 11);
    private static final Color RED       = new Color(239, 68, 68);
    private static final Color CARD      = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 30, 40);
    private static final Color MUTED     = new Color(110, 115, 130);
    private static final Color BORDER_C  = new Color(210, 220, 235);
    private static final NumberFormat VND = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final AccountModel      account;
    private final int               userId;
    private final WalletController  walletCtrl  = new WalletController();
    private final BankAccountDAO    bankDAO     = new BankAccountDAO();
    private final InvestmentDAO     investDAO   = new InvestmentDAO();

    // ── Tab 1: Nạp tiền ───────────────────────────────────────────────────────
    private JComboBox<String>       cbGateway;
    private JTextField              txtDepositAmount;
    private JLabel                  lblBalance;

    // ── Tab 2: Rút tiền ───────────────────────────────────────────────────────
    private JTextField              txtWithdrawAmount;
    private JComboBox<String>       cbBankAccounts;
    private List<BankAccount>       bankList;

    // ── Tab 3: Bank accounts ──────────────────────────────────────────────────
    private JList<String>           lstBanks;
    private DefaultListModel<String> bankListModel;
    private List<BankAccount>       bankListFull;

    // ── Tabs (field để switch từ bên trong) ───────────────────────────────────
    private JTabbedPane             tabs;
    private JPanel                  noBankPanel;

    public MoneyManagementPanel(AccountModel account) {
        this.account = account;
        this.userId  = account.getUser().getUserId();
        setLayout(new BorderLayout());
        setBackground(UIUtils.APP_BG);
        build();
        refreshBalance();
    }

    // =========================================================================
    //  Build
    // =========================================================================

    private void build() {
        JPanel inner = new JPanel(new BorderLayout(0, 16));
        inner.setBackground(UIUtils.APP_BG);
        inner.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = UIUtils.createHeading1("Quản lý Tiền");
        lblBalance = new JLabel("Số dư: đang tải...");
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblBalance.setForeground(BLUE);
        header.add(title,    BorderLayout.WEST);
        header.add(lblBalance, BorderLayout.EAST);
        inner.add(header, BorderLayout.NORTH);

        // Tabs
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(CARD);
        tabs.addTab("  Nạp tiền",   buildDepositTab());
        tabs.addTab("  Rút tiền",   buildWithdrawTab());
        tabs.addTab("  Tài khoản NH", buildBankTab());
        inner.add(tabs, BorderLayout.CENTER);

        add(inner, BorderLayout.CENTER);
    }

    // ── Tab 1: Nạp tiền ───────────────────────────────────────────────────────
    private JPanel buildDepositTab() {
        JPanel p = cardPanel();

        p.add(sectionLabel("Tạo lệnh nạp tiền mới"));
        p.add(Box.createVerticalStrut(20));

        // Gateway
        p.add(fieldLabel("Phương thức thanh toán"));
        p.add(Box.createVerticalStrut(6));
        cbGateway = new JComboBox<>(new String[]{
            "BANKING — Chuyển khoản ngân hàng",
            "MOMO — Ví MoMo",
            "VNPAY — VNPay QR"
        });
        styleCombo(cbGateway);
        p.add(cbGateway);
        p.add(Box.createVerticalStrut(16));

        // Số tiền
        p.add(fieldLabel("Số tiền nạp (VNĐ)"));
        p.add(Box.createVerticalStrut(6));
        txtDepositAmount = new JTextField();
        styleField(txtDepositAmount);
        txtDepositAmount.setToolTipText("Tối thiểu 10,000 VNĐ");
        p.add(txtDepositAmount);
        p.add(Box.createVerticalStrut(24));

        // Nút nạp
        JButton btnDeposit = actionBtn("Tạo lệnh nạp", BLUE);
        btnDeposit.addActionListener(e -> onDeposit());
        p.add(btnDeposit);

        // Note
        p.add(Box.createVerticalStrut(16));
        JLabel note = new JLabel("<html><i>Lệnh nạp sẽ được nhân viên xác nhận trong vòng 30 phút làm việc.</i></html>");
        note.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        note.setForeground(MUTED);
        note.setAlignmentX(LEFT_ALIGNMENT);
        p.add(note);

        return wrap(p);
    }

    // ── Tab 2: Rút tiền ───────────────────────────────────────────────────────
    private JPanel buildWithdrawTab() {
        JPanel p = cardPanel();

        p.add(sectionLabel("Tạo lệnh rút tiền"));
        p.add(Box.createVerticalStrut(20));

        p.add(fieldLabel("Số tiền rút (VNĐ)"));
        p.add(Box.createVerticalStrut(6));
        txtWithdrawAmount = new JTextField();
        styleField(txtWithdrawAmount);
        p.add(txtWithdrawAmount);
        p.add(Box.createVerticalStrut(16));

        // Panel hiện khi chưa có tài khoản NH
        noBankPanel = new JPanel();
        noBankPanel.setLayout(new BoxLayout(noBankPanel, BoxLayout.Y_AXIS));
        noBankPanel.setOpaque(false);
        noBankPanel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel noAccLbl = new JLabel("⚠  Bạn chưa liên kết tài khoản ngân hàng nào.");
        noAccLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        noAccLbl.setForeground(RED);
        noAccLbl.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnGoBank = new JButton("→  Thêm tài khoản ngân hàng ngay");
        btnGoBank.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGoBank.setBackground(BLUE);
        btnGoBank.setForeground(Color.WHITE);
        btnGoBank.setFocusPainted(false);
        btnGoBank.setBorderPainted(false);
        btnGoBank.setOpaque(true);
        btnGoBank.setAlignmentX(LEFT_ALIGNMENT);
        btnGoBank.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnGoBank.addActionListener(e -> {
            if (tabs != null) tabs.setSelectedIndex(2);
        });

        noBankPanel.add(noAccLbl);
        noBankPanel.add(Box.createVerticalStrut(10));
        noBankPanel.add(btnGoBank);
        p.add(noBankPanel);
        p.add(Box.createVerticalStrut(16));

        p.add(fieldLabel("Tài khoản ngân hàng nhận"));
        p.add(Box.createVerticalStrut(6));
        cbBankAccounts = new JComboBox<>();
        styleCombo(cbBankAccounts);
        p.add(cbBankAccounts);
        p.add(Box.createVerticalStrut(24));

        JButton btnWithdraw = actionBtn("Tạo lệnh rút", new Color(245, 158, 11));
        btnWithdraw.addActionListener(e -> onWithdraw());
        p.add(btnWithdraw);

        // Load bank list when tab is shown
        loadBankCombo();

        return wrap(p);
    }

    // ── Tab 3: Tài khoản ngân hàng ───────────────────────────────────────────
    private JPanel buildBankTab() {
        JPanel p = cardPanel();

        p.add(sectionLabel("Tài khoản ngân hàng liên kết"));
        p.add(Box.createVerticalStrut(16));

        bankListModel = new DefaultListModel<>();
        lstBanks = new JList<>(bankListModel);
        lstBanks.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lstBanks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstBanks.setFixedCellHeight(44);
        lstBanks.setCellRenderer(new BankCellRenderer());

        JScrollPane sp = new JScrollPane(lstBanks);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C, 1));
        sp.setAlignmentX(LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        sp.setPreferredSize(new Dimension(0, 220));
        p.add(sp);
        p.add(Box.createVerticalStrut(14));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnAdd = smallBtn("➕  Thêm", GREEN);
        btnAdd.addActionListener(e -> onAddBank());

        JButton btnDef = smallBtn("⭐  Đặt mặc định", BLUE);
        btnDef.addActionListener(e -> onSetDefault());

        JButton btnDel = smallBtn("🗑  Xóa", new Color(239, 68, 68));
        btnDel.addActionListener(e -> onDeleteBank());

        btnRow.add(btnAdd);
        btnRow.add(btnDef);
        btnRow.add(btnDel);
        p.add(btnRow);

        loadBankList();

        return wrap(p);
    }

    // =========================================================================
    //  Actions
    // =========================================================================

    private void onDeposit() {
        String raw = txtDepositAmount.getText().trim().replace(",", "").replace(".", "");
        BigDecimal amount;
        try {
            amount = new BigDecimal(raw);
            if (amount.compareTo(new BigDecimal("10000")) < 0) {
                warn("Số tiền nạp tối thiểu là 10,000 VNĐ!"); return;
            }
        } catch (NumberFormatException ex) {
            warn("Số tiền không hợp lệ!"); return;
        }

        String gwFull = (String) cbGateway.getSelectedItem();
        String gw = gwFull != null ? gwFull.split(" ")[0] : "BANKING";

        int depositId = walletCtrl.requestDeposit(userId, amount, gw, gw + "_ACCOUNT", 60);
        if (depositId > 0) {
            String transferNote = "NAP" + depositId;
            JPanel dlgPanel = new JPanel();
            dlgPanel.setLayout(new BoxLayout(dlgPanel, BoxLayout.Y_AXIS));
            dlgPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

            JLabel title = new JLabel("✅  Tạo lệnh nạp thành công — Mã #" + depositId);
            title.setFont(new Font("Segoe UI", Font.BOLD, 14));
            title.setForeground(new Color(16, 185, 129));
            title.setAlignmentX(LEFT_ALIGNMENT);
            dlgPanel.add(title);
            dlgPanel.add(Box.createVerticalStrut(14));

            JLabel instr = new JLabel("Vui lòng chuyển khoản theo thông tin sau:");
            instr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            instr.setAlignmentX(LEFT_ALIGNMENT);
            dlgPanel.add(instr);
            dlgPanel.add(Box.createVerticalStrut(10));

            JPanel infoBox = new JPanel(new GridLayout(4, 2, 8, 8));
            infoBox.setBackground(new Color(245, 248, 255));
            infoBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 235), 1, true),
                new EmptyBorder(12, 14, 12, 14)));
            infoBox.setAlignmentX(LEFT_ALIGNMENT);

            Font labelFont = new Font("Segoe UI", Font.PLAIN, 12);
            Font valueFont = new Font("Segoe UI", Font.BOLD, 13);

            JLabel l1 = new JLabel("Ngân hàng:"); l1.setFont(labelFont);
            JLabel v1 = new JLabel("BIDV");        v1.setFont(valueFont);

            JLabel l2 = new JLabel("Số tài khoản:"); l2.setFont(labelFont);
            JLabel v2 = new JLabel("8801726545");    v2.setFont(valueFont); v2.setForeground(BLUE);

            JLabel l3 = new JLabel("Số tiền:"); l3.setFont(labelFont);
            JLabel v3 = new JLabel(VND.format(amount) + " VNĐ"); v3.setFont(valueFont); v3.setForeground(GREEN);

            JLabel l4 = new JLabel("Nội dung CK:"); l4.setFont(labelFont);
            JLabel v4 = new JLabel(transferNote);    v4.setFont(valueFont); v4.setForeground(RED);

            infoBox.add(l1); infoBox.add(v1);
            infoBox.add(l2); infoBox.add(v2);
            infoBox.add(l3); infoBox.add(v3);
            infoBox.add(l4); infoBox.add(v4);
            dlgPanel.add(infoBox);
            dlgPanel.add(Box.createVerticalStrut(10));

            JLabel note2 = new JLabel("<html><i>⚠ Vui lòng nhập ĐÚNG nội dung chuyển khoản để được xác nhận nhanh.<br/>"
                + "Lệnh sẽ được xác nhận trong vòng 30 phút giờ hành chính.</i></html>");
            note2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            note2.setForeground(MUTED);
            note2.setAlignmentX(LEFT_ALIGNMENT);
            dlgPanel.add(note2);

            JOptionPane.showMessageDialog(this, dlgPanel, "Thông tin chuyển khoản", JOptionPane.PLAIN_MESSAGE);
            txtDepositAmount.setText("");
            refreshBalance();
        } else {
            error("Tạo lệnh nạp thất bại. Vui lòng thử lại!");
        }
    }

    private void onWithdraw() {
        String raw = txtWithdrawAmount.getText().trim().replace(",", "");
        BigDecimal amount;
        try {
            amount = new BigDecimal(raw);
            if (amount.compareTo(new BigDecimal("50000")) < 0) {
                warn("Số tiền rút tối thiểu là 50,000 VNĐ!"); return;
            }
        } catch (NumberFormatException ex) {
            warn("Số tiền không hợp lệ!"); return;
        }

        int idx = cbBankAccounts.getSelectedIndex();
        if (bankList == null || bankList.isEmpty() || idx < 0) {
            warn("Vui lòng chọn tài khoản ngân hàng!"); return;
        }
        int bankAccountId = bankList.get(idx).getBankAccountId();

        WalletController.Result result = walletCtrl.requestWithdraw(
            userId, bankAccountId, amount, BigDecimal.ZERO);

        switch (result) {
            case SUCCESS -> {
                JOptionPane.showMessageDialog(this,
                    String.format("Tạo lệnh rút thành công!\nSố tiền: %s VNĐ\nSẽ được xử lý trong 1-3 ngày làm việc.",
                        VND.format(amount)),
                    "Rút tiền", JOptionPane.INFORMATION_MESSAGE);
                txtWithdrawAmount.setText("");
                refreshBalance();
            }
            case INSUFFICIENT_FUNDS -> warn("Số dư không đủ để thực hiện lệnh rút này!");
            default -> error("Tạo lệnh rút thất bại. Vui lòng thử lại!");
        }
    }

    private void onAddBank() {
        JTextField tfBank = new JTextField();
        JTextField tfNum  = new JTextField();
        JPanel dlg = new JPanel(new GridLayout(4, 1, 6, 6));
        dlg.add(new JLabel("Tên ngân hàng (ví dụ: Vietcombank):"));
        dlg.add(tfBank);
        dlg.add(new JLabel("Số tài khoản:"));
        dlg.add(tfNum);

        int res = JOptionPane.showConfirmDialog(this, dlg, "Thêm tài khoản ngân hàng",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String bank = tfBank.getText().trim();
        String num  = tfNum.getText().trim();
        if (bank.isEmpty() || num.isEmpty()) { warn("Vui lòng điền đầy đủ thông tin!"); return; }

        BankAccount ba = new BankAccount();
        ba.setUserId(userId);
        ba.setBankName(bank);
        ba.setAccountNumber(num);
        ba.setIsLinked(0);
        boolean ok = bankDAO.add(ba);
        if (ok) {
            loadBankList();
            loadBankCombo();
            JOptionPane.showMessageDialog(this, "Đã thêm tài khoản ngân hàng thành công!\nBạn có thể chuyển sang tab Rút tiền để rút ngay.");
        } else {
            error("Thêm thất bại. Vui lòng thử lại!");
        }
    }

    private void onSetDefault() {
        int idx = lstBanks.getSelectedIndex();
        if (idx < 0 || bankListFull == null || idx >= bankListFull.size()) {
            warn("Vui lòng chọn tài khoản ngân hàng!"); return;
        }
        int bankId = bankListFull.get(idx).getBankAccountId();
        boolean ok = bankDAO.setLinked(bankId, userId);
        if (ok) { loadBankList(); loadBankCombo(); }
        else error("Đặt mặc định thất bại!");
    }

    private void onDeleteBank() {
        int idx = lstBanks.getSelectedIndex();
        if (idx < 0) { warn("Vui lòng chọn tài khoản cần xóa!"); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Xóa tài khoản ngân hàng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        // Soft delete bằng cách gọi setLinked = 0 (không xóa hẳn vì DAO chưa có delete)
        // Tạm thời hiện thông báo — trong thực tế cần thêm BankAccountDAO.delete()
        warn("Chức năng xóa cần thêm BankAccountDAO.delete() vào DAO.");
    }

    // =========================================================================
    //  Data helpers
    // =========================================================================

    private void refreshBalance() {
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() {
                Wallet w = walletCtrl.getBalance(userId);
                if (w == null) return "Không tải được";
                return VND.format(w.getAvailableBalance()) + " VNĐ";
            }
            @Override protected void done() {
                try { lblBalance.setText("Số dư: " + get()); }
                catch (Exception ignored) {}
            }
        }.execute();
    }

    private void loadBankCombo() {
        bankList = bankDAO.findByAccount(userId);
        if (cbBankAccounts == null) return;
        cbBankAccounts.removeAllItems();
        boolean hasBank = bankList != null && !bankList.isEmpty();
        if (noBankPanel != null) noBankPanel.setVisible(!hasBank);
        cbBankAccounts.setVisible(hasBank);
        if (hasBank) {
            for (BankAccount b : bankList) {
                cbBankAccounts.addItem(b.getBankName() + " — " + b.getAccountNumber()
                    + (b.getIsLinked() == 1 ? " ⭐" : ""));
            }
        }
    }

    private void loadBankList() {
        bankListFull = bankDAO.findByAccount(userId);
        if (bankListModel == null) return;
        bankListModel.clear();
        for (BankAccount b : bankListFull) {
            bankListModel.addElement(b.getBankName() + "  |  " + b.getAccountNumber()
                + (b.getIsLinked() == 1 ? "  ⭐ Mặc định" : ""));
        }
    }

    // =========================================================================
    //  UI builders
    // =========================================================================

    private JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        UIUtils.styleCard(p);
        return p;
    }

    private JPanel wrap(JPanel inner) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(UIUtils.APP_BG);
        outer.setBorder(new EmptyBorder(16, 0, 0, 0));
        outer.add(inner, BorderLayout.NORTH);
        return outer;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = UIUtils.createHeading2(text);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C, 1, true),
            new EmptyBorder(0, 12, 0, 12)));
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setAlignmentX(LEFT_ALIGNMENT);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }

    private JButton actionBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton smallBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }
    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // ── Bank cell renderer ────────────────────────────────────────────────────
    private static class BankCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int idx, boolean sel, boolean focus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, idx, sel, focus);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            l.setBorder(new EmptyBorder(8, 14, 8, 14));
            return l;
        }
    }
}
