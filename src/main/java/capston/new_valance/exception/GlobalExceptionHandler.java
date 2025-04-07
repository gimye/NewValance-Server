package capston.new_valance.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 기존 ResponseStatusException 핸들링
    @ExceptionHandler(ResponseStatusException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = (HttpStatus) ex.getStatusCode(); // 명시적 캐스팅

        return Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", ex.getReason(),
                "path", request.getRequestURI()
        );
    }



    // OAuth2 인증 예외 처리
    @ExceptionHandler(OAuth2AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleOAuth2AuthenticationException(OAuth2AuthenticationException ex) {
        return Map.of(
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "OAuth2 Authentication Failed",
                "message", ex.getError().getDescription() != null ? ex.getError().getDescription() : ex.getMessage()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleJwtRuntimeException(RuntimeException ex, HttpServletRequest request) {
        return Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", ex.getMessage(),
                "path", request.getRequestURI()
        );
    }

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


    // 기타 알 수 없는 예외 처리
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
