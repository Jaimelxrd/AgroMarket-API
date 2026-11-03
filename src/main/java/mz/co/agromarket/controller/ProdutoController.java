package mz.co.agromarket.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import mz.co.agromarket.dto.request.ProdutoRequest;
import mz.co.agromarket.dto.response.PaginaResponse;
import mz.co.agromarket.dto.response.ProdutoResponse;
import mz.co.agromarket.service.ProdutoService;

@RestController
@Validated
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public PaginaResponse<ProdutoResponse> pesquisar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String provincia,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) BigDecimal precoMinimo,
            @RequestParam(required = false) BigDecimal precoMaximo,
            @RequestParam(required = false) Long produtorId,
            @RequestParam(required = false) Boolean disponivel,
            @PageableDefault(size = 20, sort = "dataPublicacao", direction = Sort.Direction.DESC) Pageable pageable) {
        return produtoService.pesquisar(nome, categoriaId, provincia, distrito, precoMinimo, precoMaximo,
                produtorId, disponivel, pageable);
    }

    @GetMapping("/meus")
    @PreAuthorize("hasRole('PRODUTOR')")
    public PaginaResponse<ProdutoResponse> meusProdutos(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "dataPublicacao", direction = Sort.Direction.DESC) Pageable pageable) {
        return produtoService.listarDoProdutor(authentication.getName(), pageable);
    }

    @GetMapping("/{id}")
    public ProdutoResponse obter(@PathVariable Long id) {
        return produtoService.obterDisponivel(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRODUTOR')")
    public ResponseEntity<ProdutoResponse> criar(
            Authentication authentication,
            @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(produtoService.criar(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRODUTOR')")
    public ProdutoResponse atualizar(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ProdutoRequest request) {
        return produtoService.atualizar(authentication.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRODUTOR')")
    public ResponseEntity<Void> eliminar(Authentication authentication, @PathVariable Long id) {
        produtoService.eliminar(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/fotografias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PRODUTOR')")
    public ResponseEntity<List<String>> adicionarFotografias(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam("ficheiros") List<MultipartFile> ficheiros) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(produtoService.adicionarFotografias(authentication.getName(), id, ficheiros));
    }

    @DeleteMapping("/{id}/fotografias/{fotografiaId}")
    @PreAuthorize("hasRole('PRODUTOR')")
    public ResponseEntity<Void> eliminarFotografia(
            Authentication authentication,
            @PathVariable Long id,
            @PathVariable Long fotografiaId) {
        produtoService.eliminarFotografia(authentication.getName(), id, fotografiaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/fotografias/{fotografiaId}")
    public ResponseEntity<Resource> obterFotografia(
            Authentication authentication,
            @PathVariable Long id,
            @PathVariable Long fotografiaId) {
        Resource resource = produtoService.obterFotografia(id, fotografiaId, authentication);
        MediaType mediaType = MediaType.parseMediaType(produtoService.tipoConteudoFotografia(id, fotografiaId));
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"fotografia\"")
                .body(resource);
    }
}
