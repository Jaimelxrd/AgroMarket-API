package mz.co.agromarket.dto.response;

import mz.co.agromarket.model.enums.TipoUtilizador;

public record InterlocutorResponse(Long utilizadorId, String nomeCompleto, TipoUtilizador tipo) {
}
