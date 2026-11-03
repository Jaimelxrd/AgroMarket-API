package mz.co.agromarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import mz.co.agromarket.dto.request.LoginRequest;
import mz.co.agromarket.dto.request.RegistoUtilizadorRequest;
import mz.co.agromarket.model.entity.Utilizador;
import mz.co.agromarket.model.enums.EstadoConta;
import mz.co.agromarket.model.enums.TipoContaPublica;
import mz.co.agromarket.model.enums.TipoUtilizador;
import mz.co.agromarket.repository.UtilizadorRepository;
import mz.co.agromarket.security.JwtService;

class AuthServiceTest {

    private UtilizadorRepository utilizadorRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void configurar() {
        utilizadorRepository = org.mockito.Mockito.mock(UtilizadorRepository.class);
        passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        authenticationManager = org.mockito.Mockito.mock(AuthenticationManager.class);
        jwtService = org.mockito.Mockito.mock(JwtService.class);
        authService = new AuthService(utilizadorRepository, passwordEncoder, authenticationManager, jwtService, 15);
    }

    @Test
    void registarNormalizaEmailCodificaPalavraPasseERegistaComoComprador() {
        when(utilizadorRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("palavra-segura")).thenReturn("hash-seguro");
        when(utilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> {
            Utilizador utilizador = invocation.getArgument(0);
            utilizador.setId(7L);
            return utilizador;
        });

        var resposta = authService.registar(new RegistoUtilizadorRequest(
                " Ana Silva ", " ANA@EXAMPLE.COM ", "+258841234567", "palavra-segura", TipoContaPublica.COMPRADOR));

        assertEquals(7L, resposta.id());
        assertEquals("ana@example.com", resposta.email());
        assertEquals(TipoUtilizador.COMPRADOR, resposta.tipo());
        verify(passwordEncoder).encode("palavra-segura");
    }

    @Test
    void registoDeEmailExistenteRespondeComConflito() {
        when(utilizadorRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.registar(new RegistoUtilizadorRequest(
                        "Ana Silva", "ana@example.com", "+258841234567", "palavra-segura", TipoContaPublica.PRODUTOR)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void loginDevolveTokenEExpiracaoConfigurada() {
        when(utilizadorRepository.findByEmailIgnoreCase("ana@example.com"))
                .thenReturn(java.util.Optional.of(utilizador(7L, "ana@example.com", TipoUtilizador.PRODUTOR)));
        when(jwtService.gerarToken(any(Utilizador.class))).thenReturn("jwt-de-teste");

        var resposta = authService.autenticar(new LoginRequest(" ANA@EXAMPLE.COM ", "palavra-segura"));

        assertEquals("jwt-de-teste", resposta.accessToken());
        assertEquals("Bearer", resposta.tokenType());
        assertEquals(900, resposta.expiresInSeconds());
        assertEquals(TipoUtilizador.PRODUTOR, resposta.utilizador().tipo());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void loginComCredenciaisInvalidasRespondeComNaoAutorizado() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.autenticar(new LoginRequest("ana@example.com", "errada")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    private Utilizador utilizador(Long id, String email, TipoUtilizador tipo) {
        Utilizador utilizador = new Utilizador();
        utilizador.setId(id);
        utilizador.setNomeCompleto("Ana Silva");
        utilizador.setEmail(email);
        utilizador.setNumeroTelefone("+258841234567");
        utilizador.setTipo(tipo);
        utilizador.setEstado(EstadoConta.ATIVA);
        return utilizador;
    }
}
