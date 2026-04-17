package com.example.library_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookId;
    private Long userId;

    private LocalDateTime rentalDate; // 借りた日
    private LocalDateTime returnDate; // 返した日
    private LocalDateTime dueDate;    // 返却期限日 ★追加

    private boolean returned;

    public Rental() {}

    public Rental(Long bookId, Long userId) {
        this.bookId = bookId;
        this.userId = userId;
        this.rentalDate = LocalDateTime.now();
        // 貸出期間を14日間に設定
        this.dueDate = this.rentalDate.plusDays(14);
        this.returned = false;
    }
}