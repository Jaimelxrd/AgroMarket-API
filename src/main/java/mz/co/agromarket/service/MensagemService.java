package mz.co.agromarket.service;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mz.co.agromarket.dto.request.EnviarMensagemRequest;
import mz.co.agromarket.dto.response.InterlocutorResponse;
import mz.co.agromarket.dto.response.MensagemResponse;
import mz.co.agromarket.dto.response.PaginaResponse;
import mz.co.agromarket.model.entity.Mensagem;
import mz.co.agromarket.model.entity.Utilizador;
import mz.co.agromarket.model.enums.EstadoConta;
import mz.co.agromarket.repository.MensagemRepository;
import mz.co.agromarket.repository.UtilizadorRepository;

@Service
public class MensagemService {

    private final MensagemRepository mensagemRepository;
    private final UtilizadorRepository utilizadorRepository;

    public MensagemService(MensagemRepository mensagemRepository, UtilizadorRepository utilizadorRepository) {
        this.mensagemRepository = mensagemRepository;
        this.utilizadorRepository = utilizadorRepository;
    }

    @Transactional
    public MensagemResponse enviar(String email, EnviarMensagemRequest request) {
        Utilizador remetente = obterAtivoPorEmail(email);
        Utilizador destinatario = utilizadorRepository.findById(request.destinatarioId())
                .filter(u -> u.getEstado() == EstadoConta.ATIVA)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destinatário não encontrado."));
        if (remetente.getId().equals(destinatario.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não pode enviar uma mensagem para si próprio.");
        }

        Mensagem mensagem = new Mensagem();
        mensagem.setRemetente(remetente);
        mensagem.setDestinatario(destinatario);
        mensagem.setConteudo(request.conteudo().trim());
        mensagem.setLida(false);
        return paraResponse(mensagemRepository.save(mensagem));
    }

    @Transactional(readOnly = true)
    public List<InterlocutorResponse> conversas(String email) {
        Utilizador utilizador = obterAtivoPorEmail(email);
        return mensagemRepository.encontrarInterlocutores(utilizador.getId()).stream()
                .map(outro -> new InterlocutorResponse(outro.getId(), outro.getNomeCompleto(), outro.getTipo()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginaResponse<MensagemResponse> mensagens(String email, Long interlocutorId, Pageable pageable) {
        Utilizador utilizador = obterAtivoPorEmail(email);
        Utilizador interlocutor = utilizadorRepository.findById(interlocutorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada."));
        if (utilizador.getId().equals(interlocutor.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador de conversa inválido.");
        }
        if (!mensagemRepository.existeConversa(utilizador.getId(), interlocutor.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada.");
        }

        Page<Mensagem> pagina = mensagemRepository.encontrarConversa(
                utilizador.getId(), interlocutor.getId(), limitarPagina(pageable));
        return new PaginaResponse<>(pagina.getContent().stream().map(this::paraResponse).toList(),
                pagina.getNumber(), pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages(), pagina.isLast());
    }

    @Transactional
    public void marcarComoLida(String email, Long mensagemId) {
        Utilizador destinatario = obterAtivoPorEmail(email);
        Mensagem mensagem = mensagemRepository.findByIdAndDestinatarioId(mensagemId, destinatario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada."));
        mensagem.setLida(true);
    }

    private Utilizador obterAtivoPorEmail(String email) {
        Utilizador utilizador = utilizadorRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizador não encontrado."));
        if (utilizador.getEstado() != EstadoConta.ATIVA) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A conta está desativada.");
        }
        return utilizador;
    }

    private Pageable limitarPagina(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), pageable.getSort());
    }

    private MensagemResponse paraResponse(Mensagem mensagem) {
        return new MensagemResponse(mensagem.getId(), mensagem.getRemetente().getId(), mensagem.getDestinatario().getId(),
                mensagem.getConteudo(), mensagem.getDataEnvio(), mensagem.isLida());
    }
}
