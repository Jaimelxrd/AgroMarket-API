package mz.co.agromarket.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import mz.co.agromarket.model.enums.EstadoProduto;
import mz.co.agromarket.model.enums.UnidadeMedida;

public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        Long categoriaId,
        String categoriaNome,
        Long produtorId,
        String nomeProdutor,
        BigDecimal precoUnidade,
        UnidadeMedida unidadeMedida,
        BigDecimal quantidadeDisponivel,
        String provincia,
        String distrito,
        String localidade,
        List<String> fotografias,
        EstadoProduto estado,
        LocalDateTime dataPublicacao) {
}
