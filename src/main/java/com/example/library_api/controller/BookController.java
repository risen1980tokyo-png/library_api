package com.example.library_api.controller;

import com.example.library_api.dto.RentalRequest;
import com.example.library_api.entity.Book;
import com.example.library_api.repository.BookRepository;
import com.example.library_api.repository.RentalRepository;
import com.example.library_api.service.BookService;
import com.example.library_api.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository repository;
    private final RentalService rentalService;
    private final RentalRepository rentalRepository;
    private final BookService bookService; // ★追加

    // ★コンストラクタに bookService を追加
    public BookController(BookRepository repository, RentalService rentalService, RentalRepository rentalRepository, BookService bookService) {
        this.repository = repository;
        this.rentalService = rentalService;
        this.rentalRepository = rentalRepository;
        this.bookService = bookService;
    }

    // すべての本を取得（名前順にソート）
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = repository.findAll().stream()
                .sorted((a, b) -> a.getTitle().compareTo(b.getTitle()))
                .toList();
        return ResponseEntity.ok(books);
    }

    // 本の保存・更新（バリデーションと補完ロジックを統合）
    @PostMapping
    public ResponseEntity<?> saveBook(@Valid @RequestBody Book book, BindingResult result) {
        // 1. バリデーションチェック（Book.javaの@Min(0)や@NotBlankがここで効きます）
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors().get(0).getDefaultMessage());
        }

        // 2. 既存データがある場合の補完ロジック
        if (book.getId() != null) {
            Book existingBook = repository.findById(book.getId())
                    .orElse(null);

            if (existingBook == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("更新対象の本が見つかりません");
            }

            if (book.getAuthor() == null || book.getAuthor().isBlank()) {
                book.setAuthor(existingBook.getAuthor());
            }
            if (book.getIsbn() == null) {
                book.setIsbn(existingBook.getIsbn());
            }
            // ★重要：作成日時を引き継ぐ
            book.setCreatedAt(existingBook.getCreatedAt());
        } else {
            // 新規登録時
            if (book.getAuthor() == null || book.getAuthor().isBlank()) {
                book.setAuthor("（未設定）");
            }
        }

        try {
            // ★ここが最重要：repository.save ではなく bookService.saveBook を呼ぶ！
            Book savedBook = bookService.saveBook(book);
            return ResponseEntity.ok(savedBook);
        } catch (RuntimeException e) {
            // Serviceで投げた「ISBN重複」や「在庫0エラー」をここでキャッチして画面に返す
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 特定の本を取得（戻り値をResponseEntityに統一）
    @GetMapping("/{id}")
    public ResponseEntity<?> getBook(@PathVariable Long id) {
        return repository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("本が見つかりません"));
    }

    // 貸出処理
    @PostMapping("/rent")
    public ResponseEntity<String> rentBook(@RequestBody RentalRequest request) {
        try {
            rentalService.rentBook(request.bookId(), request.userId());
            return ResponseEntity.ok("貸出処理が完了しました！");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 削除処理（貸出中ガード付き）
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        boolean isBeingBorrowed = !rentalRepository.findByBookIdAndReturnedFalse(id).isEmpty();

        if (isBeingBorrowed) {
            return ResponseEntity.badRequest()
                    .body("この本は現在貸出中のため削除できません。すべての返却が完了してから削除してください。");
        }

        if (!repository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("削除対象の本が見つかりません");
        }

        repository.deleteById(id);
        return ResponseEntity.ok("削除しました");
    }

    // 検索処理
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String q) {
        List<Book> results = repository.findByTitleContainingIgnoreCase(q);
        return ResponseEntity.ok(results);
    }
}