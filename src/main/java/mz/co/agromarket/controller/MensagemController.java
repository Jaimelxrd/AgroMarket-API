package mz.co.agromarket.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mz.co.agromarket.dto.request.EnviarMensagemRequest;
import mz.co.agromarket.dto.response.InterlocutorResponse;
import mz.co.agromarket.dto.response.MensagemResponse;
import mz.co.agromarket.dto.response.PaginaResponse;
import mz.co.agromarket.service.MensagemService;

@RestController
@Validated
@RequestMapping("/api/mensagens")
public class MensagemController {

    private final MensagemService mensagemService;

    public MensagemController(MensagemService mensagemService) {
        this.mensagemService = mensagemService;
    }

    @PostMapping
    public ResponseEntity<MensagemResponse> enviar(
            Authentication authentication,
            @Valid @RequestBody EnviarMensagemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mensagemService.enviar(authentication.getName(), request));
    }

    @GetMapping("/conversas")
    public List<InterlocutorResponse> conversas(Authentication authentication) {
        return mensagemService.conversas(authentication.getName());
    }

    @GetMapping("/conversas/{interlocutorId}")
    public PaginaResponse<MensagemResponse> mensagens(
            Authentication authentication,
            @PathVariable Long interlocutorId,
            @PageableDefault(size = 30, sort = "dataEnvio", direction = Sort.Direction.DESC) Pageable pageable) {
        return mensagemService.mensagens(authentication.getName(), interlocutorId, pageable);
    }

    @PatchMapping("/{mensagemId}/lida")
    public ResponseEntity<Void> marcarComoLida(Authentication authentication, @PathVariable Long mensagemId) {
        mensagemService.marcarComoLida(authentication.getName(), mensagemId);
        return ResponseEntity.noContent().build();
    }
}
