package com.yitong.guides.web.api;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ApiExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<?> handleApi(ApiException ex) {
    return ResponseEntity.status(ex.getStatus())
        .body(Map.of("timestamp", Instant.now().toString(), "message", ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            Map.of(
                "timestamp",
                Instant.now().toString(),
                "message",
                "参数校验失败",
                "details",
                ex.getBindingResult().getAllErrors().stream()
                    .map(err -> err.getDefaultMessage())
                    .toList()));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<?> handleConstraint(ConstraintViolationException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("timestamp", Instant.now().toString(), "message", ex.getMessage()));
  }

  /** 数据约束冲突（如外键、唯一约束）：返回明确提示，避免前端只看到“服务器错误” */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex) {
    log.warn("数据约束冲突: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of(
            "timestamp", Instant.now().toString(),
            "message", "操作失败，数据约束冲突，请检查关联数据"));
  }

  /** 上传文件超过 spring.servlet.multipart.max-file-size 时抛出，返回明确提示 */
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<?> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
    log.warn("上传文件过大: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
        .body(Map.of(
            "timestamp", Instant.now().toString(),
            "message", "文件大小不能超过 10MB"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleOther(Exception ex) {
    log.error("未处理的异常", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("timestamp", Instant.now().toString(), "message", "服务器错误"));
  }
}

