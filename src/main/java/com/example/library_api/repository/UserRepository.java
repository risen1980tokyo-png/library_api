package com.example.library_api.repository;

import com.example.library_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // メールアドレスチェック（唯一の識別キー）
    boolean existsByEmail(String email);
}