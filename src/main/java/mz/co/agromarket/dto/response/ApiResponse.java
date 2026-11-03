package mz.co.agromarket.dto.response;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record ApiResponse<T>(boolean success, String message, T data, OffsetDateTime timestamp) {
    public static <T> ApiResponse<T> sucesso(T data) {
        return new ApiResponse<>(true, "Operação realizada com sucesso.", data, agora());
    }

    public static OffsetDateTime agora() {
        return OffsetDateTime.now(ZoneId.of("Africa/Maputo"));
    }
}
