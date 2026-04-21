package com.example.library_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 蔵書エンティティ
 * データベースの 'books' テーブルと対応し、本の基本情報を保持します。
 */
@Entity
@Data
@NoArgsConstructor // JPA用の空コンストラクタ
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "⚠️ タイトルは必須です。")
    private String title;

    @NotBlank(message = "⚠️ 著者は必須です。")
    private String author;

    private String isbn;

    @Min(value = 0, message = "⚠️ 在庫数は0以上で入力してください。")
    private Integer stock;

    /** 作成日時：一度登録したら更新不可 */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 新規保存時に実行：作成日時を現在時刻で自動セットします。
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}