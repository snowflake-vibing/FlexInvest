package Utils;

import Model.AccountModel;
import Model.Transaction;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PDFPrinter {

    public static void printInvoice(Transaction tx, AccountModel account) {
        try {
            // Lấy thư mục Downloads của người dùng
            String userHome = System.getProperty("user.home");
            String dest = userHome + "/Downloads/HoaDon_" + tx.getTransactionId() + ".pdf";
            
            Document document = new Document(PageSize.A5);
            PdfWriter.getInstance(document, new FileOutputStream(dest));
            document.open();

            // Font hỗ trợ Tiếng Việt (Arial từ Windows)
            BaseFont bf = BaseFont.createFont("C:/Windows/Fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(bf, 18, Font.BOLD, BaseColor.BLUE);
            Font headerFont = new Font(bf, 14, Font.BOLD);
            Font normalFont = new Font(bf, 12, Font.NORMAL);
            Font italicFont = new Font(bf, 10, Font.ITALIC, BaseColor.GRAY);

            // Tiêu đề
            Paragraph p1 = new Paragraph("FLEXINVEST", titleFont);
            p1.setAlignment(Element.ALIGN_CENTER);
            document.add(p1);

            Paragraph p2 = new Paragraph("BIÊN LAI GIAO DỊCH", headerFont);
            p2.setAlignment(Element.ALIGN_CENTER);
            p2.setSpacingAfter(20f);
            document.add(p2);

            // Thông tin khách hàng
            document.add(new Paragraph("Khách hàng: " + account.getAccount().getUsername(), normalFont));
            document.add(new Paragraph("Email: " + account.getUser().getEmail(), normalFont));
            document.add(new Paragraph("Ngày in: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), italicFont));
            
            document.add(new Paragraph(" ")); // Dòng trống

            // Kẻ bảng thông tin giao dịch
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            addTableRow(table, "Mã giao dịch:", String.valueOf(tx.getTransactionId()), normalFont);
            
            String type = tx.getTypeCode();
            String typeVn = switch (type == null ? "" : type) {
                case "DEPOSIT" -> "Nạp tiền";
                case "WITHDRAW" -> "Rút tiền";
                case "INVEST" -> "Đầu tư";
                case "PAYOUT" -> "Nhận lãi";
                case "BONUS" -> "Thưởng";
                default -> type;
            };
            addTableRow(table, "Loại giao dịch:", typeVn, normalFont);

            NumberFormat vndFmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            String amountStr = (tx.getAmount() != null ? vndFmt.format(tx.getAmount()) : "0") + " VNĐ";
            addTableRow(table, "Số tiền:", amountStr, normalFont);

            String timeStr = tx.getCreatedAt() != null ? tx.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "—";
            addTableRow(table, "Thời gian:", timeStr, normalFont);

            addTableRow(table, "Trạng thái:", tx.getStatus(), normalFont);

            document.add(table);

            // Cảm ơn
            Paragraph p3 = new Paragraph("Cảm ơn quý khách đã sử dụng dịch vụ của FlexInvest!", italicFont);
            p3.setAlignment(Element.ALIGN_CENTER);
            p3.setSpacingBefore(30f);
            document.add(p3);

            document.close();

            // Mở file sau khi tạo
            File file = new File(dest);
            if (file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, "Lỗi khi in hóa đơn: " + e.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addTableRow(PdfPTable table, String col1, String col2, Font font) {
        PdfPCell cell1 = new PdfPCell(new Phrase(col1, font));
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setPaddingBottom(8f);
        
        PdfPCell cell2 = new PdfPCell(new Phrase(col2, font));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell2.setPaddingBottom(8f);
        
        table.addCell(cell1);
        table.addCell(cell2);
    }
}
