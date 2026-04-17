package com.example.library_api.service;

import com.example.library_api.entity.Book;
import com.example.library_api.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    @Transactional
    public Book saveBook(Book book) {
        // 【物理的なガード】
        if (book.getStock() == null || book.getStock() < 1) {
            throw new RuntimeException("⚠️ 在庫は1冊以上で登録してください。");
        }

        if (book.getId() == null) {
            // ここに先ほどの「賢いチェック」を入れる
            if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                if (bookRepository.existsByIsbn(book.getIsbn())) {
                    throw new RuntimeException("このISBNは登録済みです");
                }
            }
            if (bookRepository.existsByTitleAndAuthor(book.getTitle(), book.getAuthor())) {
                throw new RuntimeException("同じタイトル・著者の本があります");
            }
        }
        return bookRepository.save(book);
    }
}