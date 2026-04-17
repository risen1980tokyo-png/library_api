package com.example.library_api.dto;

// recordを使うと getter, constructor, equalsなどが自動生成されます
public record RentalRequest(
        Long bookId,
        String userName,
        Long userId
) {}