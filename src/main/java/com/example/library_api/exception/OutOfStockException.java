package com.example.library_api.exception; // 自分で作ったフォルダ名に合わせます

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// このエラーが発生した時、ブラウザに「400 Bad Request」という番号を返す設定です
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String message) {
        super(message);
    }
}