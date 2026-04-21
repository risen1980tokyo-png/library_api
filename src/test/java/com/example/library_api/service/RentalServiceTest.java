package com.example.library_api.service;

import com.example.library_api.entity.Book;
import com.example.library_api.entity.Rental;
import com.example.library_api.entity.User;
import com.example.library_api.repository.BookRepository;
import com.example.library_api.repository.RentalRepository;
import com.example.library_api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private RentalRepository rentalRepository;

    // --- 正常系テスト ---

    @Test
    @DisplayName("貸出が成功し、在庫が減ることを確認するテスト")
    void testRentBookSuccess() {
        Book book = createBook("テスト用の本", 5);
        User user = createUser("テストユーザー");

        rentalService.rentBook(book.getId(), user.getId());
        bookRepository.flush();

        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertEquals(4, updatedBook.getStock(), "在庫が1減っているはずです");

        List<Rental> rentals = rentalRepository.findByUserIdAndReturnedFalse(user.getId());
        assertFalse(rentals.isEmpty());
    }

    // --- 異常系テスト（ここが履歴書アピールポイント） ---

    @Test
    @DisplayName("在庫が0の本は貸出できないこと")
    void testRentBookFail_NoStock() {
        Book book = createBook("在庫なしの本", 0);
        User user = createUser("テストユーザー");

        // 在庫0なので、RuntimeExceptionが発生することを期待
        assertThrows(RuntimeException.class, () -> {
            rentalService.rentBook(book.getId(), user.getId());
        });
    }

    @Test
    @DisplayName("延滞中のユーザー（期限が昨日以前）は新しい本を借りられないこと")
    void testRentBookFail_OverdueUser() {
        User user = createUser("延滞太郎");
        Book overdueBook = createBook("延滞されている本", 5);

        // 1. 意図的に「期限が昨日」の貸出データを作成して保存
        Rental rental = new Rental();
        rental.setUserId(user.getId());
        rental.setBookId(overdueBook.getId());
        rental.setDueDate(LocalDateTime.now().minusDays(1)); // 期限：昨日
        rental.setReturned(false);
        rentalRepository.saveAndFlush(rental);

        // 2. 実行：新しい本を借りようとすると拒否されることを確認
        Book newBook = createBook("新しい本", 5);
        assertThrows(RuntimeException.class, () -> {
            rentalService.rentBook(newBook.getId(), user.getId());
        }, "延滞中なので例外が発生するはずです");
    }

    @Test
    @DisplayName("期限が「今日」の本があっても、まだ新しい本を借りられること（本日中セーフ）")
    void testRentBookSuccess_DueToday() {
        User user = createUser("今日返却予定の人");
        Book dueTodayBook = createBook("今日が期限の本", 5);

        // 1. 期限が「今日」の貸出データを作成
        Rental rental = new Rental();
        rental.setUserId(user.getId());
        rental.setBookId(dueTodayBook.getId());
        rental.setDueDate(LocalDateTime.now()); // 期限：今日
        rental.setReturned(false);
        rentalRepository.saveAndFlush(rental);

        // 2. 実行：新しい本を借りる（成功するはず）
        Book newBook = createBook("借りられる本", 5);
        assertDoesNotThrow(() -> {
            rentalService.rentBook(newBook.getId(), user.getId());
        }, "期限当日ならまだ借りられるはずです");
    }

    @Test
    @DisplayName("他人の貸出情報を返却しようとすると例外が発生すること")
    void testReturnFail_DifferentUser() {
        Book book = createBook("貸出中の本", 5);
        User userA = createUser("ユーザーA");
        User userB = createUser("ユーザーB");

        rentalService.rentBook(book.getId(), userA.getId());

        // 実行＆検証：BさんのIDで返却を試みる
        assertThrows(RuntimeException.class, () -> {
            rentalService.returnBook(book.getId(), userB.getId());
        });
    }

    // --- 補助メソッド（テストコードを読みやすくするため） ---

    private Book createBook(String title, int stock) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor("テスト著者");
        book.setIsbn("ISBN-" + System.currentTimeMillis());
        book.setStock(stock);
        return bookRepository.saveAndFlush(book);
    }

    private User createUser(String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(name + "@example.com");
        return userRepository.saveAndFlush(user);
    }
}