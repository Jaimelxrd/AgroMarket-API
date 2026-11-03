package mz.co.agromarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import mz.co.agromarket.dto.request.CriarPedidoRequest;
import mz.co.agromarket.model.entity.Categoria;
import mz.co.agromarket.model.entity.PedidoCompra;
import mz.co.agromarket.model.entity.Produto;
import mz.co.agromarket.model.entity.Utilizador;
import mz.co.agromarket.model.enums.EstadoConta;
import mz.co.agromarket.model.enums.EstadoPedido;
import mz.co.agromarket.model.enums.EstadoProduto;
import mz.co.agromarket.model.enums.TipoUtilizador;
import mz.co.agromarket.repository.PedidoCompraRepository;
import mz.co.agromarket.repository.ProdutoRepository;
import mz.co.agromarket.repository.UtilizadorRepository;

class PedidoCompraServiceTest {

    private PedidoCompraRepository pedidoRepository;
    private ProdutoRepository produtoRepository;
    private UtilizadorRepository utilizadorRepository;
    private PedidoCompraService pedidoService;

    @BeforeEach
    void configurar() {
        pedidoRepository = org.mockito.Mockito.mock(PedidoCompraRepository.class);
        produtoRepository = org.mockito.Mockito.mock(ProdutoRepository.class);
        utilizadorRepository = org.mockito.Mockito.mock(UtilizadorRepository.class);
        pedidoService = new PedidoCompraService(pedidoRepository, produtoRepository, utilizadorRepository);
    }

    @Test
    void criarPedidoGuardaPrecoTotalEEstadoPendente() {
        Utilizador comprador = utilizador(1L, "comprador@example.com", TipoUtilizador.COMPRADOR);
        Produto produto = produto(10L, "produtor@example.com", "25.50", "100");
        when(utilizadorRepository.findByEmailIgnoreCase("comprador@example.com")).thenReturn(Optional.of(comprador));
        when(produtoRepository.encontrarPorIdComBloqueio(10L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(PedidoCompra.class))).thenAnswer(invocation -> {
            PedidoCompra pedido = invocation.getArgument(0);
            pedido.setId(40L);
            return pedido;
        });

        var resposta = pedidoService.criar("comprador@example.com",
                new CriarPedidoRequest(10L, new BigDecimal("2.500"), " Entregar de manhã "));

        assertEquals(40L, resposta.id());
        assertEquals(EstadoPedido.PENDENTE, resposta.estado());
        assertEquals(new BigDecimal("25.50"), resposta.precoUnitarioAcordado());
        assertEquals(new BigDecimal("63.75"), resposta.valorTotal());
        assertEquals("Entregar de manhã", resposta.mensagem());
    }

