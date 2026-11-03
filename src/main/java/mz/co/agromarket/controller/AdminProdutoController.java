package mz.co.agromarket.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mz.co.agromarket.dto.response.PaginaResponse;
import mz.co.agromarket.dto.response.ProdutoResponse;
import mz.co.agromarket.model.enums.EstadoProduto;
import mz.co.agromarket.service.ProdutoService;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/produtos")
public class AdminProdutoController {

    private final ProdutoService produtoService;

    public AdminProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public PaginaResponse<ProdutoResponse> listar(
            @RequestParam(required = false, defaultValue = "PENDENTE") EstadoProduto estado,
            @PageableDefault(size = 20, sort = "dataPublicacao", direction = Sort.Direction.DESC) Pageable pageable) {
        return produtoService.listarParaAdministracao(estado, pageable);
    }

    @PostMapping("/{id}/aprovar")
    public ProdutoResponse aprovar(@PathVariable Long id) {
        return produtoService.aprovar(id);
    }

    @PostMapping("/{id}/rejeitar")
    public ProdutoResponse rejeitar(@PathVariable Long id) {
        return produtoService.rejeitar(id);
    }
}
