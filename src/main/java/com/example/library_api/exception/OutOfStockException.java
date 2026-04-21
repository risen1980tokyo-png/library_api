package com.example.library_api.exception;

/**
 * 在庫不足例外
 * 貸出処理時に、対象書籍の在庫がゼロの場合にスローされます。
 */
public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String message) {
        super(message);
    }
}