package com.example.library_api.controller;

import com.example.library_api.entity.User;
import com.example.library_api.repository.UserRepository;
import com.example.library_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ユーザー管理APIコントローラー
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * 全てのユーザー情報を取得
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * ユーザーを保存または更新
     * バリデーションエラーやビジネスルール違反は GlobalExceptionHandler が処理します
     */
    @PostMapping
    public User saveUser(@Valid @RequestBody User user) {
        return userService.saveUser(user);
    }

    /**
     * ユーザーを削除
     */
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}