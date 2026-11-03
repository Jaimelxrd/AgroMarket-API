package mz.co.agromarket.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mz.co.agromarket.dto.response.PaginaResponse;
import mz.co.agromarket.dto.response.UtilizadorResponse;
import mz.co.agromarket.model.enums.TipoUtilizador;
import mz.co.agromarket.service.AdminUtilizadorService;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/utilizadores")
public class AdminUtilizadorController {

    private final AdminUtilizadorService adminUtilizadorService;

    public AdminUtilizadorController(AdminUtilizadorService adminUtilizadorService) {
        this.adminUtilizadorService = adminUtilizadorService;
    }

    @GetMapping
    public PaginaResponse<UtilizadorResponse> listar(
            @RequestParam(required = false) TipoUtilizador tipo,
            @PageableDefault(size = 20, sort = "dataRegisto", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminUtilizadorService.listar(tipo, pageable);
    }

    @PatchMapping("/{id}/ativar")
    public UtilizadorResponse ativar(@PathVariable Long id) {
        return adminUtilizadorService.ativar(id);
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(Authentication authentication, @PathVariable Long id) {
        adminUtilizadorService.desativar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
