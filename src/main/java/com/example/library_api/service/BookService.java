package com.example.library_api.service;

import com.example.library_api.entity.Book;
import com.example.library_api.repository.BookRepository;
import com.example.library_api.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 蔵書管理ビジネスロジック
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final RentalRepository rentalRepository;

    /**
     * 本の情報を保存または更新します。
     * 新規登録時には、ISBNおよびタイトル・著者の重複チェックを行います。
     */
    @Transactional
    public Book saveBook(Book book) {
        // 1. 在庫チェックなどのガード節（既存通り）
        if (book.getStock() == null || book.getStock() < 0) {
            throw new RuntimeException("⚠️ 在庫数に負の値を設定することはできません。");
        }

        if (book.getId() != null) {
            // --- 更新時の処理：既存データを取得して情報を引き継ぐ ---
            Book existingBook = bookRepository.findById(book.getId())
                    .orElseThrow(() -> new RuntimeException("⚠️ 更新対象の本が見つかりません。"));

            // 作成時刻を既存データから引き継ぐ（重要！）
            book.setCreatedAt(existingBook.getCreatedAt());

            // もし著者やISBNが空で送られてきた場合に既存値を維持する補完
            if (book.getAuthor() == null || book.getAuthor().isBlank()) {
                book.setAuthor(existingBook.getAuthor());
            }
            if (book.getIsbn() == null || book.getIsbn().isBlank()) {
                book.setIsbn(existingBook.getIsbn());
            }
        } else {
            // --- 新規登録時の処理 ---
            // 重複チェック（既存通り）
            if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                if (bookRepository.existsByIsbn(book.getIsbn())) {
                    throw new RuntimeException("⚠️ このISBNは既に登録済みです。");
                }
            }
            if (bookRepository.existsByTitleAndAuthor(book.getTitle(), book.getAuthor())) {
                throw new RuntimeException("⚠️ 同じタイトル・著者の本が既に存在します。");
            }

            // 著者が未入力なら補完
            if (book.getAuthor() == null || book.getAuthor().isBlank()) {
                book.setAuthor("（未設定）");
            }
        }

        log.info("本の保存（更新）を実行: {}", book.getTitle());
        return bookRepository.save(book);
    }

    /**
     * 本を削除します。
     * 貸出中の履歴が存在する場合は削除を拒否します。
     */
    @Transactional
    public void deleteBook(Long id) {
        // 1. 存在確認（ガード節）
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("⚠️ 削除対象の本が見つかりません。");
        }

        // 2. 貸出中（未返却）チェック
        boolean isBeingBorrowed = !rentalRepository.findByBookIdAndReturnedFalse(id).isEmpty();
        if (isBeingBorrowed) {
            throw new RuntimeException("⚠️ この本は現在貸出中のため削除できません。すべての返却が完了してから削除してください。");
        }

        log.info("本の削除を実行します: ID {}", id);
        bookRepository.deleteById(id);
    }
}