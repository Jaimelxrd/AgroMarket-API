package mz.co.agromarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import mz.co.agromarket.dto.request.ProdutoRequest;
import mz.co.agromarket.model.entity.Categoria;
import mz.co.agromarket.model.entity.Produto;
import mz.co.agromarket.model.entity.Utilizador;
import mz.co.agromarket.model.enums.EstadoCategoria;
import mz.co.agromarket.model.enums.EstadoConta;
import mz.co.agromarket.model.enums.EstadoProduto;
import mz.co.agromarket.model.enums.TipoUtilizador;
import mz.co.agromarket.model.enums.UnidadeMedida;
import mz.co.agromarket.repository.CategoriaRepository;
import mz.co.agromarket.repository.FotografiaProdutoRepository;
import mz.co.agromarket.repository.ProdutoRepository;
import mz.co.agromarket.repository.UtilizadorRepository;

class ProdutoServiceTest {

    private ProdutoRepository produtoRepository;
    private CategoriaRepository categoriaRepository;
    private UtilizadorRepository utilizadorRepository;
    private FotografiaProdutoRepository fotografiaRepository;
    private ArmazenamentoFotografiaService armazenamento;
    private ProdutoService produtoService;

    @BeforeEach
    void configurar() {
        produtoRepository = org.mockito.Mockito.mock(ProdutoRepository.class);
        categoriaRepository = org.mockito.Mockito.mock(CategoriaRepository.class);
        utilizadorRepository = org.mockito.Mockito.mock(UtilizadorRepository.class);
        fotografiaRepository = org.mockito.Mockito.mock(FotografiaProdutoRepository.class);
        armazenamento = org.mockito.Mockito.mock(ArmazenamentoFotografiaService.class);
        produtoService = new ProdutoService(produtoRepository, categoriaRepository, utilizadorRepository,
                fotografiaRepository, armazenamento);
    }

    @Test
    void criarProdutoDeProdutorAtivoFicaPendenteDeModeracao() {
        Utilizador produtor = utilizador(2L, "produtor@example.com", TipoUtilizador.PRODUTOR);
        Categoria categoria = categoria(4L, EstadoCategoria.ATIVA);
        when(utilizadorRepository.findByEmailIgnoreCase("produtor@example.com")).thenReturn(Optional.of(produtor));
        when(categoriaRepository.findById(4L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> {
            Produto produto = invocation.getArgument(0);
            produto.setId(9L);
            return produto;
        });

        var resposta = produtoService.criar(" PRODUTOR@EXAMPLE.COM ", pedidoProduto());

        assertEquals(9L, resposta.id());
        assertEquals(EstadoProduto.PENDENTE, resposta.estado());
        assertEquals(new BigDecimal("25.50"), resposta.precoUnidade());
    }

    @Test
    void utilizadorQueNaoEProdutorNaoPodeCriarProduto() {
        when(utilizadorRepository.findByEmailIgnoreCase("comprador@example.com"))
                .thenReturn(Optional.of(utilizador(3L, "comprador@example.com", TipoUtilizador.COMPRADOR)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> produtoService.criar("comprador@example.com", pedidoProduto()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void produtoSoPodeSerAprovadoQuandoPendente() {
        Produto produto = produto(9L, EstadoProduto.PENDENTE, "10");
        when(produtoRepository.encontrarPorIdComBloqueio(9L)).thenReturn(Optional.of(produto));
        when(fotografiaRepository.findAllByProdutoIdInOrderByOrdemAsc(any())).thenReturn(java.util.List.of());

        var resposta = produtoService.aprovar(9L);

        assertEquals(EstadoProduto.DISPONIVEL, resposta.estado());
        assertEquals(EstadoProduto.DISPONIVEL, produto.getEstado());
    }

    @Test
    void aprovarProdutoNaoPendenteRespondeComConflito() {
        Produto produto = produto(9L, EstadoProduto.DISPONIVEL, "10");
        when(produtoRepository.encontrarPorIdComBloqueio(9L)).thenReturn(Optional.of(produto));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> produtoService.aprovar(9L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void atualizarProdutoProprioRecolocaAnuncioEmModeracao() {
        Utilizador produtor = utilizador(2L, "produtor@example.com", TipoUtilizador.PRODUTOR);
        Produto produto = produto(9L, EstadoProduto.DISPONIVEL, "10");
        when(utilizadorRepository.findByEmailIgnoreCase("produtor@example.com")).thenReturn(Optional.of(produtor));
        when(produtoRepository.findByIdAndProdutorId(9L, 2L)).thenReturn(Optional.of(produto));
        when(categoriaRepository.findById(4L)).thenReturn(Optional.of(categoria(4L, EstadoCategoria.ATIVA)));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fotografiaRepository.findAllByProdutoIdInOrderByOrdemAsc(any())).thenReturn(List.of());

        var resposta = produtoService.atualizar("produtor@example.com", 9L, pedidoProduto());

        assertEquals(EstadoProduto.PENDENTE, resposta.estado());
        assertEquals(EstadoProduto.PENDENTE, produto.getEstado());
    }

    @Test
    void pesquisaNormalizaFiltrosEPaginaComLimiteDeCemResultados() {
        PageRequest paginaComTamanhoExcessivo = PageRequest.of(0, 200);
        when(produtoRepository.pesquisar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        var resposta = produtoService.pesquisar(" Milho ", null, " Nampula ", " Mecubúri ",
                null, null, null, null, paginaComTamanhoExcessivo);

        assertEquals(100, resposta.tamanho());
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(produtoRepository).pesquisar(
                org.mockito.ArgumentMatchers.eq("Milho"), isNull(),
                org.mockito.ArgumentMatchers.eq("Nampula"), org.mockito.ArgumentMatchers.eq("Mecubúri"),
                isNull(), isNull(), isNull(), isNull(), org.mockito.ArgumentMatchers.eq(true), pageable.capture());
        assertEquals(100, pageable.getValue().getPageSize());
    }

    @Test
    void intervaloDePrecosInvalidoERejeitadoAntesDaPesquisa() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> produtoService.pesquisar(null, null, null, null,
                        new BigDecimal("30"), new BigDecimal("10"), null, null, PageRequest.of(0, 20)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(produtoRepository, never()).pesquisar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    private ProdutoRequest pedidoProduto() {
        return new ProdutoRequest("Milho", "Milho seco", 4L, new BigDecimal("25.50"), UnidadeMedida.KG,
                new BigDecimal("100"), "Nampula", "Mecubúri", "Centro");
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

    private Categoria categoria(Long id, EstadoCategoria estado) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome("Cereais");
        categoria.setEstado(estado);
        return categoria;
    }

    private Produto produto(Long id, EstadoProduto estado, String quantidade) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome("Milho");
        produto.setCategoria(categoria(4L, EstadoCategoria.ATIVA));
        produto.setProdutor(utilizador(2L, "produtor@example.com", TipoUtilizador.PRODUTOR));
        produto.setPrecoUnidade(new BigDecimal("25.50"));
        produto.setQuantidadeDisponivel(new BigDecimal(quantidade));
        produto.setEstado(estado);
        return produto;
    }
}
