package mz.co.agromarket.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mz.co.agromarket.dto.response.PaginaResponse;
import mz.co.agromarket.dto.response.PedidoCompraResponse;
import mz.co.agromarket.model.enums.EstadoPedido;
import mz.co.agromarket.service.PedidoCompraService;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/pedidos")
public class AdminPedidoController {

    private final PedidoCompraService pedidoCompraService;

    public AdminPedidoController(PedidoCompraService pedidoCompraService) {
        this.pedidoCompraService = pedidoCompraService;
    }

    @GetMapping
    public PaginaResponse<PedidoCompraResponse> listar(
            @RequestParam(required = false) EstadoPedido estado,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        return pedidoCompraService.listarTodos(estado, pageable);
    }
}
