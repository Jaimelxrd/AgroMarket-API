package mz.co.agromarket.dto.response;

import mz.co.agromarket.model.enums.EstadoCategoria;

public record CategoriaResponse(Long id, String nome, String descricao, EstadoCategoria estado) {
}
