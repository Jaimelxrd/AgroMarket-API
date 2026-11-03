package mz.co.agromarket.dto.request;

import java.math.BigDecimal;

import mz.co.agromarket.model.enums.UnidadeMedida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProdutoRequest(
        @NotBlank @Size(max = 160) String nome,
        @Size(max = 3000) String descricao,
        @NotNull Long categoriaId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal precoUnidade,
        @NotNull UnidadeMedida unidadeMedida,
        @NotNull @PositiveOrZero BigDecimal quantidadeDisponivel,
        @NotBlank @Size(max = 80) String provincia,
        @NotBlank @Size(max = 100) String distrito,
        @Size(max = 120) String localidade) {
}
