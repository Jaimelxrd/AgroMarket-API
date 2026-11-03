package mz.co.agromarket.service;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mz.co.agromarket.dto.response.DashboardAdminResponse;
import mz.co.agromarket.dto.response.DashboardCompradorResponse;
import mz.co.agromarket.dto.response.DashboardProdutorResponse;
import mz.co.agromarket.dto.response.EstatisticaAgrupadaResponse;
import mz.co.agromarket.model.entity.Utilizador;
import mz.co.agromarket.model.enums.EstadoPedido;
import mz.co.agromarket.model.enums.EstadoProduto;
import mz.co.agromarket.model.enums.TipoUtilizador;
import mz.co.agromarket.repository.PedidoCompraRepository;
import mz.co.agromarket.repository.ProdutoRepository;
import mz.co.agromarket.repository.UtilizadorRepository;

@Service
public class DashboardService {

    private final UtilizadorRepository utilizadorRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoCompraRepository pedidoRepository;

    public DashboardService(
            UtilizadorRepository utilizadorRepository,
            ProdutoRepository produtoRepository,
            PedidoCompraRepository pedidoRepository) {
        this.utilizadorRepository = utilizadorRepository;
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardAdminResponse administrador() {
        return new DashboardAdminResponse(
                utilizadorRepository.count(),
                utilizadorRepository.countByTipo(TipoUtilizador.PRODUTOR),
                utilizadorRepository.countByTipo(TipoUtilizador.COMPRADOR),
                produtoRepository.count(),
                pedidoRepository.count(),
                pedidoRepository.countByEstado(EstadoPedido.PENDENTE),
                pedidoRepository.countByEstado(EstadoPedido.CONCLUIDO),
                mapearAgrupamentos(produtoRepository.contarProdutosPorCategoria()),
                mapearAgrupamentos(produtoRepository.contarProdutosPorProvincia()));
    }

    @Transactional(readOnly = true)
    public DashboardProdutorResponse produtor(String email) {
        Utilizador produtor = obterUtilizador(email, TipoUtilizador.PRODUTOR);
        return new DashboardProdutorResponse(
                produtoRepository.countByProdutorId(produtor.getId()),
                produtoRepository.countByProdutorIdAndEstado(produtor.getId(), EstadoProduto.DISPONIVEL),
                pedidoRepository.countByProdutorId(produtor.getId()),
                pedidoRepository.countByProdutorIdAndEstado(produtor.getId(), EstadoPedido.PENDENTE),
                pedidoRepository.countByProdutorIdAndEstado(produtor.getId(), EstadoPedido.CONCLUIDO));
    }

    @Transactional(readOnly = true)
    public DashboardCompradorResponse comprador(String email) {
        Utilizador comprador = obterUtilizador(email, TipoUtilizador.COMPRADOR);
        return new DashboardCompradorResponse(
                pedidoRepository.countByCompradorId(comprador.getId()),
                pedidoRepository.countByCompradorIdAndEstado(comprador.getId(), EstadoPedido.PENDENTE),
                pedidoRepository.countByCompradorIdAndEstado(comprador.getId(), EstadoPedido.ACEITE),
                pedidoRepository.countByCompradorIdAndEstado(comprador.getId(), EstadoPedido.CONCLUIDO));
    }

    private Utilizador obterUtilizador(String email, TipoUtilizador tipo) {
        Utilizador utilizador = utilizadorRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizador não encontrado."));
        if (utilizador.getTipo() != tipo) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissão para este dashboard.");
        }
        return utilizador;
    }

    private List<EstatisticaAgrupadaResponse> mapearAgrupamentos(List<Object[]> resultados) {
        return resultados.stream()
                .map(linha -> new EstatisticaAgrupadaResponse((String) linha[0], ((Number) linha[1]).longValue()))
                .toList();
    }
}
