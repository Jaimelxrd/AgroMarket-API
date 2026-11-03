package mz.co.agromarket.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarPedidoRequest(
        @NotNull Long produtoId,
        @NotNull @DecimalMin(value = "0.001") @Digits(integer = 12, fraction = 3) BigDecimal quantidadeSolicitada,
        @Size(max = 1000) String mensagem) {
}
