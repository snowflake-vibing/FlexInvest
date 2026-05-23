package View.staff;

import Controller.SavingsProductController;
import Model.AccountModel;
import Model.SavingsProduct;
import View.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class SavingsProductPanel extends JPanel {

    private static final Color NAVY      = new Color(15, 40, 80);
    private static final Color BLUE      = new Color(0, 162, 232);
    private static final Color GREEN     = new Color(16, 185, 129);
    private static final Color YELLOW    = new Color(245, 158, 11);
    private static final Color BG        = UIUtils.APP_BG;
    private static final Color CARD_BG   = UIUtils.CARD_BG;
    private static final Color TEXT_DARK = UIUtils.TEXT_DARK;
    private static final Color TEXT_MUTED = UIUtils.TEXT_MUTED;
    private static final Color BORDER_C  = UIUtils.BORDER_COLOR;

    private final AccountModel account;
    private final SavingsProductController spController = new SavingsProductController();

    private DefaultTableModel tableModel;
    private JTable table;
    private List<SavingsProduct> products;
    private JButton btnRefresh;

    public SavingsProductPanel(AccountModel account) {
        this.account = account;
        setLayout(new BorderLayout());
        setBackground(BG);
        build();
        loadData();
    }

    private void build() {
        JPanel inner = new JPanel(new BorderLayout(0, 16));
        inner.setBackground(BG);
        inner.setBorder(new EmptyBorder(24, 28, 24, 28));

        // ── Header chuẩn ───────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleGroup = new JPanel();
        titleGroup.setOpaque(false);
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Quản lý Gói Đầu Tư");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(NAVY);
        JLabel sub = new JLabel("Thêm, sửa và bật/tắt các gói tiết kiệm");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        titleGroup.add(title);
        titleGroup.add(sub);

        // Nút actions ở bên phải header
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGroup.setOpaque(false);

        btnRefresh = outlineBtn("Làm mới");
        btnRefresh.addActionListener(e -> loadData());

        JButton btnAdd = primaryBtn("+ Thêm Gói", NAVY);
        btnAdd.addActionListener(e -> showProductDialog(null));

        JButton btnEdit = outlineBtn("Sửa");
        btnEdit.addActionListener(e -> editSelected());

        JButton btnToggle = new JButton("Bật/Tắt");
        btnToggle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggle.setBackground(YELLOW);
        btnToggle.setForeground(Color.WHITE);
        btnToggle.setBorderPainted(false);
        btnToggle.setFocusPainted(false);
        btnToggle.setOpaque(true);
        btnToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggle.addActionListener(e -> toggleSelected());

        btnGroup.add(btnRefresh);
        btnGroup.add(btnAdd);
        btnGroup.add(btnEdit);
        btnGroup.add(btnToggle);

        header.add(titleGroup, BorderLayout.WEST);
        header.add(btnGroup,   BorderLayout.EAST);
        inner.add(header, BorderLayout.NORTH);

        // ── Table ──────────────────────────────────────────────────────────
        String[] cols = {"ID", "Tên Gói", "Lãi suất", "Kỳ hạn", "Min / Max", "Loại", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);

        // VIP marker renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                String type = (String) tableModel.getValueAt(r, 5);
                if (type != null && type.contains("VIP")) {
                    comp.setForeground(new Color(212, 175, 55)); // Gold — giữ nguyên vì là màu đặc thù VIP
                    comp.setFont(comp.getFont().deriveFont(Font.BOLD));
                } else {
                    comp.setForeground(s ? t.getSelectionForeground() : TEXT_DARK);
                    comp.setFont(t.getFont());
                }
                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_C, 1));
        scroll.getViewport().setBackground(CARD_BG);
        inner.add(scroll, BorderLayout.CENTER);

        add(inner, BorderLayout.CENTER);
    }

    public void loadData() {
        btnRefresh.setEnabled(false);
        btnRefresh.setText("Đang tải...");
        SwingWorker<List<SavingsProduct>, Void> worker = new SwingWorker<>() {
            @Override protected List<SavingsProduct> doInBackground() {
                return spController.getAllProducts();
            }

            @Override protected void done() {
                try {
                    products = get();
                    tableModel.setRowCount(0);
                    for (SavingsProduct p : products) {
                        String minMax = String.format("%,.0f - %s",
                            p.getMinInvestmentAmount(),
                            p.getMaxInvestmentAmount() != null
                                ? String.format("%,.0f", p.getMaxInvestmentAmount()) : "∞");
                        String type = "FlexToken".equalsIgnoreCase(p.getCurrency()) ? "VIP (FlexToken)" : "Thường";
                        tableModel.addRow(new Object[]{
                            p.getProductId(), p.getProductName(),
                            p.getInterestRate() + "%",
                            p.getTerm() == 0 ? "Flex-Safe" : p.getTerm() + " ngày",
                            minMax, type, p.getStatus()
                        });
                    }
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

    private void editSelected() {
        int r = table.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một gói để sửa."); return; }
        showProductDialog(products.get(r));
    }

    private void toggleSelected() {
        int r = table.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một gói để bật/tắt."); return; }
        SavingsProduct p = products.get(r);
        boolean currentlyActive = "ACTIVE".equalsIgnoreCase(p.getStatus());
        if (spController.toggleActive(p.getProductId(), !currentlyActive)) {
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật trạng thái (có thể gói đang có người dùng ACTIVE).");
        }
    }

    private void showProductDialog(SavingsProduct p) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            p == null ? "Thêm Gói Mới" : "Sửa Gói", true);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(CARD_BG);

        JTextField txtName = new JTextField(p != null ? p.getProductName() : "");
        JTextField txtRate = new JTextField(p != null ? p.getInterestRate().toString() : "0.0");
        JTextField txtTerm = new JTextField(p != null ? String.valueOf(p.getTerm()) : "0");
        JTextField txtMin  = new JTextField(p != null ? p.getMinInvestmentAmount().toString() : "100000");
        JTextField txtMax  = new JTextField(p != null && p.getMaxInvestmentAmount() != null ? p.getMaxInvestmentAmount().toString() : "");
        JComboBox<String> cbCurrency = new JComboBox<>(new String[]{"VNĐ", "FlexToken"});
        if (p != null && "FlexToken".equalsIgnoreCase(p.getCurrency())) cbCurrency.setSelectedIndex(1);

        styleDialogLabel(form, "Tên Gói:");               form.add(txtName);
        styleDialogLabel(form, "Lãi suất (%/năm):");      form.add(txtRate);
        styleDialogLabel(form, "Kỳ hạn (ngày, 0=Flex):"); form.add(txtTerm);
        styleDialogLabel(form, "Tối thiểu:");             form.add(txtMin);
        styleDialogLabel(form, "Tối đa (trống=vô hạn):"); form.add(txtMax);
        styleDialogLabel(form, "Loại (FlexToken=VIP):");   form.add(cbCurrency);

        JButton btnSave = primaryBtn("Lưu", BLUE);
        btnSave.addActionListener(e -> {
            try {
                SavingsProduct np = p == null ? new SavingsProduct() : p;
                np.setProductName(txtName.getText().trim());
                np.setInterestRate(new BigDecimal(txtRate.getText().trim()));
                np.setTerm(Integer.parseInt(txtTerm.getText().trim()));
                np.setMinInvestmentAmount(new BigDecimal(txtMin.getText().trim()));
                String maxStr = txtMax.getText().trim();
                np.setMaxInvestmentAmount(maxStr.isEmpty() ? null : new BigDecimal(maxStr));
                np.setCurrency((String) cbCurrency.getSelectedItem());
                if (p == null) {
                    np.setPenaltyRate(BigDecimal.ZERO);
                    np.setFallbackInterestRate(BigDecimal.ZERO);
                    np.setMinHoldingDays(0);
                    np.setStatus("ACTIVE");
                }
                boolean ok = p == null
                    ? spController.createProduct(np) > 0
                    : spController.updateProduct(np);
                if (ok) { loadData(); d.dispose(); }
                else JOptionPane.showMessageDialog(d, "Lỗi khi lưu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Dữ liệu không hợp lệ: " + ex.getMessage());
            }
        });

        d.add(form, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnSave);
        d.add(bottom, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

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

    private JButton primaryBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleDialogLabel(JPanel panel, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_DARK);
        panel.add(l);
    }
}
