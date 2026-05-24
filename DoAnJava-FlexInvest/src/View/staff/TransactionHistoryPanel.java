package View.staff;

import DAO.WalletDAO;
import Model.Transaction;
import View.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * TransactionHistoryPanel — Admin panel toàn bộ giao dịch hệ thống.
 *
 * Tính năng:
 *  - Bảng tất cả TRANSACTION (wallet_id, type, amount, status, thời gian)
 *  - Filter theo ngày + loại giao dịch
 *  - Export ra CSV bằng FileWriter
 *
 * NOTE: loadAllTransactions() dùng JDBC trực tiếp vì WalletDAO chưa có
 *       method getAllTransactions(). TODO: Chuyển sang WalletDAO.
 */
public class TransactionHistoryPanel extends JPanel {

    // ── Bộ màu chuẩn ──────────────────────────────────────────────────────────
    private static final Color NAVY     = new Color(15, 40, 80);
    private static final Color BLUE     = new Color(0, 162, 232);
    private static final Color GREEN    = new Color(16, 185, 129);
    private static final Color BG       = UIUtils.APP_BG;
    private static final Color CARD     = UIUtils.CARD_BG;
    private static final Color TEXT_DARK = UIUtils.TEXT_DARK;
    private static final Color TEXT_MUTED = UIUtils.TEXT_MUTED;
    private static final Color BORDER_C = UIUtils.BORDER_COLOR;
    private static final NumberFormat VND = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final WalletDAO walletDAO = new WalletDAO();

    private JTable            table;
    private TxTableModel      tableModel;
    private List<Transaction> allTx = new ArrayList<>();
    private JButton           btnRefresh;

    // Filter controls
    private JComboBox<String> cbType;
    private JTextField        txtFrom;
    private JTextField        txtTo;
    private JLabel            lblCount;

