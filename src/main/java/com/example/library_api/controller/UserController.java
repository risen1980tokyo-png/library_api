package com.example.library_api.controller;

import com.example.library_api.entity.User;
import com.example.library_api.repository.RentalRepository;
import com.example.library_api.repository.UserRepository;
import com.example.library_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor // コンストラクタを自動生成します
public class UserController {

    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final UserService userService; // ①追加

    // ユーザー一覧（管理者用）
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> saveUser(@Valid @RequestBody User user, BindingResult result) {
        // 既存のバリデーションチェック（名前必須など）
        if (result.hasErrors()) {
            //エラー内容（「名前は必須です」など）を取得
            String message = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(message);
        }

        try {
            // ② 修正：直接保存せずService経由で「賢いチェック」を行う
            User savedUser = userService.saveUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (RuntimeException e) {
            // ③ 追加：Serviceで重複エラーが起きた場合にそのメッセージをフロントへ返す
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        // ユーザーに紐付く未返却のレンタル記録があるか確認
        // rentalRepository.findByUserIdAndReturnedFalse を使う
        boolean hasActiveRentals = !rentalRepository.findByUserIdAndReturnedFalse(id).isEmpty();
        if (hasActiveRentals) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("このユーザーは本を貸出中のため削除できません。");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("削除しました");
    }
}