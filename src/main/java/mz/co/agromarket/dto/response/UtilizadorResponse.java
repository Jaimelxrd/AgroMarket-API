package mz.co.agromarket.dto.response;

import java.time.LocalDateTime;

import mz.co.agromarket.model.enums.EstadoConta;
import mz.co.agromarket.model.enums.TipoUtilizador;

public record UtilizadorResponse(
        Long id,
        String nomeCompleto,
        String email,
        String numeroTelefone,
        TipoUtilizador tipo,
        EstadoConta estado,
        LocalDateTime dataRegisto) {
}
