package com.library.service;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import com.library.entity.Role;
import com.library.entity.User;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho EmailServiceImpl: kiem tra logic gui email va xu ly loi SMTP.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    private User sampleUser;
    private Book sampleBook;
    private BorrowRecord sampleBorrowRecord;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender);

        sampleUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("testuser@example.com")
                .fullName("Nguyen Van Test")
                .role(Role.MEMBER)
                .build();

        sampleBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .totalCopies(3)
                .availableCopies(2)
                .build();

        sampleBorrowRecord = BorrowRecord.builder()
                .id(10L)
                .user(sampleUser)
                .book(sampleBook)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(BorrowStatus.BORROWED)
                .build();

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(session));
    }

    @Test
    void sendBorrowConfirmation_GuiEmailThanhCong() {
        emailService.sendBorrowConfirmation(sampleUser, sampleBook, sampleBorrowRecord);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendReturnConfirmation_GuiEmailThanhCong() {
        sampleBorrowRecord.setReturnDate(LocalDate.now());
        sampleBorrowRecord.setStatus(BorrowStatus.RETURNED);

        emailService.sendReturnConfirmation(sampleUser, sampleBook, sampleBorrowRecord);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendOverdueReminder_GuiEmailThanhCong() {
        emailService.sendOverdueReminder(sampleUser, sampleBook, sampleBorrowRecord);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendBookAvailableNotification_GuiEmailThanhCong() {
        emailService.sendBookAvailableNotification(sampleUser, sampleBook);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendBorrowConfirmation_KhiSMTPLoi_KhongNemException() {
        doThrow(new MailSendException("SMTP server unavailable"))
                .when(mailSender).send(any(MimeMessage.class));

        assertThatNoException().isThrownBy(() ->
                emailService.sendBorrowConfirmation(sampleUser, sampleBook, sampleBorrowRecord));
    }

    @Test
    void sendReturnConfirmation_KhiSMTPLoi_KhongNemException() {
        sampleBorrowRecord.setReturnDate(LocalDate.now());
        doThrow(new MailSendException("Connection refused"))
                .when(mailSender).send(any(MimeMessage.class));

        assertThatNoException().isThrownBy(() ->
                emailService.sendReturnConfirmation(sampleUser, sampleBook, sampleBorrowRecord));
    }

    @Test
    void sendOverdueReminder_KhiSMTPLoi_KhongNemException() {
        doThrow(new MailSendException("Authentication failed"))
                .when(mailSender).send(any(MimeMessage.class));

        assertThatNoException().isThrownBy(() ->
                emailService.sendOverdueReminder(sampleUser, sampleBook, sampleBorrowRecord));
    }

    @Test
    void sendBookAvailableNotification_KhiSMTPLoi_KhongNemException() {
        doThrow(new MailSendException("Timeout"))
                .when(mailSender).send(any(MimeMessage.class));

        assertThatNoException().isThrownBy(() ->
                emailService.sendBookAvailableNotification(sampleUser, sampleBook));
    }
}