    @Test
    void aceitarPedidoReduzStockEAlteraEstado() {
        Utilizador produtor = utilizador(2L, "produtor@example.com", TipoUtilizador.PRODUTOR);
        Produto produto = produto(10L, "produtor@example.com", "25.50", "5");
        PedidoCompra pedido = pedido(40L, produtor, produto, "2", EstadoPedido.PENDENTE);
        when(utilizadorRepository.findByEmailIgnoreCase("produtor@example.com")).thenReturn(Optional.of(produtor));
        when(pedidoRepository.encontrarPorIdComBloqueio(40L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.encontrarPorIdComBloqueio(10L)).thenReturn(Optional.of(produto));

        var resposta = pedidoService.aceitar("produtor@example.com", 40L);

        assertEquals(EstadoPedido.ACEITE, resposta.estado());
        assertEquals(new BigDecimal("3"), produto.getQuantidadeDisponivel());
    }

    @Test
    void aceitarPedidoQueEsgotaStockMarcaProdutoComoEsgotado() {
        Utilizador produtor = utilizador(2L, "produtor@example.com", TipoUtilizador.PRODUTOR);
        Produto produto = produto(10L, "produtor@example.com", "25.50", "2");
        PedidoCompra pedido = pedido(40L, produtor, produto, "2", EstadoPedido.PENDENTE);
        when(utilizadorRepository.findByEmailIgnoreCase("produtor@example.com")).thenReturn(Optional.of(produtor));
        when(pedidoRepository.encontrarPorIdComBloqueio(40L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.encontrarPorIdComBloqueio(10L)).thenReturn(Optional.of(produto));

        pedidoService.aceitar("produtor@example.com", 40L);

        assertEquals(BigDecimal.ZERO, produto.getQuantidadeDisponivel());
        assertEquals(EstadoProduto.ESGOTADO, produto.getEstado());
    }

    @Test
    void naoAceitaPedidoQuandoStockEInsuficiente() {
        Utilizador produtor = utilizador(2L, "produtor@example.com", TipoUtilizador.PRODUTOR);
        Produto produto = produto(10L, "produtor@example.com", "25.50", "1");
        PedidoCompra pedido = pedido(40L, produtor, produto, "2", EstadoPedido.PENDENTE);
        when(utilizadorRepository.findByEmailIgnoreCase("produtor@example.com")).thenReturn(Optional.of(produtor));
        when(pedidoRepository.encontrarPorIdComBloqueio(40L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.encontrarPorIdComBloqueio(10L)).thenReturn(Optional.of(produto));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> pedidoService.aceitar("produtor@example.com", 40L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(new BigDecimal("1"), produto.getQuantidadeDisponivel());
        assertEquals(EstadoPedido.PENDENTE, pedido.getEstado());
    }

    @Test
    void produtorNaoPodeAlterarPedidoDeOutroProdutor() {
        Utilizador produtorAutenticado = utilizador(3L, "outro@example.com", TipoUtilizador.PRODUTOR);
        Utilizador donoDoPedido = utilizador(2L, "produtor@example.com", TipoUtilizador.PRODUTOR);
        Produto produto = produto(10L, "produtor@example.com", "25.50", "5");
        PedidoCompra pedido = pedido(40L, donoDoPedido, produto, "1", EstadoPedido.PENDENTE);
        when(utilizadorRepository.findByEmailIgnoreCase("outro@example.com")).thenReturn(Optional.of(produtorAutenticado));
        when(pedidoRepository.encontrarPorIdComBloqueio(40L)).thenReturn(Optional.of(pedido));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> pedidoService.aceitar("outro@example.com", 40L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(produtoRepository, never()).encontrarPorIdComBloqueio(10L);
    }

    @Test
    void produtorPodeRejeitarPedidoPendenteProprio() {
        Utilizador produtor = utilizador(2L, "produtor@example.com", TipoUtilizador.PRODUTOR);
        Produto produto = produto(10L, "produtor@example.com", "25.50", "5");
        PedidoCompra pedido = pedido(40L, produtor, produto, "1", EstadoPedido.PENDENTE);
        when(utilizadorRepository.findByEmailIgnoreCase("produtor@example.com")).thenReturn(Optional.of(produtor));
        when(pedidoRepository.encontrarPorIdComBloqueio(40L)).thenReturn(Optional.of(pedido));

        var resposta = pedidoService.rejeitar("produtor@example.com", 40L);

        assertEquals(EstadoPedido.REJEITADO, resposta.estado());
        assertEquals(EstadoPedido.REJEITADO, pedido.getEstado());
    }

    @Test
    void produtorPodeConcluirPedidoAceiteProprio() {
        Utilizador produtor = utilizador(2L, "produtor@example.com", TipoUtilizador.PRODUTOR);
        Produto produto = produto(10L, "produtor@example.com", "25.50", "5");
        PedidoCompra pedido = pedido(40L, produtor, produto, "1", EstadoPedido.ACEITE);
        when(utilizadorRepository.findByEmailIgnoreCase("produtor@example.com")).thenReturn(Optional.of(produtor));
        when(pedidoRepository.encontrarPorIdComBloqueio(40L)).thenReturn(Optional.of(pedido));

        var resposta = pedidoService.concluir("produtor@example.com", 40L);

        assertEquals(EstadoPedido.CONCLUIDO, resposta.estado());
        assertEquals(EstadoPedido.CONCLUIDO, pedido.getEstado());
    }

    @Test
    void pedidoAceiteNaoPodeSerCanceladoPeloComprador() {
        Utilizador comprador = utilizador(1L, "comprador@example.com", TipoUtilizador.COMPRADOR);
        Utilizador produtor = utilizador(2L, "produtor@example.com", TipoUtilizador.PRODUTOR);
        Produto produto = produto(10L, "produtor@example.com", "25.50", "5");
        PedidoCompra pedido = pedido(40L, produtor, produto, "1", EstadoPedido.ACEITE);
        pedido.setComprador(comprador);
        when(utilizadorRepository.findByEmailIgnoreCase("comprador@example.com")).thenReturn(Optional.of(comprador));
        when(pedidoRepository.encontrarPorIdComBloqueio(40L)).thenReturn(Optional.of(pedido));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> pedidoService.cancelar("comprador@example.com", 40L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    private Utilizador utilizador(Long id, String email, TipoUtilizador tipo) {
        Utilizador utilizador = new Utilizador();
        utilizador.setId(id);
        utilizador.setNomeCompleto("Utilizador de teste");
        utilizador.setEmail(email);
        utilizador.setTipo(tipo);
        utilizador.setEstado(EstadoConta.ATIVA);
        return utilizador;
    }

    private Produto produto(Long id, String produtorEmail, String preco, String stock) {
        Utilizador produtor = utilizador(2L, produtorEmail, TipoUtilizador.PRODUTOR);
        Categoria categoria = new Categoria();
        categoria.setId(5L);
        categoria.setNome("Cereais");
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome("Milho");
        produto.setProdutor(produtor);
        produto.setCategoria(categoria);
        produto.setPrecoUnidade(new BigDecimal(preco));
        produto.setQuantidadeDisponivel(new BigDecimal(stock));
        produto.setEstado(EstadoProduto.DISPONIVEL);
        return produto;
    }

    private PedidoCompra pedido(Long id, Utilizador produtor, Produto produto, String quantidade, EstadoPedido estado) {
        PedidoCompra pedido = new PedidoCompra();
        pedido.setId(id);
        pedido.setComprador(utilizador(1L, "comprador@example.com", TipoUtilizador.COMPRADOR));
        pedido.setProdutor(produtor);
        pedido.setProduto(produto);
        pedido.setQuantidadeSolicitada(new BigDecimal(quantidade));
        pedido.setPrecoUnitarioAcordado(produto.getPrecoUnidade());
        pedido.setValorTotal(produto.getPrecoUnidade().multiply(new BigDecimal(quantidade)));
        pedido.setEstado(estado);
        return pedido;
    }
}
