package com.example.library_api.controller;

import com.example.library_api.entity.Book;
import com.example.library_api.entity.Rental;
import com.example.library_api.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // 貸出処理
    @PostMapping("/rent") // URLは /rent
    public ResponseEntity<?> rentBook(@RequestParam Long bookId, @RequestParam Long userId) {
        try {
            // 成功時は JSON (Book) を返す
            Book updatedBook = rentalService.rentBook(bookId, userId);
            return ResponseEntity.ok(updatedBook);
        } catch (RuntimeException e) {
            // 失敗時は String (エラーメッセージ) を返す
            // ResponseEntity.badRequest().body() は自動的に適切な形式で返してくれます
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 返却処理
    @PostMapping("/return") // ★ここが /rent になっていたのを /return に修正
    public ResponseEntity<?> returnBook(@RequestParam Long bookId, @RequestParam Long userId) {
        try {
            // 成功時は JSON (Book) を返す
            Book updatedBook = rentalService.returnBook(bookId, userId);
            return ResponseEntity.ok(updatedBook);
        } catch (RuntimeException e) {
            // 失敗時は String を返す
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 現在の貸出状況取得
    @GetMapping("/active")
    public List<Rental> getActiveRentals(@RequestParam Long userId) {
        return rentalService.getActiveRentals(userId);
    }

    @GetMapping("/history")
    public List<Rental> getRentalHistory(@RequestParam Long userId) {
        return rentalService.getUserHistory(userId); // Serviceに作成済みのメソッドを呼び出す
    }

    @GetMapping("/overdue")
    public List<Rental> getOverdueRentals() {
        return rentalService.getOverdueRentals();
    }
}