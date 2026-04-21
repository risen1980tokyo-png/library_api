package com.example.library_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 貸出履歴エンティティ
 * 蔵書の貸出状況、返却期限、および返却フラグを管理します。
 */
@Entity
@Data
@NoArgsConstructor // JPA用の空コンストラクタを自動生成
@Table(name = "rental")
public class Rental {

    // ★ 貸出期間を定数として定義（実務ではこのように書きます）
    public static final int DEFAULT_RENTAL_DAYS = 14;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookId;
    private Long userId;

    private LocalDateTime rentalDate; // 貸出日
    private LocalDateTime returnDate; // 実際の返却日
    private LocalDateTime dueDate;    // 返却期限日

    private boolean returned;

    /**
     * 新規貸出用のコンストラクタ
     * 貸出日、返却期限、ステータスを初期化します。
     */
    public Rental(Long bookId, Long userId) {
        this.bookId = bookId;
        this.userId = userId;
        this.rentalDate = LocalDateTime.now();
        this.dueDate = this.rentalDate.plusDays(DEFAULT_RENTAL_DAYS);
        this.returned = false;
    }
}