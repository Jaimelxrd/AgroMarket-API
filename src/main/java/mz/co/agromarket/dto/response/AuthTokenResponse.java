package mz.co.agromarket.dto.response;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UtilizadorResponse utilizador) {
}
