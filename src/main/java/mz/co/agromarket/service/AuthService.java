package mz.co.agromarket.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mz.co.agromarket.dto.request.LoginRequest;
import mz.co.agromarket.dto.request.RegistoUtilizadorRequest;
import mz.co.agromarket.dto.response.AuthTokenResponse;
import mz.co.agromarket.dto.response.UtilizadorResponse;
import mz.co.agromarket.model.entity.Utilizador;
import mz.co.agromarket.model.enums.EstadoConta;
import mz.co.agromarket.model.enums.TipoUtilizador;
import mz.co.agromarket.repository.UtilizadorRepository;
import mz.co.agromarket.security.JwtService;

@Service
public class AuthService {

    private final UtilizadorRepository utilizadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long duracaoMinutos;

    public AuthService(
            UtilizadorRepository utilizadorRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            @Value("${security.jwt.expiration-minutes:15}") long duracaoMinutos) {
        this.utilizadorRepository = utilizadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.duracaoMinutos = duracaoMinutos;
    }

    @Transactional
    public UtilizadorResponse registar(RegistoUtilizadorRequest request) {
        String email = normalizarEmail(request.email());
        if (utilizadorRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este email já está registado.");
        }

        Utilizador utilizador = new Utilizador();
        utilizador.setNomeCompleto(request.nomeCompleto().trim());
        utilizador.setEmail(email);
        utilizador.setNumeroTelefone(request.numeroTelefone());
        utilizador.setPalavraPasseHash(passwordEncoder.encode(request.palavraPasse()));
        utilizador.setTipo(request.tipo() == mz.co.agromarket.model.enums.TipoContaPublica.PRODUTOR
                ? TipoUtilizador.PRODUTOR
                : TipoUtilizador.COMPRADOR);
        utilizador.setEstado(EstadoConta.ATIVA);

        return paraResponse(utilizadorRepository.save(utilizador));
    }

    public AuthTokenResponse autenticar(LoginRequest request) {
        String email = normalizarEmail(request.email());
        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, request.palavraPasse()));
        } catch (BadCredentialsException | DisabledException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou palavra-passe inválidos.");
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não foi possível autenticar a conta.");
        }

        Utilizador utilizador = utilizadorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthenticationServiceException("Utilizador autenticado não encontrado."));
        String token = jwtService.gerarToken(utilizador);
        return new AuthTokenResponse(token, "Bearer", duracaoMinutos * 60, paraResponse(utilizador));
    }

    private UtilizadorResponse paraResponse(Utilizador utilizador) {
        return new UtilizadorResponse(
                utilizador.getId(),
                utilizador.getNomeCompleto(),
                utilizador.getEmail(),
                utilizador.getNumeroTelefone(),
                utilizador.getTipo(),
                utilizador.getEstado(),
                utilizador.getDataRegisto());
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
