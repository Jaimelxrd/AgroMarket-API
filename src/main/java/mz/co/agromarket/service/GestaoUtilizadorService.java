package mz.co.agromarket.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mz.co.agromarket.dto.request.AlterarPalavraPasseRequest;
import mz.co.agromarket.dto.request.AtualizarDadosUtilizadorRequest;
import mz.co.agromarket.dto.response.UtilizadorResponse;
import mz.co.agromarket.model.entity.Utilizador;
import mz.co.agromarket.model.enums.EstadoConta;
import mz.co.agromarket.repository.UtilizadorRepository;

@Service
public class GestaoUtilizadorService {

    private final UtilizadorRepository utilizadorRepository;
    private final PasswordEncoder passwordEncoder;

    public GestaoUtilizadorService(UtilizadorRepository utilizadorRepository, PasswordEncoder passwordEncoder) {
        this.utilizadorRepository = utilizadorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UtilizadorResponse obterPerfil(String email) {
        return paraResponse(obterAtivo(email));
    }

    @Transactional
    public UtilizadorResponse atualizarPerfil(String emailAutenticado, AtualizarDadosUtilizadorRequest request) {
        Utilizador utilizador = obterAtivo(emailAutenticado);
        String novoEmail = normalizarEmail(request.email());
        if (!utilizador.getEmail().equalsIgnoreCase(novoEmail)
                && utilizadorRepository.existsByEmailIgnoreCase(novoEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este email já está registado.");
        }
        utilizador.setNomeCompleto(request.nomeCompleto().trim());
        utilizador.setEmail(novoEmail);
        utilizador.setNumeroTelefone(request.numeroTelefone());
        return paraResponse(utilizador);
    }

    @Transactional
    public void alterarPalavraPasse(String email, AlterarPalavraPasseRequest request) {
        Utilizador utilizador = obterAtivo(email);
        if (!passwordEncoder.matches(request.palavraPasseAtual(), utilizador.getPalavraPasseHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A palavra-passe atual está incorreta.");
        }
        utilizador.setPalavraPasseHash(passwordEncoder.encode(request.novaPalavraPasse()));
    }

    @Transactional
    public void desativarConta(String email) {
        Utilizador utilizador = obterAtivo(email);
        utilizador.setEstado(EstadoConta.INATIVA);
    }

    private Utilizador obterAtivo(String email) {
        Utilizador utilizador = utilizadorRepository.findByEmailIgnoreCase(normalizarEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizador não encontrado."));
        if (utilizador.getEstado() != EstadoConta.ATIVA) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A conta está desativada.");
        }
        return utilizador;
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UtilizadorResponse paraResponse(Utilizador utilizador) {
        return new UtilizadorResponse(
                utilizador.getId(), utilizador.getNomeCompleto(), utilizador.getEmail(),
                utilizador.getNumeroTelefone(), utilizador.getTipo(), utilizador.getEstado(), utilizador.getDataRegisto());
    }
}
