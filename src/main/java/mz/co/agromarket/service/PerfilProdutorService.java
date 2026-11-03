package mz.co.agromarket.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mz.co.agromarket.dto.request.PerfilProdutorRequest;
import mz.co.agromarket.dto.response.PerfilProdutorResponse;
import mz.co.agromarket.model.entity.PerfilProdutor;
import mz.co.agromarket.model.entity.Utilizador;
import mz.co.agromarket.model.enums.TipoUtilizador;
import mz.co.agromarket.repository.PerfilProdutorRepository;
import mz.co.agromarket.repository.UtilizadorRepository;

@Service
public class PerfilProdutorService {

    private final PerfilProdutorRepository perfilRepository;
    private final UtilizadorRepository utilizadorRepository;

    public PerfilProdutorService(PerfilProdutorRepository perfilRepository, UtilizadorRepository utilizadorRepository) {
        this.perfilRepository = perfilRepository;
        this.utilizadorRepository = utilizadorRepository;
    }

    @Transactional(readOnly = true)
    public PerfilProdutorResponse obter(String email) {
        Utilizador produtor = obterProdutor(email);
        return perfilRepository.findByUtilizadorId(produtor.getId())
                .map(this::paraResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil do produtor ainda não foi criado."));
    }

    @Transactional
    public PerfilProdutorResponse guardar(String email, PerfilProdutorRequest request) {
        Utilizador produtor = obterProdutor(email);
        PerfilProdutor perfil = perfilRepository.findByUtilizadorId(produtor.getId()).orElseGet(PerfilProdutor::new);
        perfil.setUtilizador(produtor);
        perfil.setNomeExploracao(request.nomeExploracao().trim());
        perfil.setDescricao(request.descricao());
        perfil.setProvincia(request.provincia().trim());
        perfil.setDistrito(request.distrito().trim());
        perfil.setLocalidade(request.localidade());
        perfil.setEndereco(request.endereco());
        perfil.setTipoProducao(request.tipoProducao());
        perfil.setContactoAlternativo(request.contactoAlternativo());
        return paraResponse(perfilRepository.save(perfil));
    }

    private Utilizador obterProdutor(String email) {
        Utilizador utilizador = utilizadorRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizador não encontrado."));
        if (utilizador.getTipo() != TipoUtilizador.PRODUTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas produtores podem gerir este perfil.");
        }
        return utilizador;
    }

    private PerfilProdutorResponse paraResponse(PerfilProdutor perfil) {
        return new PerfilProdutorResponse(
                perfil.getId(), perfil.getUtilizador().getId(), perfil.getNomeExploracao(), perfil.getDescricao(),
                perfil.getProvincia(), perfil.getDistrito(), perfil.getLocalidade(), perfil.getEndereco(),
                perfil.getTipoProducao(), perfil.getContactoAlternativo());
    }
}
