package com.example.library_api.controller;

import com.example.library_api.entity.Book;
import com.example.library_api.entity.Rental;
import com.example.library_api.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 貸出・返却管理APIコントローラー
 */
@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    /**
     * 指定された本をユーザーに貸し出す
     */
    @PostMapping("/rent")
    public Book rentBook(@RequestParam Long bookId, @RequestParam Long userId) {
        // 成功時は更新された本情報をそのまま返す
        // 失敗時（延滞・上限など）は自動で GlobalExceptionHandler が 400 を返す
        return rentalService.rentBook(bookId, userId);
    }

    /**
     * 指定された本を返却する
     */
    @PostMapping("/return")
    public Book returnBook(@RequestParam Long bookId, @RequestParam Long userId) {
        return rentalService.returnBook(bookId, userId);
    }

    /**
     * 指定されたユーザーの現在の貸出状況を取得
     */
    @GetMapping("/active")
    public List<Rental> getActiveRentals(@RequestParam Long userId) {
        return rentalService.getActiveRentals(userId);
    }

    /**
     * 指定されたユーザーの過去の貸出履歴を取得
     */
    @GetMapping("/history")
    public List<Rental> getRentalHistory(@RequestParam Long userId) {
        return rentalService.getUserHistory(userId);
    }

    /**
     * 期限を超過している貸出記録の一覧を取得
     */
    @GetMapping("/overdue")
    public List<Rental> getOverdueRentals() {
        return rentalService.getOverdueRentals();
    }
}