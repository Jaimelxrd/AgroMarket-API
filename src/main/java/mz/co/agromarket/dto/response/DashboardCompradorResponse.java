package mz.co.agromarket.dto.response;

public record DashboardCompradorResponse(
        long totalPedidos,
        long pedidosPendentes,
        long pedidosAceites,
        long pedidosConcluidos) {
}
