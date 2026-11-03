package mz.co.agromarket.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mz.co.agromarket.dto.request.CriarPedidoRequest;
import mz.co.agromarket.dto.response.PaginaResponse;
import mz.co.agromarket.dto.response.PedidoCompraResponse;
import mz.co.agromarket.model.enums.EstadoPedido;
import mz.co.agromarket.service.PedidoCompraService;

@RestController
@Validated
public class PedidoCompraController {

    private final PedidoCompraService pedidoCompraService;

    public PedidoCompraController(PedidoCompraService pedidoCompraService) {
        this.pedidoCompraService = pedidoCompraService;
    }

    @PostMapping("/api/pedidos")
    @PreAuthorize("hasRole('COMPRADOR')")
    public ResponseEntity<PedidoCompraResponse> criar(
            Authentication authentication,
            @Valid @RequestBody CriarPedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoCompraService.criar(authentication.getName(), request));
    }

    @GetMapping("/api/pedidos/meus")
    @PreAuthorize("hasRole('COMPRADOR')")
    public PaginaResponse<PedidoCompraResponse> meusPedidos(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        return pedidoCompraService.listarDoComprador(authentication.getName(), pageable);
    }

    @GetMapping("/api/encomendas/recebidas")
    @PreAuthorize("hasRole('PRODUTOR')")
    public PaginaResponse<PedidoCompraResponse> pedidosRecebidos(
            Authentication authentication,
            @RequestParam(required = false) EstadoPedido estado,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        return pedidoCompraService.listarRecebidos(authentication.getName(), estado, pageable);
    }

    @GetMapping("/api/encomendas/pendentes")
    @PreAuthorize("hasRole('PRODUTOR')")
    public PaginaResponse<PedidoCompraResponse> pedidosPendentes(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        return pedidoCompraService.listarRecebidos(authentication.getName(), EstadoPedido.PENDENTE, pageable);
    }

    @GetMapping("/api/pedidos/{id}")
    public PedidoCompraResponse obter(Authentication authentication, @PathVariable Long id) {
        return pedidoCompraService.obter(authentication.getName(), id);
    }

    @PostMapping("/api/pedidos/{id}/cancelar")
    @PreAuthorize("hasRole('COMPRADOR')")
    public PedidoCompraResponse cancelar(Authentication authentication, @PathVariable Long id) {
        return pedidoCompraService.cancelar(authentication.getName(), id);
    }

    @PostMapping("/api/encomendas/{id}/aceitar")
    @PreAuthorize("hasRole('PRODUTOR')")
    public PedidoCompraResponse aceitar(Authentication authentication, @PathVariable Long id) {
        return pedidoCompraService.aceitar(authentication.getName(), id);
    }

    @PostMapping("/api/encomendas/{id}/rejeitar")
    @PreAuthorize("hasRole('PRODUTOR')")
    public PedidoCompraResponse rejeitar(Authentication authentication, @PathVariable Long id) {
        return pedidoCompraService.rejeitar(authentication.getName(), id);
    }

    @PostMapping("/api/encomendas/{id}/concluir")
    @PreAuthorize("hasRole('PRODUTOR')")
    public PedidoCompraResponse concluir(Authentication authentication, @PathVariable Long id) {
        return pedidoCompraService.concluir(authentication.getName(), id);
    }
}
