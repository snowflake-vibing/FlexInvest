package View;

import Controller.NotificationController;
import Model.Notification;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * NotificationDialog — JDialog dạng dropdown danh sách thông báo.
 * Mở bằng cách bấm icon chuông trong Sidebar.
 *
 * Tính năng:
 *  - Hiển thị danh sách thông báo gần nhất (tối đa 30)
 *  - Phân biệt đã đọc / chưa đọc bằng màu nền
 *  - Nút "Đánh dấu tất cả đã đọc"
 *  - Sau khi đóng, trả về callback để Sidebar cập nhật badge
 */
public class NotificationDialog extends JDialog {

    private static final Color BG        = UIUtils.APP_BG;
    private static final Color CARD_NEW  = new Color(238, 242, 255);   // chưa đọc
    private static final Color CARD_OLD  = Color.WHITE;                // đã đọc
    private static final Color TEXT_MUTED = UIUtils.TEXT_MUTED;
    private static final Color RED       = new Color(239, 68, 68);

    // Font Awesome để hiển thị icon (tránh lỗi emoji/unicode trên Swing/Windows)
    private static final Font FA_FONT;
    static {
        Font f;
        try {
            f = Font.createFont(Font.TRUETYPE_FONT,
                new java.io.File("src/Resources/fa-solid-900.ttf")).deriveFont(14f);
        } catch (Exception e) {
            f = new Font("Segoe UI", Font.PLAIN, 14);
        }
        FA_FONT = f;
    }


    private final int                    userId;
    private final NotificationController notifCtrl;
    private final Runnable               onDismiss;   // callback cập nhật badge

    private JPanel listPanel;

