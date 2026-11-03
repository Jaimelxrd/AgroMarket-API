package mz.co.agromarket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EnviarMensagemRequest(
        @NotNull Long destinatarioId,
        @NotBlank @Size(max = 4000) String conteudo) {
}
