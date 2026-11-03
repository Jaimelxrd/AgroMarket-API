package mz.co.agromarket.dto.response;

import java.util.List;

public record DashboardAdminResponse(
        long totalUtilizadores,
        long totalProdutores,
        long totalCompradores,
        long totalProdutos,
        long totalPedidos,
        long pedidosPendentes,
        long pedidosConcluidos,
        List<EstatisticaAgrupadaResponse> produtosPorCategoria,
        List<EstatisticaAgrupadaResponse> produtosPorProvincia) {
}
