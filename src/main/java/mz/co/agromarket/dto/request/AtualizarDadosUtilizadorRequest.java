package mz.co.agromarket.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AtualizarDadosUtilizadorRequest(
        @NotBlank @Size(max = 150) String nomeCompleto,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Pattern(regexp = "^(\\+258)?(8[2-7]\\d{7}|2[1-7]\\d{7})$", message = "Número de telefone moçambicano inválido") String numeroTelefone) {
}
