package com.example.library_api.service;

import com.example.library_api.entity.User;
import com.example.library_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * ユーザー保存処理（賢い重複チェック付き）
     */
    @Transactional
    public User saveUser(User user) {

        // 1. 空文字チェック（念のため）
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("⚠️ メールアドレスは必須です。");
        }

        // 新規登録時（IDがnull）のみ、メールアドレスの重複をチェックする
        if (user.getId() == null) {

            // 2. 重複チェック
            if (userRepository.existsByEmail(user.getEmail())) {
                log.warn("ユーザー登録失敗：既に登録済みのメールアドレスです -> {}", user.getEmail());
                throw new RuntimeException("⚠️ このメールアドレス（" + user.getEmail() + "）は既に登録されています。");
            }
        }

        log.info("ユーザーを保存します: {}", user.getName());
        return userRepository.save(user);
    }
}