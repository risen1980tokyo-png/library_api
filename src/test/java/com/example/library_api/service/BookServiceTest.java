package com.example.library_api.service;

import com.example.library_api.entity.Book;
import com.example.library_api.entity.Rental;
import com.example.library_api.repository.BookRepository;
import com.example.library_api.repository.RentalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 本管理機能のユニットテスト
 * バリデーション、ISBN重複、削除ガードのロジックを検証します。
 */
@SpringBootTest
@Transactional
public class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RentalRepository rentalRepository;

    // --- 1. バリデーション：入力チェック ---

    @Test
    @DisplayName("バリデーション：タイトルが未入力の場合は保存に失敗すること")
    void testSaveBook_TitleRequired() {
        Book book = new Book();
        book.setAuthor("テスト著者");
        book.setStock(1);
        book.setIsbn("ISBN-001");

        assertThrows(RuntimeException.class, () -> {
            bookService.saveBook(book);
        });
    }

    @Test
    @DisplayName("バリデーション：著者が空文字の場合は保存に失敗すること")
    void testSaveBook_AuthorRequired() {
        Book book = new Book();
        book.setTitle("テスト本");
        book.setAuthor("");
        book.setStock(1);

        assertThrows(RuntimeException.class, () -> {
            bookService.saveBook(book);
        });
    }

    @Test
    @DisplayName("バリデーション：在庫数がマイナスの場合は保存に失敗すること")
    void testSaveBook_NegativeStockFail() {
        Book book = new Book();
        book.setTitle("マイナス在庫の本");
        book.setAuthor("テスト著者");
        book.setStock(-1);

        assertThrows(RuntimeException.class, () -> {
            bookService.saveBook(book);
        });
    }

    // --- 2. 一意性チェック：重複登録の防止 ---

    @Test
    @DisplayName("一意性チェック：同じタイトルと著者の組み合わせは登録できないこと")
    void testDuplicateTitleAndAuthorFail() {
        Book book1 = new Book();
        book1.setTitle("重複チェック本");
        book1.setAuthor("著者A");
        book1.setStock(1);
        bookRepository.saveAndFlush(book1);

        Book book2 = new Book();
        book2.setTitle("重複チェック本");
        book2.setAuthor("著者A");

        assertThrows(RuntimeException.class, () -> {
            bookService.saveBook(book2);
        });
    }

    @Test
    @DisplayName("一意性チェック：同じISBNの本は登録できないこと")
    void testDuplicateIsbnFail() {
        String commonIsbn = "978-4-0000-0000-0";

        Book book1 = new Book();
        book1.setTitle("本1");
        book1.setAuthor("著者1");
        book1.setIsbn(commonIsbn);
        bookRepository.saveAndFlush(book1);

        Book book2 = new Book();
        book2.setTitle("本2");
        book2.setIsbn(commonIsbn);

        assertThrows(RuntimeException.class, () -> {
            bookService.saveBook(book2);
        });
    }

    // --- 3. 削除制限：データ整合性の保護 ---

    @Test
    @DisplayName("削除制限：未返却の貸出履歴がある本は削除できないこと")
    void testDeleteBook_FailIfRentalActive() {
        // 1. 本を登録
        Book book = new Book();
        book.setTitle("貸出中の本");
        book.setAuthor("テスト著者");
        book.setStock(1);
        book = bookRepository.saveAndFlush(book);

        // 2. 「未返却」の貸出データを作成
        Rental rental = new Rental(book.getId(), 1L);
        rentalRepository.saveAndFlush(rental);

        // 3. 削除実行とエラーメッセージの検証
        final Long bookId = book.getId();
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookService.deleteBook(bookId);
        });

        assertTrue(exception.getMessage().contains("貸出中のため削除できません"));
    }
}