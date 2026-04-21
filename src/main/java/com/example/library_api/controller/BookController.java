package com.example.library_api.controller;

import com.example.library_api.dto.RentalRequest;
import com.example.library_api.entity.Book;
import com.example.library_api.repository.BookRepository;
import com.example.library_api.service.BookService;
import com.example.library_api.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 蔵書管理APIコントローラー
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository repository;
    private final BookService bookService;
    private final RentalService rentalService;

    /**
     * 全ての蔵書をタイトル順に取得
     */
    @GetMapping
    public List<Book> getAllBooks() {
        return repository.findAll().stream()
                .sorted((a, b) -> a.getTitle().compareTo(b.getTitle()))
                .toList();
    }

    /**
     * 本の保存・更新
     * バリデーション失敗時は自動的に GlobalExceptionHandler が 400 を返します
     */
    @PostMapping
    public Book saveBook(@Valid @RequestBody Book book) {
        return bookService.saveBook(book);
    }

    /**
     * 特定の本の詳細情報を取得
     */
    @GetMapping("/{id}")
    public Book getBook(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("指定された本が見つかりません。"));
    }

    /**
     * 本の貸出処理
     */
    @PostMapping("/rent")
    public String rentBook(@RequestBody RentalRequest request) {
        rentalService.rentBook(request.bookId(), request.userId());
        return "貸出処理が完了しました！";
    }

    /**
     * 本の削除
     */
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }

    /**
     * タイトルによる部分一致検索
     */
    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String q) {
        return repository.findByTitleContainingIgnoreCase(q);
    }
}