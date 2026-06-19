package com.library.service;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;

public interface EmailService {

    void sendBorrowConfirmation(User user, Book book, BorrowRecord borrowRecord);

    void sendReturnConfirmation(User user, Book book, BorrowRecord borrowRecord);

    void sendOverdueReminder(User user, Book book, BorrowRecord borrowRecord);

    void sendBookAvailableNotification(User user, Book book);
}
