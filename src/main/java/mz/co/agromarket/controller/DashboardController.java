package mz.co.agromarket.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mz.co.agromarket.dto.response.DashboardAdminResponse;
import mz.co.agromarket.dto.response.DashboardCompradorResponse;
import mz.co.agromarket.dto.response.DashboardProdutorResponse;
import mz.co.agromarket.service.DashboardService;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardAdminResponse dashboardAdmin() {
        return dashboardService.administrador();
    }

    @GetMapping("/api/produtores/me/dashboard")
    @PreAuthorize("hasRole('PRODUTOR')")
    public DashboardProdutorResponse dashboardProdutor(Authentication authentication) {
        return dashboardService.produtor(authentication.getName());
    }

    @GetMapping("/api/compradores/me/dashboard")
    @PreAuthorize("hasRole('COMPRADOR')")
    public DashboardCompradorResponse dashboardComprador(Authentication authentication) {
        return dashboardService.comprador(authentication.getName());
    }
}
