package mz.co.agromarket.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import mz.co.agromarket.model.enums.EstadoPedido;

public record PedidoCompraResponse(
        Long id,
        Long compradorId,
        Long produtorId,
        Long produtoId,
        String nomeProduto,
        BigDecimal quantidadeSolicitada,
        BigDecimal precoUnitarioAcordado,
        BigDecimal valorTotal,
        String mensagem,
        EstadoPedido estado,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao) {
}
