package com.example.library_api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * アプリケーション全体の例外ハンドラー
 * 各レイヤーで発生した例外をキャッチし、一貫したフォーマットでクライアントへ返します。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * パターンA：Controllerの @Valid で発生したバリデーションエラー
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            // すべてのエラーメッセージに警告アイコンを付与して統一感を出す
            String message = error.getDefaultMessage();
            if (message != null && !message.startsWith("⚠️")) {
                message = "⚠️ " + message;
            }
            errors.put(error.getField(), message);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * パターンB：Entity層（DB保存直前）でのバリデーションエラー
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String message = violation.getMessage();
            if (message != null && !message.startsWith("⚠️")) {
                message = "⚠️ " + message;
            }
            errors.put(violation.getPropertyPath().toString(), message);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * パターンC-1：特定の業務例外（在庫不足）
     * 具体的な例外を先に書く
     */
    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<Map<String, String>> handleOutOfStockException(OutOfStockException ex) {
        log.warn("在庫不足検知: {}", ex.getMessage());
        // 独自の例外なので、ここで ⚠️ を付けて返すと決めておけば Service 側は楽になります。
        return ResponseEntity.badRequest().body(Map.of("error", "⚠️ " + ex.getMessage()));
    }

    /**
     * パターンC-2：その他の業務エラー（一般的な RuntimeException）
     * 最後に「その他」として受け止めます。
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        if (message != null && !message.startsWith("⚠️")) {
            message = "⚠️ " + message;
        }
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}