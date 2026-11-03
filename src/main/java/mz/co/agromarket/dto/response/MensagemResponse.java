package mz.co.agromarket.dto.response;

import java.time.LocalDateTime;

public record MensagemResponse(
        Long id,
        Long remetenteId,
        Long destinatarioId,
        String conteudo,
        LocalDateTime dataEnvio,
        boolean lida) {
}
