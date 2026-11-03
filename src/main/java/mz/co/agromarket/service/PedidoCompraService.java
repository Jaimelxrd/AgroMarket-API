package mz.co.agromarket.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mz.co.agromarket.dto.request.CriarPedidoRequest;
import mz.co.agromarket.dto.response.PaginaResponse;
import mz.co.agromarket.dto.response.PedidoCompraResponse;
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

@Service
public class PedidoCompraService {

    private final PedidoCompraRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UtilizadorRepository utilizadorRepository;

    public PedidoCompraService(
            PedidoCompraRepository pedidoRepository,
            ProdutoRepository produtoRepository,
            UtilizadorRepository utilizadorRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.utilizadorRepository = utilizadorRepository;
    }

    @Transactional
    public PedidoCompraResponse criar(String email, CriarPedidoRequest request) {
        Utilizador comprador = obterUtilizador(email, TipoUtilizador.COMPRADOR);
        Produto produto = produtoRepository.encontrarPorIdComBloqueio(request.produtoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        if (produto.getEstado() != EstadoProduto.DISPONIVEL || produto.getQuantidadeDisponivel().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este produto não está disponível para pedidos.");
        }
        if (produto.getProdutor().getId().equals(comprador.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não pode fazer pedidos aos seus próprios produtos.");
        }

        BigDecimal precoAcordado = produto.getPrecoUnidade().setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = precoAcordado.multiply(request.quantidadeSolicitada()).setScale(2, RoundingMode.HALF_UP);
        PedidoCompra pedido = new PedidoCompra();
        pedido.setComprador(comprador);
        pedido.setProdutor(produto.getProdutor());
        pedido.setProduto(produto);
        pedido.setQuantidadeSolicitada(request.quantidadeSolicitada());
        pedido.setPrecoUnitarioAcordado(precoAcordado);
        pedido.setValorTotal(total);
        pedido.setMensagem(normalizarMensagem(request.mensagem()));
        pedido.setEstado(EstadoPedido.PENDENTE);
        return paraResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PedidoCompraResponse> listarDoComprador(String email, Pageable pageable) {
        Utilizador comprador = obterUtilizador(email, TipoUtilizador.COMPRADOR);
        return paraPagina(pedidoRepository.findAllByCompradorId(comprador.getId(), limitarPagina(pageable)));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PedidoCompraResponse> listarRecebidos(String email, EstadoPedido estado, Pageable pageable) {
        Utilizador produtor = obterUtilizador(email, TipoUtilizador.PRODUTOR);
        Page<PedidoCompra> pedidos = estado == null
                ? pedidoRepository.findAllByProdutorId(produtor.getId(), limitarPagina(pageable))
                : pedidoRepository.findAllByProdutorIdAndEstado(produtor.getId(), estado, limitarPagina(pageable));
        return paraPagina(pedidos);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PedidoCompraResponse> listarTodos(EstadoPedido estado, Pageable pageable) {
        Pageable pagina = limitarPagina(pageable);
        Page<PedidoCompra> pedidos = estado == null
                ? pedidoRepository.findAll(pagina)
                : pedidoRepository.findAllByEstado(estado, pagina);
        return paraPagina(pedidos);
    }

    @Transactional(readOnly = true)
    public PedidoCompraResponse obter(String email, Long id) {
        Utilizador utilizador = obterAtivo(email);
        PedidoCompra pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
        if (!pedido.getComprador().getId().equals(utilizador.getId())
                && !pedido.getProdutor().getId().equals(utilizador.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Não tem acesso a este pedido.");
        }
        return paraResponse(pedido);
    }

    @Transactional
    public PedidoCompraResponse cancelar(String email, Long id) {
        Utilizador comprador = obterUtilizador(email, TipoUtilizador.COMPRADOR);
        PedidoCompra pedido = obterComBloqueio(id);
        verificarComprador(pedido, comprador);
        exigirEstado(pedido, EstadoPedido.PENDENTE, "Só pode cancelar pedidos pendentes.");
        pedido.setEstado(EstadoPedido.CANCELADO);
        return paraResponse(pedido);
    }

    @Transactional
    public PedidoCompraResponse aceitar(String email, Long id) {
        Utilizador produtor = obterUtilizador(email, TipoUtilizador.PRODUTOR);
        PedidoCompra pedido = obterComBloqueio(id);
        verificarProdutor(pedido, produtor);
        exigirEstado(pedido, EstadoPedido.PENDENTE, "Só pode aceitar pedidos pendentes.");

        Produto produto = produtoRepository.encontrarPorIdComBloqueio(pedido.getProduto().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        if (produto.getEstado() != EstadoProduto.DISPONIVEL
                || produto.getQuantidadeDisponivel().compareTo(pedido.getQuantidadeSolicitada()) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para aceitar este pedido.");
        }

        produto.setQuantidadeDisponivel(produto.getQuantidadeDisponivel().subtract(pedido.getQuantidadeSolicitada()));
        if (produto.getQuantidadeDisponivel().signum() == 0) {
            produto.setEstado(EstadoProduto.ESGOTADO);
        }
        pedido.setEstado(EstadoPedido.ACEITE);
        return paraResponse(pedido);
    }

    @Transactional
    public PedidoCompraResponse rejeitar(String email, Long id) {
        Utilizador produtor = obterUtilizador(email, TipoUtilizador.PRODUTOR);
        PedidoCompra pedido = obterComBloqueio(id);
        verificarProdutor(pedido, produtor);
        exigirEstado(pedido, EstadoPedido.PENDENTE, "Só pode rejeitar pedidos pendentes.");
        pedido.setEstado(EstadoPedido.REJEITADO);
        return paraResponse(pedido);
    }

    @Transactional
    public PedidoCompraResponse concluir(String email, Long id) {
        Utilizador produtor = obterUtilizador(email, TipoUtilizador.PRODUTOR);
        PedidoCompra pedido = obterComBloqueio(id);
        verificarProdutor(pedido, produtor);
        exigirEstado(pedido, EstadoPedido.ACEITE, "Só pode concluir pedidos aceites.");
        pedido.setEstado(EstadoPedido.CONCLUIDO);
        return paraResponse(pedido);
    }

    private Utilizador obterUtilizador(String email, TipoUtilizador tipo) {
        Utilizador utilizador = obterAtivo(email);
        if (utilizador.getTipo() != tipo) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissão para esta operação.");
        }
        return utilizador;
    }

    private Utilizador obterAtivo(String email) {
        Utilizador utilizador = utilizadorRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizador não encontrado."));
        if (utilizador.getEstado() != EstadoConta.ATIVA) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A conta está desativada.");
        }
        return utilizador;
    }

    private PedidoCompra obterComBloqueio(Long id) {
        return pedidoRepository.encontrarPorIdComBloqueio(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
    }

    private void verificarComprador(PedidoCompra pedido, Utilizador comprador) {
        if (!pedido.getComprador().getId().equals(comprador.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só o comprador pode alterar este pedido.");
        }
    }

    private void verificarProdutor(PedidoCompra pedido, Utilizador produtor) {
        if (!pedido.getProdutor().getId().equals(produtor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este pedido não pertence ao produtor autenticado.");
        }
    }

    private void exigirEstado(PedidoCompra pedido, EstadoPedido estado, String mensagem) {
        if (pedido.getEstado() != estado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, mensagem);
        }
    }

    private String normalizarMensagem(String mensagem) {
        return mensagem == null || mensagem.isBlank() ? null : mensagem.trim();
    }

    private Pageable limitarPagina(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), pageable.getSort());
    }

    private PaginaResponse<PedidoCompraResponse> paraPagina(Page<PedidoCompra> pagina) {
        return new PaginaResponse<>(pagina.getContent().stream().map(this::paraResponse).toList(),
                pagina.getNumber(), pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages(), pagina.isLast());
    }

    private PedidoCompraResponse paraResponse(PedidoCompra pedido) {
        return new PedidoCompraResponse(
                pedido.getId(), pedido.getComprador().getId(), pedido.getProdutor().getId(),
                pedido.getProduto().getId(), pedido.getProduto().getNome(), pedido.getQuantidadeSolicitada(),
                pedido.getPrecoUnitarioAcordado(), pedido.getValorTotal(), pedido.getMensagem(), pedido.getEstado(),
                pedido.getDataCriacao(), pedido.getDataAtualizacao());
    }
}
