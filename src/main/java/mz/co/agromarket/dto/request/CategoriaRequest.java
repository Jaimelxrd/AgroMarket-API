package mz.co.agromarket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank @Size(max = 100) String nome,
        @Size(max = 1000) String descricao) {
}
