package com.example.library_api.service;

import com.example.library_api.entity.Rental;
import com.example.library_api.entity.User;
import com.example.library_api.repository.RentalRepository;
import com.example.library_api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RentalRepository rentalRepository;

    // --- 1. バリデーション：名前のチェック ---

    @Test
    @DisplayName("バリデーション：名前が未入力の場合は保存に失敗すること")
    void testSaveUser_NameRequired() {
        User user = new User();
        user.setEmail("test@example.com");

        assertThrows(RuntimeException.class, () -> {
            userService.saveUser(user);
        });
    }

    @Test
    @DisplayName("バリデーション：名前が1文字の場合は保存に失敗すること")
    void testSaveUser_NameTooShort() {
        User user = new User();
        user.setName("あ");
        user.setEmail("test@example.com");

        assertThrows(RuntimeException.class, () -> {
            userService.saveUser(user);
        });
    }

    @Test
    @DisplayName("バリデーション：名前が21文字の場合は保存に失敗すること")
    void testSaveUser_NameTooLong() {
        User user = new User();
        user.setName("あ".repeat(21));
        user.setEmail("test@example.com");

        assertThrows(RuntimeException.class, () -> {
            userService.saveUser(user);
        });
    }

    // --- 2. バリデーション：メールアドレスのチェック ---

    @Test
    @DisplayName("バリデーション：無効な形式のメールアドレスは失敗すること")
    void testSaveUser_InvalidEmailFormat() {
        User user = new User();
        user.setName("テスト太郎");
        user.setEmail("invalid-email-format");

        assertThrows(RuntimeException.class, () -> {
            userService.saveUser(user);
        });
    }

    // --- 3. 重複チェック・削除制限 ---

    @Test
    @DisplayName("一意性チェック：重複したメールアドレスでの登録は拒否されること")
    void testDuplicateEmailFail() {
        String commonEmail = "duplicate@example.com";

        User user1 = new User();
        user1.setName("ユーザー1");
        user1.setEmail(commonEmail);
        userRepository.saveAndFlush(user1);

        User user2 = new User();
        user2.setName("ユーザー2");
        user2.setEmail(commonEmail);

        assertThrows(RuntimeException.class, () -> {
            userService.saveUser(user2);
        });
    }

    @Test
    @DisplayName("削除チェック：未返却の本があるユーザーは削除できないこと")
    void testDeleteUser_FailIfHasActiveRental() {
        User user = new User();
        user.setName("テスト借り手");
        user.setEmail("borrower@example.com");
        user = userRepository.saveAndFlush(user);

        Rental rental = new Rental(1L, user.getId());
        rentalRepository.saveAndFlush(rental);

        final Long userId = user.getId();
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(userId);
        });

        assertTrue(exception.getMessage().contains("未返却の本があるため"));
    }
}