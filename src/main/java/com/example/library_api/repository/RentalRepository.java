package com.example.library_api.repository;

import com.example.library_api.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    // 【重要】一人一冊チェック：これを使って二重貸出を確実に防ぎます
    boolean existsByUserIdAndBookIdAndReturnedFalse(Long userId, Long bookId);

    // 【重要】返却処理：特定の未返却データを確実に1件取得
    Optional<Rental> findFirstByUserIdAndBookIdAndReturnedFalse(Long userId, Long bookId);

    // ユーザーごとの貸出中リスト（5冊制限・延滞チェック用）
    List<Rental> findByUserIdAndReturnedFalse(Long userId);

    // 本の削除ガード用（貸出中なら削除不可）
    List<Rental> findByBookIdAndReturnedFalse(Long bookId);

    // 履歴取得用
    List<Rental> findByUserIdAndReturnedTrueOrderByReturnDateDesc(Long userId);

    // 延滞抽出用
    List<Rental> findByDueDateBeforeAndReturnedFalse(LocalDateTime dateTime);
}