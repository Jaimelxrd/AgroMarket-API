package mz.co.agromarket.service;

import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mz.co.agromarket.dto.response.PaginaResponse;
import mz.co.agromarket.dto.response.UtilizadorResponse;
import mz.co.agromarket.model.entity.Utilizador;
import mz.co.agromarket.model.enums.EstadoConta;
import mz.co.agromarket.model.enums.TipoUtilizador;
import mz.co.agromarket.repository.UtilizadorRepository;

@Service
public class AdminUtilizadorService {

    private final UtilizadorRepository utilizadorRepository;

    public AdminUtilizadorService(UtilizadorRepository utilizadorRepository) {
        this.utilizadorRepository = utilizadorRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<UtilizadorResponse> listar(TipoUtilizador tipo, Pageable pageable) {
        Pageable pagina = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), pageable.getSort());
        Page<Utilizador> utilizadores = tipo == null
                ? utilizadorRepository.findAll(pagina)
                : utilizadorRepository.findAllByTipo(tipo, pagina);
        return new PaginaResponse<>(utilizadores.getContent().stream().map(this::paraResponse).toList(),
                utilizadores.getNumber(), utilizadores.getSize(), utilizadores.getTotalElements(),
                utilizadores.getTotalPages(), utilizadores.isLast());
    }

    @Transactional
    public UtilizadorResponse ativar(Long id) {
        Utilizador utilizador = obter(id);
        utilizador.setEstado(EstadoConta.ATIVA);
        return paraResponse(utilizador);
    }

    @Transactional
    public void desativar(Long id, String emailAdmin) {
        Utilizador admin = utilizadorRepository.findByEmailIgnoreCase(emailAdmin.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado."));
        if (admin.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não pode desativar a própria conta de administrador.");
        }
        obter(id).setEstado(EstadoConta.INATIVA);
    }

    private Utilizador obter(Long id) {
        return utilizadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizador não encontrado."));
    }

    private UtilizadorResponse paraResponse(Utilizador utilizador) {
        return new UtilizadorResponse(utilizador.getId(), utilizador.getNomeCompleto(), utilizador.getEmail(),
                utilizador.getNumeroTelefone(), utilizador.getTipo(), utilizador.getEstado(), utilizador.getDataRegisto());
    }
}
