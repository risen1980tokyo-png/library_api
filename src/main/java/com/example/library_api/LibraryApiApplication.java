package com.example.library_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 図書管理システム API アプリケーション
 * * このクラスは Spring Boot アプリケーションの起動エントリーポイントです。
 * コンポーネントスキャンや自動設定を有効化し、埋め込みサーバーを起動します。
 */
@SpringBootApplication
public class LibraryApiApplication {

    public static void main(String[] args) {
        // アプリケーションの起動を実行
        SpringApplication.run(LibraryApiApplication.class, args);
    }

}