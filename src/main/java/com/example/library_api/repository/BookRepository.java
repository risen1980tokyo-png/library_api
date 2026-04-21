package com.example.library_api.repository;

import com.example.library_api.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 蔵書データアクセス層
 * 標準的なCRUD操作に加え、排他制御および重複チェックのためのクエリを提供します。
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    // --- 1. 重複チェック（バリデーション用） ---

    /** タイトルと著者の組み合わせによる存在確認 */
    boolean existsByTitleAndAuthor(String title, String author);

    /** ISBNによる存在確認 */
    boolean existsByIsbn(String isbn);


    // --- 2. 検索・参照系 ---

    /** タイトルによる部分一致検索（大文字小文字を区別しない） */
    List<Book> findByTitleContainingIgnoreCase(String keyword);


    // --- 3. 排他制御（トランザクション用） ---

    /**
     * 指定したIDの本を悲観的ロック（PESSIMISTIC_WRITE）をかけて取得します。
     * 貸出・返却時の在庫不整合（ロストアップデート）を防止するために使用します。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdWithLock(@Param("id") Long id);
}