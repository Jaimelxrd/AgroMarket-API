package mz.co.agromarket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PerfilProdutorRequest(
        @NotBlank @Size(max = 160) String nomeExploracao,
        @Size(max = 2000) String descricao,
        @NotBlank @Size(max = 80) String provincia,
        @NotBlank @Size(max = 100) String distrito,
        @Size(max = 120) String localidade,
        @Size(max = 300) String endereco,
        @Size(max = 120) String tipoProducao,
        @Pattern(regexp = "^(\\+258)?(8[2-7]\\d{7}|2[1-7]\\d{7})$", message = "Contacto alternativo inválido") String contactoAlternativo) {
}
