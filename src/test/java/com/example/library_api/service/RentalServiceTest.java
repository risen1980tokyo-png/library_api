package com.example.library_api.service;

import com.example.library_api.entity.Book;
import com.example.library_api.entity.Rental; // 追加
import com.example.library_api.entity.User;
import com.example.library_api.repository.BookRepository;
import com.example.library_api.repository.RentalRepository; // 追加
import com.example.library_api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RentalServiceTest {

    @Autowired
    private RentalService rentalService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RentalRepository rentalRepository; // 追加

    @Test
    @DisplayName("貸出が成功し、在庫が減ることを確認するテスト")
    void testRentBookSuccess() {
        // 1. 本の作成
        Book book = new Book();
        book.setTitle("テスト用の本");
        book.setAuthor("テスト著者");
        book.setIsbn("TEST-ISBN-123");
        book.setStock(5);
        Book savedBook = bookRepository.saveAndFlush(book);

        // 2. ユーザーの作成
        User user = new User();
        user.setName("テストユーザー");
        user.setEmail("test@example.com");
        User savedUser = userRepository.saveAndFlush(user);

        // 3. 実行
        rentalService.rentBook(savedBook.getId(), savedUser.getId());

        bookRepository.flush();

        // 4. 検証：在庫が5から4に減っていることを確認
        Book updatedBook = bookRepository.findById(savedBook.getId()).orElseThrow();
        assertEquals(4, updatedBook.getStock(), "在庫が1減っているはずです");

        // 5. 【修正ポイント】Rentalテーブルに正しい履歴があるか確認する
        // getBorrower() は使わず、RentalRepository から貸出中の情報を取ってきます
        List<Rental> rentals = rentalRepository.findByUserIdAndReturnedFalse(savedUser.getId());

        assertFalse(rentals.isEmpty(), "貸出履歴が存在するはずです");
        assertEquals(savedBook.getId(), rentals.get(0).getBookId(), "借りた本のIDが一致するはずです");
    }

    @Test
    @DisplayName("他人のIDで返却しようとすると例外が発生すること")
    void testRentAndReturnFail_DifferentUser() {
        // 1. 準備
        Book book = new Book();
        book.setTitle("テスト用の本");
        book.setAuthor("テスト著者");
        book.setStock(5);
        Book savedBook = bookRepository.saveAndFlush(book);

        User user = new User();
        user.setName("テストユーザー");
        User savedUser = userRepository.saveAndFlush(user);

        // Aさんが借りる
        rentalService.rentBook(savedBook.getId(), savedUser.getId());

        // 2. 実行＆検証：BさんのID（999L）で返却を試みる
        Long strangerUserId = 999L;

        // RentalService.returnBook 内で履歴が見つからず RuntimeException が出ることを確認
        assertThrows(RuntimeException.class, () -> {
            rentalService.returnBook(savedBook.getId(), strangerUserId);
        });
    }
}