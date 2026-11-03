package mz.co.agromarket.dto.response;

public record PerfilProdutorResponse(
        Long id,
        Long utilizadorId,
        String nomeExploracao,
        String descricao,
        String provincia,
        String distrito,
        String localidade,
        String endereco,
        String tipoProducao,
        String contactoAlternativo) {
}
