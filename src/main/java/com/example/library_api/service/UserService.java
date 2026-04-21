package com.example.library_api.service;

import com.example.library_api.entity.User;
import com.example.library_api.repository.RentalRepository;
import com.example.library_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザー管理ビジネスロジック
 * 登録・更新・削除および、それに付随する整合性チェックを担当します。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

    /**
     * ユーザー情報を保存または更新します。
     * 新規登録時にはメールアドレスの一一性をチェックします。
     * * @param user 保存対象のユーザーエンティティ
     * @return 保存されたユーザーエンティティ
     */
    @Transactional
    public User saveUser(User user) {
        // 1. 必須入力チェック（ガード節）
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("⚠️ メールアドレスは必須です。");
        }

        // 2. 新規登録時の重複チェック
        if (user.getId() == null) {
            if (userRepository.existsByEmail(user.getEmail())) {
                log.warn("ユーザー登録失敗：既に登録済みのメールアドレスです -> {}", user.getEmail());
                throw new RuntimeException("⚠️ このメールアドレス（" + user.getEmail() + "）は既に登録されています。");
            }
        }

        log.info("ユーザーを保存します: {}", user.getName());
        return userRepository.save(user);
    }

    /**
     * ユーザーを削除します。
     * 未返却の本がある場合は、データ整合性維持のため削除を拒否します。
     * * @param id 削除対象のユーザーID
     */
    @Transactional
    public void deleteUser(Long id) {
        // 1. 存在確認（ガード節）
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("⚠️ 削除対象のユーザーが見つかりません。");
        }

        // 2. 未返却本の有無を確認
        boolean hasActiveRental = !rentalRepository.findByUserIdAndReturnedFalse(id).isEmpty();
        if (hasActiveRental) {
            log.warn("ユーザー削除拒否：未返却本あり（ユーザーID: {}）", id);
            throw new RuntimeException("⚠️ 未返却の本があるため、このユーザーは削除できません。");
        }

        log.info("ユーザーを削除しました: ID {}", id);
        userRepository.deleteById(id);
    }
}