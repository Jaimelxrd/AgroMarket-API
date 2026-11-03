package mz.co.agromarket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarPalavraPasseRequest(
        @NotBlank String palavraPasseAtual,
        @NotBlank @Size(min = 8, max = 72) String novaPalavraPasse) {
}
