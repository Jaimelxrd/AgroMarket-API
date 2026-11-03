package mz.co.agromarket.dto.response;

public record DashboardProdutorResponse(
        long totalProdutos,
        long produtosDisponiveis,
        long pedidosRecebidos,
        long pedidosPendentes,
        long encomendasConcluidas) {
}
