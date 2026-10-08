package com.capstone.taxiApp.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// 전역 예외 처리기.
// 컨트롤러에서 처리되지 않은 예외를 공통 에러 응답(JSON)으로 변환해 클라이언트에 전달한다.
// 앱에서 서버 에러 메시지를 읽어 사용자에게 표시할 때 이 형식의 응답을 파싱한다.
@RestControllerAdvice
public class ApiExceptionHandler {

    // 서비스 레이어에서 throw된 IllegalArgumentException을 HTTP 400으로 매핑한다.
    // 예: "UNIVERSITIES 테이블에서 학교를 찾을 수 없습니다."
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    // @Valid/@NotNull 등 DTO 유효성 검증 실패 시 첫 번째 필드 오류 메시지를 HTTP 400으로 반환한다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().isEmpty()
                ? "요청 값이 올바르지 않습니다."
                : exception.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
    }
}
