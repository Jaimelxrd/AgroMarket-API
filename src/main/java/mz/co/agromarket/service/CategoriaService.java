package mz.co.agromarket.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mz.co.agromarket.dto.request.CategoriaRequest;
import mz.co.agromarket.dto.response.CategoriaResponse;
import mz.co.agromarket.model.entity.Categoria;
import mz.co.agromarket.model.enums.EstadoCategoria;
import mz.co.agromarket.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarAtivas() {
        return categoriaRepository.findAllByEstadoOrderByNomeAsc(EstadoCategoria.ATIVA)
                .stream().map(this::paraResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.findAll().stream().map(this::paraResponse).toList();
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        String nome = request.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma categoria com esse nome.");
        }
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setDescricao(normalizarDescricao(request.descricao()));
        categoria.setEstado(EstadoCategoria.ATIVA);
        return paraResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = obter(id);
        String nome = request.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma categoria com esse nome.");
        }
        categoria.setNome(nome);
        categoria.setDescricao(normalizarDescricao(request.descricao()));
        return paraResponse(categoria);
    }

    @Transactional
    public void desativar(Long id) {
        Categoria categoria = obter(id);
        categoria.setEstado(EstadoCategoria.INATIVA);
    }

    private Categoria obter(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada."));
    }

    private String normalizarDescricao(String descricao) {
        return descricao == null || descricao.isBlank() ? null : descricao.trim();
    }

    private CategoriaResponse paraResponse(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getDescricao(), categoria.getEstado());
    }
}
