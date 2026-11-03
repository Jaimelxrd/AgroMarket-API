package mz.co.agromarket.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mz.co.agromarket.dto.request.PerfilProdutorRequest;
import mz.co.agromarket.dto.response.PerfilProdutorResponse;
import mz.co.agromarket.service.PerfilProdutorService;

@RestController
@Validated
@PreAuthorize("hasRole('PRODUTOR')")
@RequestMapping("/api/produtores/me")
public class PerfilProdutorController {

    private final PerfilProdutorService perfilProdutorService;

    public PerfilProdutorController(PerfilProdutorService perfilProdutorService) {
        this.perfilProdutorService = perfilProdutorService;
    }

    @GetMapping
    public PerfilProdutorResponse obter(Authentication authentication) {
        return perfilProdutorService.obter(authentication.getName());
    }

    @PutMapping
    public PerfilProdutorResponse guardar(
            Authentication authentication,
            @Valid @RequestBody PerfilProdutorRequest request) {
        return perfilProdutorService.guardar(authentication.getName(), request);
    }
}
