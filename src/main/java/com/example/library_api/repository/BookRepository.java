package com.example.library_api.repository;

import com.example.library_api.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    // タイトルと著者の組み合わせで存在確認
    boolean existsByTitleAndAuthor(String title, String author);
    // ISBN単体で存在確認
    boolean existsByIsbn(String isbn);

    // ↓ ここが最重要！DBの行をロックして読み込みます
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdWithLock(@Param("id") Long id);

    // タイトルにキーワードが含まれるものを探す (IgnoreCaseで大文字小文字を区別しない)
    List<Book> findByTitleContainingIgnoreCase(String keyword);
}