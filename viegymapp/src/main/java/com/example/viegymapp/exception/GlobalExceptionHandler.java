package com.example.viegymapp.exception;

import com.example.viegymapp.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String MIN_ATTRIBUTE = "min";

    // Xử lý favicon.ico và static resources không tìm thấy (tránh log spam)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse> handleNoResourceFoundException(NoResourceFoundException exception) {
        // Chỉ log ở debug level để tránh spam
        log.debug("Static resource not found: {}", exception.getResourcePath());
        
        ApiResponse apiResponse = ApiResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message("Resource not found")
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }

    //Bắt toàn bộ exception chưa xử lý
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handlingException(Exception exception) {
        log.error("Exception: ", exception);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    //Dùng khi user không đủ quyền truy cập (403 Forbidden)
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    //Dùng khi Spring Security throw AuthorizationDeniedException
    //Nếu không có authentication (token expired/null) -> trả 401
    //Nếu có authentication nhưng không đủ quyền -> trả 403
    @ExceptionHandler(AuthorizationDeniedException.class)
    ResponseEntity<ApiResponse> handlingAuthorizationDeniedException(AuthorizationDeniedException exception) {
        // Kiểm tra xem có authentication trong SecurityContext hay không
        // Nếu không có authentication (null hoặc anonymous) -> 401 Unauthorized
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || 
            authentication.getName() == null || 
            authentication.getName().equals("anonymousUser") ||
            !authentication.isAuthenticated()) {
            ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder()
                            .code(errorCode.getCode())
                            .message(errorCode.getMessage())
                            .build());
        }
        
        // Có authentication nhưng không đủ quyền -> 403
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    //Dùng khi authentication thất bại
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiResponse> handlingAuthenticationException(AuthenticationException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    //Validation lỗi @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
            var constraintViolation =
                    exception.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);

            attributes = constraintViolation.getConstraintDescriptor().getAttributes();

            log.info(attributes.toString());
        } catch (IllegalArgumentException e) {

        }

        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(
                Objects.nonNull(attributes)
                        ? mapAttribute(errorCode.getMessage(), attributes)
                        : errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);

    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));

        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponse response = ApiResponse.builder()
                .code(ErrorCode.INVALID_KEY.getCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    // Validation lỗi @RequestParam, @PathVariable
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolation(ConstraintViolationException ex) {
        ApiResponse response = ApiResponse.builder()
                .code(ErrorCode.CONSTRAINT_VIOLATION.getCode())
                .message(ex.getConstraintViolations().iterator().next().getMessage())
                .build();
        return ResponseEntity.status(ErrorCode.CONSTRAINT_VIOLATION.getStatusCode()).body(response);
    }

    // Gọi sai HTTP method
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ApiResponse response = ApiResponse.builder()
                .code(ErrorCode.METHOD_NOT_SUPPORTED.getCode())
                .message(ErrorCode.METHOD_NOT_SUPPORTED.getMessage() + ": " + ex.getMethod())
                .build();
        return ResponseEntity.status(ErrorCode.METHOD_NOT_SUPPORTED.getStatusCode()).body(response);
    }

    // Thêm exception handler cho DataAccessException
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse> handleDataAccessException(DataAccessException ex) {
        log.error("Database error: ", ex);
        ApiResponse response = ApiResponse.builder()
                .code(ErrorCode.DATABASE_ERROR.getCode())
                .message("Database operation failed")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Thêm exception handler cho FileUploadException
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        ApiResponse response = ApiResponse.builder()
                .code(ErrorCode.FILE_TOO_LARGE.getCode())
                .message(ErrorCode.FILE_TOO_LARGE.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Xử lý lỗi khi UUID không hợp lệ trong @PathVariable
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Invalid argument type for parameter {}: {}", ex.getName(), ex.getValue());
        String message = "Tham số không hợp lệ";
        if (ex.getRequiredType() != null && ex.getRequiredType().equals(java.util.UUID.class)) {
            message = "ID không hợp lệ. Vui lòng kiểm tra lại.";
        }
        ApiResponse response = ApiResponse.builder()
                .code(ErrorCode.INVALID_REQUEST_PARAMETER.getCode())
                .message(message)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
