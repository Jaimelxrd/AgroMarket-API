package mz.co.agromarket.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(boolean success, String message, List<String> errors, OffsetDateTime timestamp) {
    public static ApiErrorResponse erro(String message, List<String> errors) {
        return new ApiErrorResponse(false, message, errors, ApiResponse.agora());
    }
}
