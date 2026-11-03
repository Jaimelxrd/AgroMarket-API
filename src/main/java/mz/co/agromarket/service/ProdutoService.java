package mz.co.agromarket.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import mz.co.agromarket.dto.request.ProdutoRequest;
import mz.co.agromarket.dto.response.PaginaResponse;
import mz.co.agromarket.dto.response.ProdutoResponse;
import mz.co.agromarket.model.entity.Categoria;
import mz.co.agromarket.model.entity.FotografiaProduto;
import mz.co.agromarket.model.entity.Produto;
import mz.co.agromarket.model.entity.Utilizador;
import mz.co.agromarket.model.enums.EstadoCategoria;
import mz.co.agromarket.model.enums.EstadoProduto;
import mz.co.agromarket.model.enums.TipoUtilizador;
import mz.co.agromarket.repository.CategoriaRepository;
import mz.co.agromarket.repository.FotografiaProdutoRepository;
import mz.co.agromarket.repository.ProdutoRepository;
import mz.co.agromarket.repository.UtilizadorRepository;

@Service
public class ProdutoService {

    private static final int MAXIMO_FOTOGRAFIAS_POR_PRODUTO = 5;
    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UtilizadorRepository utilizadorRepository;
    private final FotografiaProdutoRepository fotografiaRepository;
    private final ArmazenamentoFotografiaService armazenamento;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            CategoriaRepository categoriaRepository,
            UtilizadorRepository utilizadorRepository,
            FotografiaProdutoRepository fotografiaRepository,
            ArmazenamentoFotografiaService armazenamento) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.utilizadorRepository = utilizadorRepository;
        this.fotografiaRepository = fotografiaRepository;
        this.armazenamento = armazenamento;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ProdutoResponse> pesquisar(
            String nome, Long categoriaId, String provincia, String distrito,
            BigDecimal precoMinimo, BigDecimal precoMaximo, Long produtorId,
            Boolean disponivel, Pageable pageable) {
        validarIntervaloPrecos(precoMinimo, precoMaximo);
        Pageable pagina = limitarPagina(pageable);
        Page<Produto> produtos = produtoRepository.pesquisar(
                normalizar(nome), categoriaId, normalizar(provincia), normalizar(distrito),
                precoMinimo, precoMaximo, produtorId, null,
                disponivel == null ? Boolean.TRUE : disponivel, pagina);
        return paraPagina(produtos);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ProdutoResponse> listarDoProdutor(String email, Pageable pageable) {
        Utilizador produtor = obterProdutor(email);
        return paraPagina(produtoRepository.findAllByProdutorId(produtor.getId(), limitarPagina(pageable)));
    }

    @Transactional(readOnly = true)
    public ProdutoResponse obterDisponivel(Long id) {
        Produto produto = produtoRepository.findById(id)
                .filter(this::estaDisponivel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        return paraResponse(produto, fotografiasPorProduto(List.of(produto.getId())));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ProdutoResponse> listarParaAdministracao(EstadoProduto estado, Pageable pageable) {
        Pageable pagina = limitarPagina(pageable);
        Page<Produto> produtos = estado == null
                ? produtoRepository.findAll(pagina)
                : produtoRepository.findAllByEstadoOrderByDataPublicacaoDesc(estado, pagina);
        return paraPagina(produtos);
    }

    @Transactional
    public ProdutoResponse aprovar(Long id) {
        Produto produto = produtoRepository.encontrarPorIdComBloqueio(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        if (produto.getEstado() != EstadoProduto.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Só é possível aprovar produtos pendentes.");
        }
        produto.setEstado(produto.getQuantidadeDisponivel().signum() > 0
                ? EstadoProduto.DISPONIVEL
                : EstadoProduto.ESGOTADO);
        return paraResponse(produto, fotografiasPorProduto(List.of(id)));
    }

    @Transactional
    public ProdutoResponse rejeitar(Long id) {
        Produto produto = produtoRepository.encontrarPorIdComBloqueio(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        if (produto.getEstado() != EstadoProduto.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Só é possível rejeitar produtos pendentes.");
        }
        produto.setEstado(EstadoProduto.REJEITADO);
        return paraResponse(produto, fotografiasPorProduto(List.of(id)));
    }

    @Transactional
    public ProdutoResponse criar(String email, ProdutoRequest request) {
        Utilizador produtor = obterProdutor(email);
        Produto produto = new Produto();
        produto.setProdutor(produtor);
        preencher(produto, request);
        produto.setEstado(EstadoProduto.PENDENTE);
        produto = produtoRepository.save(produto);
        return paraResponse(produto, Map.of());
    }

    @Transactional
    public ProdutoResponse atualizar(String email, Long id, ProdutoRequest request) {
        Utilizador produtor = obterProdutor(email);
        Produto produto = produtoRepository.findByIdAndProdutorId(id, produtor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        preencher(produto, request);
        produto.setEstado(EstadoProduto.PENDENTE);
        produto = produtoRepository.save(produto);
        return paraResponse(produto, fotografiasPorProduto(List.of(id)));
    }

    @Transactional
    public void eliminar(String email, Long id) {
        Utilizador produtor = obterProdutor(email);
        Produto produto = produtoRepository.findByIdAndProdutorId(id, produtor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        produto.setEstado(EstadoProduto.INDISPONIVEL);
    }

    @Transactional
    public List<String> adicionarFotografias(String email, Long produtoId, List<MultipartFile> ficheiros) {
        Utilizador produtor = obterProdutor(email);
        Produto produto = produtoRepository.findByIdAndProdutorId(produtoId, produtor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        if (ficheiros == null || ficheiros.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie pelo menos uma fotografia.");
        }
        List<FotografiaProduto> existentes = fotografiaRepository.findAllByProdutoIdOrderByOrdemAsc(produtoId);
        if (existentes.size() + ficheiros.size() > MAXIMO_FOTOGRAFIAS_POR_PRODUTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cada produto pode ter no máximo 5 fotografias.");
        }

        List<String> caminhosCriados = new ArrayList<>();
        List<FotografiaProduto> fotografias = new ArrayList<>();
        try {
            int ordem = existentes.size();
            for (MultipartFile ficheiro : ficheiros) {
                String caminho = armazenamento.guardar(ficheiro);
                caminhosCriados.add(caminho);
                FotografiaProduto fotografia = new FotografiaProduto();
                fotografia.setProduto(produto);
                fotografia.setCaminhoFicheiro(caminho);
                fotografia.setOrdem(ordem++);
                fotografias.add(fotografia);
            }
            fotografiaRepository.saveAll(fotografias);
            return fotografias.stream().map(f -> urlFotografia(produtoId, f.getId())).toList();
        } catch (RuntimeException exception) {
            caminhosCriados.forEach(armazenamento::eliminar);
            throw exception;
        }
    }

    @Transactional
    public void eliminarFotografia(String email, Long produtoId, Long fotografiaId) {
        Utilizador produtor = obterProdutor(email);
        produtoRepository.findByIdAndProdutorId(produtoId, produtor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        FotografiaProduto fotografia = fotografiaRepository.findByIdAndProdutoId(fotografiaId, produtoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fotografia não encontrada."));
        fotografiaRepository.delete(fotografia);
        armazenamento.eliminar(fotografia.getCaminhoFicheiro());
    }

    @Transactional(readOnly = true)
    public Resource obterFotografia(Long produtoId, Long fotografiaId, Authentication authentication) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        FotografiaProduto fotografia = fotografiaRepository.findByIdAndProdutoId(fotografiaId, produtoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fotografia não encontrada."));
        boolean admin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean dono = authentication != null && authentication.isAuthenticated()
                && produto.getProdutor().getEmail().equalsIgnoreCase(authentication.getName());
        if (!estaDisponivel(produto) && !dono && !admin) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fotografia não encontrada.");
        }
        return armazenamento.carregar(fotografia.getCaminhoFicheiro());
    }

    public String tipoConteudoFotografia(Long produtoId, Long fotografiaId) {
        FotografiaProduto fotografia = fotografiaRepository.findByIdAndProdutoId(fotografiaId, produtoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fotografia não encontrada."));
        return armazenamento.obterTipoConteudo(fotografia.getCaminhoFicheiro());
    }

    private void preencher(Produto produto, ProdutoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .filter(c -> c.getEstado() == EstadoCategoria.ATIVA)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria não encontrada ou inativa."));
        produto.setNome(request.nome().trim());
        produto.setDescricao(request.descricao());
        produto.setCategoria(categoria);
        produto.setPrecoUnidade(request.precoUnidade());
        produto.setUnidadeMedida(request.unidadeMedida());
        produto.setQuantidadeDisponivel(request.quantidadeDisponivel());
        produto.setProvincia(request.provincia().trim());
        produto.setDistrito(request.distrito().trim());
        produto.setLocalidade(request.localidade());
    }

    private Utilizador obterProdutor(String email) {
        Utilizador utilizador = utilizadorRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizador não encontrado."));
        if (utilizador.getTipo() != TipoUtilizador.PRODUTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas produtores podem gerir produtos.");
        }
        if (utilizador.getEstado() != mz.co.agromarket.model.enums.EstadoConta.ATIVA) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A conta está desativada.");
        }
        return utilizador;
    }

    private boolean estaDisponivel(Produto produto) {
        return produto.getEstado() == EstadoProduto.DISPONIVEL
                && produto.getQuantidadeDisponivel().compareTo(BigDecimal.ZERO) > 0;
    }

    private void validarIntervaloPrecos(BigDecimal minimo, BigDecimal maximo) {
        if (minimo != null && minimo.signum() < 0 || maximo != null && maximo.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os preços não podem ser negativos.");
        }
        if (minimo != null && maximo != null && minimo.compareTo(maximo) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O preço mínimo não pode exceder o máximo.");
        }
    }

    private Pageable limitarPagina(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), pageable.getSort());
    }

    private PaginaResponse<ProdutoResponse> paraPagina(Page<Produto> pagina) {
        List<Long> ids = pagina.getContent().stream().map(Produto::getId).toList();
        Map<Long, List<String>> fotografias = fotografiasPorProduto(ids);
        return new PaginaResponse<>(
                pagina.getContent().stream().map(p -> paraResponse(p, fotografias)).toList(),
                pagina.getNumber(), pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages(), pagina.isLast());
    }

    private Map<Long, List<String>> fotografiasPorProduto(Collection<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        return fotografiaRepository.findAllByProdutoIdInOrderByOrdemAsc(ids).stream()
                .collect(Collectors.groupingBy(f -> f.getProduto().getId(), Collectors.mapping(
                        f -> urlFotografia(f.getProduto().getId(), f.getId()), Collectors.toList())));
    }

    private ProdutoResponse paraResponse(Produto p, Map<Long, List<String>> fotografias) {
        return new ProdutoResponse(
                p.getId(), p.getNome(), p.getDescricao(), p.getCategoria().getId(), p.getCategoria().getNome(),
                p.getProdutor().getId(), p.getProdutor().getNomeCompleto(), p.getPrecoUnidade(), p.getUnidadeMedida(),
                p.getQuantidadeDisponivel(), p.getProvincia(), p.getDistrito(), p.getLocalidade(),
                fotografias.getOrDefault(p.getId(), List.of()), p.getEstado(), p.getDataPublicacao());
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String urlFotografia(Long produtoId, Long fotografiaId) {
        return "/api/produtos/" + produtoId + "/fotografias/" + fotografiaId;
    }
}
