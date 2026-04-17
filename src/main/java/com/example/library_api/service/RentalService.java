package com.example.library_api.service;

import com.example.library_api.entity.Book;
import com.example.library_api.entity.Rental;
import com.example.library_api.entity.User;
import com.example.library_api.repository.BookRepository;
import com.example.library_api.repository.RentalRepository;
import com.example.library_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

    /**
     * 貸出処理
     */
    @Transactional
    public Book rentBook(Long bookId, Long userId) {
        Book book = bookRepository.findByIdWithLock(bookId)
                .orElseThrow(() -> new RuntimeException("本が見つかりません"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("利用者が存在しません"));

        List<Rental> activeRentals = rentalRepository.findByUserIdAndReturnedFalse(userId);
        LocalDate today = LocalDate.now();

        // 1. 延滞チェック
        for (Rental rental : activeRentals) {
            if (rental.getDueDate().isBefore(today.atStartOfDay())) {
                throw new RuntimeException("延滞中の本があります。先に返却してください。");
            }
        }
        // 2. 5冊制限
        if (activeRentals.size() >= 5) {
            throw new RuntimeException("貸出上限（5冊）に達しています。");
        }
        // 3. 一人一冊制限（★Repositoryのexistsメソッドを使って、より明確に修正）
        if (rentalRepository.existsByUserIdAndBookIdAndReturnedFalse(userId, bookId)) {
            throw new RuntimeException("同じ本は一人一冊までです。既にこの本を借りています。");
        }
        // 4. 在庫チェック
        if (book.getStock() == null || book.getStock() <= 0) {
            throw new RuntimeException("在庫がないため貸出できません。");
        }

        // 在庫更新と保存（★saveAndFlush で即座にDBへ反映させ、不整合を防ぐ）
        book.setStock(book.getStock() - 1);
        bookRepository.saveAndFlush(book);

        Rental rental = new Rental(bookId, userId);
        rental.setRentalDate(LocalDateTime.now());
        rental.setDueDate(LocalDateTime.now().plusDays(7));
        rental.setReturned(false);
        rentalRepository.saveAndFlush(rental);

        log.info("貸出完了: BookID={}, UserID={}", bookId, userId);
        return book;
    }

    /**
     * 返却処理（★saveAndFlushを追加し、確実に在庫が戻るように修正）
     */
    @Transactional
    public Book returnBook(Long bookId, Long userId) {
        // 1. 対象の貸出記録を特定
        Rental rental = rentalRepository.findFirstByUserIdAndBookIdAndReturnedFalse(userId, bookId)
                .orElseThrow(() -> new RuntimeException("有効な貸出記録（未返却）が見つかりません"));

        // 2. 本を特定（在庫更新のためにロックを取得）
        Book book = bookRepository.findByIdWithLock(bookId)
                .orElseThrow(() -> new RuntimeException("本が見つかりません"));

        // 3. 履歴を「返却済み」に更新（★saveAndFlush で即座に反映）
        rental.setReturned(true);
        rental.setReturnDate(LocalDateTime.now());
        rentalRepository.saveAndFlush(rental);

        // 4. 在庫を確実に1増やす（★saveAndFlush で即座に反映）
        int beforeStock = (book.getStock() == null) ? 0 : book.getStock();
        book.setStock(beforeStock + 1);
        bookRepository.saveAndFlush(book);

        log.info("返却完了: BookID={}, UserID={}, 在庫: {} -> {}", bookId, userId, beforeStock, book.getStock());
        return book;
    }

    // --- 参照系メソッド（既存維持） ---
    public List<Rental> getActiveRentals(Long userId) {
        return rentalRepository.findByUserIdAndReturnedFalse(userId);
    }

    public List<Rental> getUserHistory(Long userId) {
        return rentalRepository.findByUserIdAndReturnedTrueOrderByReturnDateDesc(userId);
    }

    public List<Rental> getOverdueRentals() {
        return rentalRepository.findByDueDateBeforeAndReturnedFalse(LocalDateTime.now());
    }
}