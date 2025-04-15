package capston.new_valance.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = (HttpStatus) ex.getStatusCode();
        Map<String, Object> body = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", ex.getReason(),
                "path", request.getRequestURI()
        );
        return new ResponseEntity<>(body, status);
    }

    // 2. 유효성 검사 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", errorMessage,
                "path", request.getRequestURI()
        );
    }

    // 3. OAuth2 인증 실패 처리
    @ExceptionHandler(OAuth2AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleOAuth2AuthenticationException(OAuth2AuthenticationException ex, HttpServletRequest request) {
        return Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "OAuth2 Authentication Failed",
                "message", ex.getError().getDescription() != null ? ex.getError().getDescription() : ex.getMessage(),
                "path", request.getRequestURI()
        );
    }

    // 4. JWT 인증 실패로 간주되는 예외 처리
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleJwtUnauthorized(IllegalStateException ex, HttpServletRequest request) {
        return Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", ex.getMessage(),
                "path", request.getRequestURI()
        );
    }

    // 5. IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", ex.getMessage(),
                "path", request.getRequestURI()
        );
    }

    // 6. NoHandlerFoundException (404)
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        return Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.NOT_FOUND.value(),
                "error", HttpStatus.NOT_FOUND.getReasonPhrase(),
                "message", "No endpoint " + ex.getHttpMethod() + " " + ex.getRequestURL() + ".",
                "path", request.getRequestURI()
        );
    }

    // 7. DB 제약 위반 처리
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        return Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", "Database constraint violation: " + ex.getMostSpecificCause().getMessage(),
                "path", request.getRequestURI()
        );
    }

    // 8. 제약 조건 유효성 검사 실패
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        return Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", "Validation error: " + ex.getMessage(),
                "path", request.getRequestURI()
        );
    }

    // 9. 알 수 없는 예외
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleOtherExceptions(Exception ex, HttpServletRequest request) {
        return Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "Internal Server Error",
                "message", ex.getMessage(),
                "path", request.getRequestURI()
        );
    }
}
