package com.example.library_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "タイトルは必須です")
    private String title;

    @NotBlank(message = "著者は必須です")
    private String author;

    private String isbn;

    @Min(value = 0, message = "在庫は0以上で入力してください")
    private Integer stock;

    @Column(updatable = false) // ★更新時はこの列を無視する設定を追加
    private LocalDateTime createdAt;

    // Book.java (Entity) 内にメソッドを作る
    public void rentTo(User user) {
        if (this.stock <= 0) {
            throw new RuntimeException("在庫がありません");
        }
        this.stock--;
    }

    public Integer getStock() {
        return stock;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}