package com.example.library_api;

import com.example.library_api.entity.Book;
import com.example.library_api.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LibraryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApiApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner demo(BookRepository repository) {
//        return (args) -> {
//            // 本を1冊作成して保存
//            Book book = new Book();
//            book.setTitle("45歳からのJavaリスタート");
//            book.setAuthor("自分自身");
//            book.setIsbn("123-456789");
//
//            repository.save(book);
//
//            System.out.println("★本を保存しました！タイトル: " + book.getTitle());
//        };
//    }
}