    /**
     * @param parent    parent frame để định vị dialog
     * @param userId    ID user hiện tại
     * @param onDismiss Callback sau khi dialog đóng (để Sidebar cập nhật badge)
     */
    public NotificationDialog(Frame parent, int userId,
                              NotificationController notifCtrl, Runnable onDismiss) {
        super(parent, "Thông báo", false);   // false = non-modal để không block UI
        this.userId    = userId;
        this.notifCtrl = notifCtrl;
        this.onDismiss = onDismiss;

        setSize(420, 540);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                if (onDismiss != null) onDismiss.run();
            }
        });
        
        // Tự động đóng khi click ra ngoài, nhưng dùng Timer delay để tránh
        // đóng nhầm khi focus chuyển sang child component bên trong dialog
        final javax.swing.Timer[] closeTimer = { null };
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override public void windowGainedFocus(java.awt.event.WindowEvent e) {
                // Huỷ timer nếu focus quay lại dialog
                if (closeTimer[0] != null) {
                    closeTimer[0].stop();
                    closeTimer[0] = null;
                }
            }
            @Override public void windowLostFocus(java.awt.event.WindowEvent e) {
                // Delay 200ms trước khi đóng — nếu focus quay lại thì hủy
                closeTimer[0] = new javax.swing.Timer(200, ev -> dispose());
                closeTimer[0].setRepeats(false);
                closeTimer[0].start();
            }
        });

        build();
        loadNotifications();
    }

    // =========================================================================
    //  Build
    // =========================================================================

    private void build() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(UIUtils.NAVY);
        header.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Icon chuông dùng Font Awesome — tránh lỗi emoji không render trên Swing/Windows
        JLabel iconBell = new JLabel("\uf0f3");
        iconBell.setFont(FA_FONT.deriveFont(16f));
        iconBell.setForeground(Color.WHITE);

        JLabel lblTitle = new JLabel("Thông báo");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(iconBell);
        titlePanel.add(lblTitle);

        // Nút đánh dấu — icon check dùng Font Awesome
        JPanel btnMarkAllPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btnMarkAllPanel.setOpaque(false);

        JLabel iconCheck = new JLabel("\uf00c");
        iconCheck.setFont(FA_FONT.deriveFont(11f));
        iconCheck.setForeground(Color.WHITE);

        JLabel lblMarkAll = new JLabel("Đánh dấu tất cả đã đọc");
        lblMarkAll.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMarkAll.setForeground(Color.WHITE);
        lblMarkAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnMarkAllPanel.add(iconCheck);
        btnMarkAllPanel.add(lblMarkAll);

        // Wrap trong panel có background để hover effect
        JPanel btnMarkAllWrapper = new JPanel(new BorderLayout());
        btnMarkAllWrapper.setBackground(new Color(30, 65, 115));
        btnMarkAllWrapper.setBorder(new EmptyBorder(4, 10, 4, 10));
        btnMarkAllWrapper.add(btnMarkAllPanel, BorderLayout.CENTER);
        btnMarkAllWrapper.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMarkAllWrapper.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btnMarkAllWrapper.setBackground(new Color(50, 90, 150));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btnMarkAllWrapper.setBackground(new Color(30, 65, 115));
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                notifCtrl.markAllRead(userId);
                loadNotifications();
                if (onDismiss != null) onDismiss.run();
            }
        });

        header.add(titlePanel,       BorderLayout.WEST);
        header.add(btnMarkAllWrapper, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Scrollable list ───────────────────────────────────────────────────
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(BG);
        listPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane sp = new JScrollPane(listPanel);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(sp, BorderLayout.CENTER);
    }


    // =========================================================================
    //  Load
    // =========================================================================

    private void loadNotifications() {
        new SwingWorker<List<Notification>, Void>() {
            @Override protected List<Notification> doInBackground() {
                return notifCtrl.getRecent(userId, 30);
            }
            @Override protected void done() {
                try {
                    List<Notification> list = get();
                    renderList(list);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void renderList(List<Notification> list) {
        listPanel.removeAll();

        if (list.isEmpty()) {
            JLabel empty = new JLabel("Bạn chưa có thông báo nào.", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(TEXT_MUTED);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalStrut(40));
            listPanel.add(empty);
        } else {
            for (Notification n : list) {
                listPanel.add(buildCard(n));
                listPanel.add(Box.createVerticalStrut(6));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildCard(Notification n) {
        boolean unread = n.isUnread();

        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(unread ? CARD_NEW : CARD_OLD);
        card.setBorder(new EmptyBorder(10, 14, 10, 14));
        card.putClientProperty("FlatLaf.style", "arc: 12;");
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lbTitle = new JLabel(n.getTitle() != null ? n.getTitle() : "(Không tiêu đề)");
        lbTitle.setFont(new Font("Segoe UI", unread ? Font.BOLD : Font.PLAIN, 13));
        lbTitle.setForeground(unread ? UIUtils.NAVY : new Color(60, 70, 90));

        JLabel lbTime = new JLabel(formatTime(n));
        lbTime.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbTime.setForeground(TEXT_MUTED);

        top.add(lbTitle, BorderLayout.CENTER);
        top.add(lbTime,  BorderLayout.EAST);

        JLabel lbBody = new JLabel();
        if (n.getBody() != null && !n.getBody().isBlank()) {
            String body = n.getBody().length() > 80
                ? n.getBody().substring(0, 77) + "..." : n.getBody();
            lbBody.setText("<html><span style='color:#6e7382;font-size:11px'>" + body + "</span></html>");
        }

        // Unread dot
        JLabel dot = new JLabel(unread ? "●" : "");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dot.setForeground(UIUtils.ACCENT);
        dot.setPreferredSize(new Dimension(14, 14));

        card.add(dot,  BorderLayout.WEST);
        card.add(top,  BorderLayout.CENTER);
        if (n.getBody() != null && !n.getBody().isBlank())
            card.add(lbBody, BorderLayout.SOUTH);

        // Click → mark read
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (unread) {
                    notifCtrl.markRead(n.getNotificationId());
                    loadNotifications();
                    if (onDismiss != null) onDismiss.run();
                }
            }
        });

        return card;
    }

    private String formatTime(Notification n) {
        if (n.getSentAt() == null) return "";
        try {
            return n.getSentAt().toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
        } catch (Exception e) { return ""; }
    }

    // =========================================================================
    //  Static factory — mở dialog cạnh icon chuông
    // =========================================================================

    /**
     * Mở dialog cạnh component nguồn (icon chuông).
     * Dùng trong Sidebar: NotificationDialog.open(frame, bellBtn, userId, ctrl, onDismiss);
     */
    public static NotificationDialog open(Frame frame, Component anchor,
                            int userId, NotificationController ctrl,
                            Runnable onDismiss) {
        NotificationDialog dlg = new NotificationDialog(frame, userId, ctrl, onDismiss);

        // Định vị dialog sát icon chuông
        Point loc = anchor.getLocationOnScreen();
        int x = loc.x - dlg.getWidth() + anchor.getWidth();
        int y = loc.y + anchor.getHeight() + 4;

        // Đảm bảo không bị cắt ngoài màn hình
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (x + dlg.getWidth() > screen.width) x = screen.width - dlg.getWidth() - 8;
        if (y + dlg.getHeight() > screen.height) y = loc.y - dlg.getHeight() - 4;

        dlg.setLocation(x, y);
        dlg.setVisible(true);
        return dlg;
    }
}
