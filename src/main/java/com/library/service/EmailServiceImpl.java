package com.library.service;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private static final String LIBRARY_NAME = "Hệ thống Quản lý Thư viện";
    private static final String FROM_ADDRESS = "no-reply@library.com";

    private final JavaMailSender mailSender;

    @Override
    public void sendBorrowConfirmation(User user, Book book, BorrowRecord borrowRecord) {
        String subject = "[Thư viện] Xác nhận mượn sách thành công";
        String content = buildBorrowConfirmationHtml(user, book, borrowRecord);
        sendHtmlEmail(user.getEmail(), subject, content);
    }

    @Override
    public void sendReturnConfirmation(User user, Book book, BorrowRecord borrowRecord) {
        String subject = "[Thư viện] Xác nhận trả sách thành công";
        String content = buildReturnConfirmationHtml(user, book, borrowRecord);
        sendHtmlEmail(user.getEmail(), subject, content);
    }

    @Override
    public void sendOverdueReminder(User user, Book book, BorrowRecord borrowRecord) {
        String subject = "[Thư viện] Nhắc nhở: Sách sắp đến hạn trả";
        String content = buildOverdueReminderHtml(user, book, borrowRecord);
        sendHtmlEmail(user.getEmail(), subject, content);
    }

    @Override
    public void sendBookAvailableNotification(User user, Book book) {
        String subject = "[Thư viện] Sách bạn quan tâm đã có sẵn";
        String content = buildBookAvailableHtml(user, book);
        sendHtmlEmail(user.getEmail(), subject, content);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(FROM_ADDRESS);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.debug("Đã gửi email '{}' đến {}", subject, to);
        } catch (Exception e) {
            log.warn("Không thể gửi email '{}' đến {}: {}", subject, to, e.getMessage());
        }
    }

    private String buildBorrowConfirmationHtml(User user, Book book, BorrowRecord record) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 600px; margin: auto; background: #fff; border-radius: 8px;
                              padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #2e7d32;">✅ Mượn sách thành công</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Bạn đã mượn sách thành công. Dưới đây là thông tin chi tiết:</p>
                    <table style="width: 100%%; border-collapse: collapse; margin: 16px 0;">
                      <tr style="background-color: #e8f5e9;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Tên sách</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Tác giả</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                      </tr>
                      <tr style="background-color: #e8f5e9;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Ngày mượn</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Hạn trả</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd; color: #c62828;"><strong>%s</strong></td>
                      </tr>
                    </table>
                    <p style="color: #555;">Vui lòng trả sách đúng hạn để tránh bị phạt. Cảm ơn bạn!</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px;">%s</p>
                  </div>
                </body>
                </html>
                """.formatted(
                user.getFullName(),
                book.getTitle(),
                book.getAuthor(),
                record.getBorrowDate(),
                record.getDueDate(),
                LIBRARY_NAME
        );
    }

    private String buildReturnConfirmationHtml(User user, Book book, BorrowRecord record) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 600px; margin: auto; background: #fff; border-radius: 8px;
                              padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #1565c0;">📚 Trả sách thành công</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Bạn đã trả sách thành công. Cảm ơn bạn đã sử dụng thư viện!</p>
                    <table style="width: 100%%; border-collapse: collapse; margin: 16px 0;">
                      <tr style="background-color: #e3f2fd;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Tên sách</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Tác giả</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                      </tr>
                      <tr style="background-color: #e3f2fd;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Ngày mượn</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Ngày trả thực tế</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                      </tr>
                    </table>
                    <p style="color: #555;">Hẹn gặp lại bạn lần sau tại thư viện!</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px;">%s</p>
                  </div>
                </body>
                </html>
                """.formatted(
                user.getFullName(),
                book.getTitle(),
                book.getAuthor(),
                record.getBorrowDate(),
                record.getReturnDate(),
                LIBRARY_NAME
        );
    }

    private String buildOverdueReminderHtml(User user, Book book, BorrowRecord record) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 600px; margin: auto; background: #fff; border-radius: 8px;
                              padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #e65100;">⚠️ Nhắc nhở: Sách sắp đến hạn trả</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Cuốn sách dưới đây sẽ <strong style="color: #c62828;">đến hạn trả vào ngày mai</strong>.
                       Vui lòng trả sách đúng hạn để tránh bị tính phí phạt.</p>
                    <table style="width: 100%%; border-collapse: collapse; margin: 16px 0;">
                      <tr style="background-color: #fff3e0;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Tên sách</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Tác giả</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                      </tr>
                      <tr style="background-color: #fff3e0;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Hạn trả</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd; color: #c62828;"><strong>%s</strong></td>
                      </tr>
                    </table>
                    <p style="color: #555;">Nếu bạn cần gia hạn, vui lòng liên hệ trực tiếp với thư viện.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px;">%s</p>
                  </div>
                </body>
                </html>
                """.formatted(
                user.getFullName(),
                book.getTitle(),
                book.getAuthor(),
                record.getDueDate(),
                LIBRARY_NAME
        );
    }

    private String buildBookAvailableHtml(User user, Book book) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 600px; margin: auto; background: #fff; border-radius: 8px;
                              padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #6a1b9a;">🔔 Sách bạn quan tâm đã có sẵn</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Cuốn sách bạn đã đặt chờ hiện đã có sẵn tại thư viện. Hãy đến mượn sớm!</p>
                    <table style="width: 100%%; border-collapse: collapse; margin: 16px 0;">
                      <tr style="background-color: #f3e5f5;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Tên sách</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Tác giả</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                      </tr>
                      <tr style="background-color: #f3e5f5;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Số bản hiện có</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%d bản</td>
                      </tr>
                    </table>
                    <p style="color: #555;">Số lượng có hạn, vui lòng đến thư viện sớm để mượn sách.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px;">%s</p>
                  </div>
                </body>
                </html>
                """.formatted(
                user.getFullName(),
                book.getTitle(),
                book.getAuthor(),
                book.getAvailableCopies(),
                LIBRARY_NAME
        );
    }
}
