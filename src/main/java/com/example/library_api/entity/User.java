package com.example.library_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ユーザー（利用者）エンティティ
 * データベースの 'users' テーブルと対応し、利用者の基本情報を保持します。
 */
@Entity
@Data
@NoArgsConstructor // JPA用の空コンストラクタ
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "⚠️ 名前は必須です。")
    @Size(min = 2, max = 20, message = "⚠️ 名前は2文字以上20文字以内で入力してください。")
    private String name;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "⚠️ メールアドレスは必須です。")
    @Email(message = "⚠️ 有効なメールアドレスを入力してください。")
    private String email;
}