    public TransactionHistoryPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        build();
        loadData();
    }

    // =========================================================================
    //  Build
    // =========================================================================

    private void build() {
        // ── Top: header + filter ────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setBackground(BG);
        top.setBorder(new EmptyBorder(24, 28, 0, 28));

        // Header chuẩn
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleGroup = new JPanel();
        titleGroup.setOpaque(false);
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Lịch sử Giao Dịch Hệ Thống");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(NAVY);
        JLabel sub = new JLabel("Xem và lọc toàn bộ giao dịch — export CSV");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        titleGroup.add(title);
        titleGroup.add(sub);

        btnRefresh = outlineBtn("Làm mới");
        btnRefresh.addActionListener(e -> loadData());

        header.add(titleGroup, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);
        top.add(header, BorderLayout.NORTH);

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        UIUtils.styleCard(filterBar);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C, 1),
            new EmptyBorder(0, 8, 0, 8)));

        filterBar.add(filterLabel("Loại:"));
        cbType = new JComboBox<>(new String[]{
            "Tất cả", "DEPOSIT", "WITHDRAW", "INVEST", "PAYOUT", "BONUS"
        });
        cbType.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterBar.add(cbType);

        filterBar.add(filterLabel("  Từ:"));
        txtFrom = new JTextField("01/01/" + LocalDate.now().getYear(), 10);
        txtFrom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterBar.add(txtFrom);

        filterBar.add(filterLabel("Đến:"));
        txtTo = new JTextField(LocalDate.now().format(DATE_FMT), 10);
        txtTo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterBar.add(txtTo);

        JButton btnFilter = primaryBtn("Lọc", BLUE);
        btnFilter.addActionListener(e -> applyFilter());
        filterBar.add(btnFilter);

        JButton btnExport = primaryBtn("Export CSV", GREEN);
        btnExport.addActionListener(e -> exportCsv());
        filterBar.add(btnExport);

        lblCount = new JLabel("0 giao dịch");
        lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblCount.setForeground(TEXT_MUTED);
        filterBar.add(lblCount);

        top.add(filterBar, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // ── Table ──────────────────────────────────────────────────────────
        tableModel = new TxTableModel();
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        int[] widths = {55, 70, 100, 120, 90, 160};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_C, 1));
        sp.getViewport().setBackground(CARD);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BG);
        tablePanel.setBorder(new EmptyBorder(12, 28, 24, 28));
        tablePanel.add(sp, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);
    }

    // =========================================================================
    //  Data
    // =========================================================================

    public void loadData() {
        btnRefresh.setEnabled(false);
        btnRefresh.setText("Đang tải...");
        new SwingWorker<List<Transaction>, Void>() {
            @Override protected List<Transaction> doInBackground() {
                return loadAllTransactions();
            }
            @Override protected void done() {
                try {
                    allTx = get();
                    applyFilter();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    btnRefresh.setEnabled(true);
                    btnRefresh.setText("Làm mới");
                }
            }
        }.execute();
    }

    private List<Transaction> loadAllTransactions() {
        return walletDAO.getAllTransactions();
    }

    private void applyFilter() {
        String type = (String) cbType.getSelectedItem();
        LocalDate from = parseDate(txtFrom.getText(), LocalDate.of(2020, 1, 1));
        LocalDate to   = parseDate(txtTo.getText(), LocalDate.now());

        List<Transaction> filtered = allTx.stream().filter(t -> {
            if (!"Tất cả".equals(type) && !type.equals(t.getTypeCode())) return false;
            if (t.getCreatedAt() != null) {
                LocalDate d = t.getCreatedAt().toLocalDate();
                if (d.isBefore(from) || d.isAfter(to)) return false;
            }
            return true;
        }).toList();

        tableModel.setData(filtered);
        lblCount.setText(filtered.size() + " giao dịch");
    }

    // =========================================================================
    //  Export CSV
    // =========================================================================

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("transactions_" + LocalDate.now() + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
            fw.write("ID,WalletID,Loai,SoTien,TrangThai,ThoiGian\n");
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                fw.write(
                    tableModel.getValueAt(r, 0) + "," +
                    tableModel.getValueAt(r, 1) + "," +
                    tableModel.getValueAt(r, 2) + "," +
                    tableModel.getValueAt(r, 3) + "," +
                    tableModel.getValueAt(r, 4) + "," +
                    tableModel.getValueAt(r, 5) + "\n"
                );
            }
            JOptionPane.showMessageDialog(this, "Export CSV thành công!\n" + fc.getSelectedFile().getPath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    //  TableModel
    // =========================================================================

    static class TxTableModel extends AbstractTableModel {
        private static final String[] COLS = {
            "ID", "WalletID", "Loại", "Số tiền (VNĐ)", "Trạng thái", "Thời gian"
        };
        private List<Transaction> rows = new ArrayList<>();
        private static final NumberFormat FMT = NumberFormat.getNumberInstance(new Locale("vi","VN"));
        private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        void setData(List<Transaction> data) { rows = data; fireTableDataChanged(); }

        @Override public int getRowCount()    { return rows.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }
        @Override public boolean isCellEditable(int r, int c) { return false; }

        @Override public Object getValueAt(int r, int c) {
            Transaction t = rows.get(r);
            return switch (c) {
                case 0 -> t.getTransactionId();
                case 1 -> t.getWalletId();
                case 2 -> typeLabel(t.getTypeCode());
                case 3 -> t.getAmount() != null ? FMT.format(t.getAmount()) : "—";
                case 4 -> t.getStatus() != null ? t.getStatus() : "—";
                case 5 -> t.getCreatedAt() != null ? t.getCreatedAt().format(DT) : "—";
                default -> "";
            };
        }

        private String typeLabel(String code) {
            if (code == null) return "—";
            return switch (code) {
                case "DEPOSIT"  -> "Nạp";
                case "WITHDRAW" -> "Rút";
                case "INVEST"   -> "Đầu tư";
                case "PAYOUT"   -> "Lãi";
                case "BONUS"    -> "Thưởng";
                default -> code;
            };
        }
    }

    // =========================================================================
    //  UI Helpers
    // =========================================================================

    private LocalDate parseDate(String s, LocalDate fallback) {
        try { return LocalDate.parse(s.trim(), DATE_FMT); }
        catch (Exception e) { return fallback; }
    }

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

    private JLabel filterLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTED);
        return l;
    }
}
