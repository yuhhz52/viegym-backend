package com.example.viegymapp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Khóa không hợp lệ", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "Email đã được sử dụng", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Tên người dùng phải có ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Mật khẩu phải có ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Email hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Tuổi của bạn phải ít nhất {min}", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1009, "Vai trò mặc định không tồn tại", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_USED(1010, "Email đã được sử dụng", HttpStatus.BAD_REQUEST),
    TOKEN_REFRESH_FAILED(1011, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại", HttpStatus.FORBIDDEN),
    EXERCISE_NOT_FOUND(1012, "Bài tập không tồn tại", HttpStatus.NOT_FOUND),
    WORKOUT_PROGRAM_NOT_FOUND(1013, "Chương trình tập luyện không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_FILE_TYPE(1014, "Loại file không hợp lệ", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(1015, "Kích thước file quá lớn", HttpStatus.BAD_REQUEST),
    DUPLICATE_ENTRY(1016, "Bản ghi trùng lặp", HttpStatus.CONFLICT),
    RESOURCE_NOT_FOUND(1017, "Tài nguyên không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_REQUEST_PARAMETER(1018, "Tham số yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    DATABASE_ERROR(1019, "Lỗi thao tác cơ sở dữ liệu", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_FAILED(1020, "Xác thực thất bại", HttpStatus.BAD_REQUEST),
    CONSTRAINT_VIOLATION(1021, "Vi phạm ràng buộc", HttpStatus.BAD_REQUEST),
    METHOD_NOT_SUPPORTED(1022, "Phương thức yêu cầu không được hỗ trợ", HttpStatus.METHOD_NOT_ALLOWED),
    MEDIA_NOT_FOUND(1023, "Media bài tập không tồn tại", HttpStatus.NOT_FOUND),
    PROGRAM_NOT_FOUND(1024, "Chương trình không tồn tại",HttpStatus.NOT_FOUND),
    PROGRAM_EXERCISE_NOT_FOUND(1025, "Bài tập trong chương trình không tồn tại",HttpStatus.NOT_FOUND),
    SESSION_NOT_FOUND(1026, "Phiên tập luyện không tồn tại",HttpStatus.NOT_FOUND),
    LOG_NOT_FOUND(1027, "Nhật ký không tồn tại",HttpStatus.NOT_FOUND),
    HEALTH_LOG_NOT_FOUND(1028, "Nhật ký sức khỏe không tồn tại",HttpStatus.NOT_FOUND),
    POST_NOT_FOUND(1029, "Bài viết không tồn tại",HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND(1030, "Bình luận không tồn tại",HttpStatus.NOT_FOUND),
    INVALID_EMAIL(1031, "Email phải có ít nhất {min} ký tự",HttpStatus.BAD_REQUEST),
    FILE_EMPTY(1032, "File trống",HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1033, "Token không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1034, "Token đã hết hạn", HttpStatus.BAD_REQUEST),
    TOKEN_ALREADY_USED(1035, "Token đã được sử dụng", HttpStatus.BAD_REQUEST),
    TOO_MANY_RESET_REQUESTS(1036, "Bạn chỉ có thể yêu cầu đặt lại mật khẩu một lần mỗi giờ", HttpStatus.TOO_MANY_REQUESTS),
    CANNOT_DELETE_SELF(1037, "Bạn không thể xóa chính mình", HttpStatus.BAD_REQUEST),
    CANNOT_DISABLE_SELF(1038, "Bạn không thể vô hiệu hóa chính mình", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_PERMISSION(1039, "Bạn không có đủ quyền để thực hiện thao tác này", HttpStatus.FORBIDDEN),
    ALREADY_REPORTED(1040, "Bạn đã báo cáo bài viết này trước đó", HttpStatus.BAD_REQUEST),
    CLIENT_NOT_FOUND(1041, "Học viên không tồn tại hoặc không thuộc quyền quản lý của bạn", HttpStatus.NOT_FOUND),
    CLIENT_ALREADY_EXISTS(1042, "Học viên này đã được thêm vào danh sách của bạn", HttpStatus.CONFLICT),
    PROGRAM_ALREADY_SAVED(1043, "Chương trình này đã được gán cho học viên", HttpStatus.CONFLICT),
    TIMESLOT_NOT_FOUND(1044, "Khung giờ không tồn tại", HttpStatus.NOT_FOUND),
    TIMESLOT_NOT_AVAILABLE(1045, "Khung giờ này đã được đặt", HttpStatus.CONFLICT),
    BOOKING_NOT_FOUND(1046, "Lịch hẹn không tồn tại", HttpStatus.NOT_FOUND),
    CANNOT_BOOK_SELF(1047, "Bạn không thể đặt lịch với chính mình", HttpStatus.BAD_REQUEST),
    TOO_MANY_BOOKINGS(1048, "Bạn đã đặt quá nhiều lịch. Vui lòng thử lại sau 10 phút", HttpStatus.TOO_MANY_REQUESTS),
    TOO_MANY_CANCELLATIONS(1049, "Bạn đã hủy quá nhiều lịch hẹn. Vui lòng thử lại sau 1 giờ", HttpStatus.TOO_MANY_REQUESTS),
    SLOT_HAS_BOOKINGS(1050, "Không thể xóa khung giờ đã có lịch hẹn. Vui lòng hủy các lịch hẹn trước", HttpStatus.CONFLICT),
    TIMESLOT_OVERLAPS(1051, "Khung giờ này trùng với lịch đã tạo. Vui lòng chọn thời gian khác", HttpStatus.CONFLICT),
    NOTIFICATION_NOT_FOUND(1052, "Thông báo không tồn tại", HttpStatus.NOT_FOUND)
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
