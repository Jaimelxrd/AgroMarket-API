package mz.co.agromarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mz.co.agromarket.dto.request.AlterarPalavraPasseRequest;
import mz.co.agromarket.dto.request.AtualizarDadosUtilizadorRequest;
import mz.co.agromarket.dto.response.UtilizadorResponse;
import mz.co.agromarket.service.GestaoUtilizadorService;

@RestController
@Validated
@RequestMapping("/api/utilizadores/me")
public class UtilizadorController {

    private final GestaoUtilizadorService gestaoUtilizadorService;

    public UtilizadorController(GestaoUtilizadorService gestaoUtilizadorService) {
        this.gestaoUtilizadorService = gestaoUtilizadorService;
    }

    @GetMapping
    public UtilizadorResponse obterPerfil(Authentication authentication) {
        return gestaoUtilizadorService.obterPerfil(authentication.getName());
    }

    @PutMapping
    public UtilizadorResponse atualizarPerfil(
            Authentication authentication,
            @Valid @RequestBody AtualizarDadosUtilizadorRequest request) {
        return gestaoUtilizadorService.atualizarPerfil(authentication.getName(), request);
    }

    @PutMapping("/palavra-passe")
    public ResponseEntity<Void> alterarPalavraPasse(
            Authentication authentication,
            @Valid @RequestBody AlterarPalavraPasseRequest request) {
        gestaoUtilizadorService.alterarPalavraPasse(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> desativarConta(Authentication authentication) {
        gestaoUtilizadorService.desativarConta(authentication.getName());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
