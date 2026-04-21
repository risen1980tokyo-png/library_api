package com.example.library_api.repository;

import com.example.library_api.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 貸出履歴データアクセス層
 * 貸出・返却の整合性チェック、制限ルールの判定、および履歴照会クエリを提供します。
 */
@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    // --- 1. 貸出・返却時の整合性チェック ---

    /**
     * 特定のユーザーが特定の書籍を現在借りているか（未返却か）確認します。
     * 「一人一冊制限」の判定に使用します。
     */
    boolean existsByUserIdAndBookIdAndReturnedFalse(Long userId, Long bookId);

    /**
     * 返却対象となる貸出記録を1件取得します。
     */
    Optional<Rental> findFirstByUserIdAndBookIdAndReturnedFalse(Long userId, Long bookId);


    // --- 2. ビジネスルール制限・ガード用 ---

    /**
     * ユーザーごとの現在の貸出中リストを取得します。
     * 「5冊制限」や「延滞チェック」の判定に使用します。
     */
    List<Rental> findByUserIdAndReturnedFalse(Long userId);

    /**
     * 特定の書籍の現在の貸出状況を確認します。
     * 「書籍削除時の貸出中ガード」に使用します。
     */
    List<Rental> findByBookIdAndReturnedFalse(Long bookId);


    // --- 3. 履歴照会・統計系 ---

    /**
     * ユーザーの過去の返却済み履歴を、返却日の新しい順に取得します。
     */
    List<Rental> findByUserIdAndReturnedTrueOrderByReturnDateDesc(Long userId);

    /**
     * 返却期限を過ぎている未返却の貸出記録を抽出します。
     */
    List<Rental> findByDueDateBeforeAndReturnedFalse(LocalDateTime dateTime);
}