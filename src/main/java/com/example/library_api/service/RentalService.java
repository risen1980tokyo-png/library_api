package com.example.library_api.service;

import com.example.library_api.entity.Book;
import com.example.library_api.entity.Rental;
import com.example.library_api.entity.User;
import com.example.library_api.exception.OutOfStockException;
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

/**
 * 貸出・返却管理ビジネスロジック
 * 在庫の排他制御、延滞チェック、貸出制限ルールの適用を担当します。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

    /**
     * 貸出処理を実行します。
     * 延滞、貸出冊数制限、二重貸出、在庫不足をチェックし、在庫を1減らします。
     * * @param bookId 貸出対象の本ID
     * @param userId 貸出を受けるユーザーID
     * @return 更新された本エンティティ
     */
    @Transactional
    public Book rentBook(Long bookId, Long userId) {
        // 1. 基本存在チェック（findByIdWithLockで同時実行時の在庫不整合を防止）
        Book book = bookRepository.findByIdWithLock(bookId)
                .orElseThrow(() -> new RuntimeException("⚠️ 指定された本が見つかりません。"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("⚠️ 利用者が存在しません。"));

        List<Rental> activeRentals = rentalRepository.findByUserIdAndReturnedFalse(userId);
        LocalDate today = LocalDate.now();

        // 2. 延滞チェック（ガード節）
        for (Rental rental : activeRentals) {
            if (rental.getDueDate().isBefore(today.atStartOfDay())) {
                throw new RuntimeException("⚠️ 延滞中の本があります。先に返却してください。");
            }
        }

        // 3. 貸出冊数制限（最大5冊）
        if (activeRentals.size() >= 5) {
            throw new RuntimeException("⚠️ 貸出上限（5冊）に達しています。");
        }

        // 4. 同一書籍の二重貸出制限
        if (rentalRepository.existsByUserIdAndBookIdAndReturnedFalse(userId, bookId)) {
            throw new RuntimeException("⚠️ この本は既に貸出中です。同じ本は一人一冊までです。");
        }

        // 5. 在庫チェック
        if (book.getStock() == null || book.getStock() <= 0) {
            throw new OutOfStockException("在庫がないため貸出できません。");
        }

        // 在庫更新
        book.setStock(book.getStock() - 1);
        bookRepository.saveAndFlush(book);

        // 貸出履歴作成
        Rental rental = new Rental(bookId, userId);
        rental.setRentalDate(LocalDateTime.now());
        rental.setDueDate(LocalDateTime.now().plusDays(7)); // 貸出期間: 7日間
        rental.setReturned(false);
        rentalRepository.saveAndFlush(rental);

        log.info("貸出完了: Book='{}', User='{}'", book.getTitle(), user.getName());
        return book;
    }

    /**
     * 返却処理を実行します。
     * 貸出記録を「返却済み」に更新し、在庫を1増やします。
     * * @param bookId 返却する本ID
     * @param userId 返却するユーザーID
     * @return 更新された本エンティティ
     */
    @Transactional
    public Book returnBook(Long bookId, Long userId) {
        // 1. 貸出記録の存在確認
        Rental rental = rentalRepository.findFirstByUserIdAndBookIdAndReturnedFalse(userId, bookId)
                .orElseThrow(() -> new RuntimeException("⚠️ 有効な貸出記録（未返却）が見つかりません。"));

        // 2. 本の存在確認（在庫更新のためのロック取得）
        Book book = bookRepository.findByIdWithLock(bookId)
                .orElseThrow(() -> new RuntimeException("⚠️ 本が見つかりません。"));

        // 3. 履歴を返却済みに更新
        rental.setReturned(true);
        rental.setReturnDate(LocalDateTime.now());
        rentalRepository.saveAndFlush(rental);

        // 4. 在庫を復元
        int currentStock = (book.getStock() == null) ? 0 : book.getStock();
        book.setStock(currentStock + 1);
        bookRepository.saveAndFlush(book);

        log.info("返却完了: BookID={}, UserID={}, 在庫: {} -> {}", bookId, userId, currentStock, book.getStock());
        return book;
    }

    // --- 参照系メソッド ---

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