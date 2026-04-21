package com.example.library_api.repository;

import com.example.library_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ユーザーデータアクセス層
 * 利用者の登録情報の参照、および一意性制約の確認クエリを提供します。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // --- 1. バリデーション・重複チェック ---

    /**
     * 指定されたメールアドレスが既に登録されているか確認します。
     * @param email 確認対象のメールアドレス
     * @return 存在する場合は true
     */
    boolean existsByEmail(String email);

    // --- 2. 検索系（将来の拡張用） ---

    /**
     * メールアドレスからユーザーを特定します。
     * ログイン機能や詳細確認などを実装する際に使用します。
     */
    Optional<User> findByEmail(String email);
}