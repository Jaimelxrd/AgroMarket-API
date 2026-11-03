package mz.co.agromarket.exception;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;
import mz.co.agromarket.dto.response.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> tratarStatusHttp(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String mensagem = exception.getReason() == null ? status.getReasonPhrase() : exception.getReason();
        return resposta(status, mensagem, List.of(mensagem));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiErrorResponse> tratarValidacaoCampos(Exception exception) {
        List<String> erros;
        if (exception instanceof MethodArgumentNotValidException invalid) {
            erros = invalid.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage()).distinct().toList();
        } else {
            erros = ((BindException) exception).getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage()).distinct().toList();
        }
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos.", erros);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> tratarValidacaoMetodo(HandlerMethodValidationException exception) {
        List<String> erros = exception.getAllErrors().stream()
                .map(error -> error.getDefaultMessage() == null ? "Parâmetro inválido." : error.getDefaultMessage())
                .distinct().toList();
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos.", erros);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> tratarViolacao(ConstraintViolationException exception) {
        List<String> erros = exception.getConstraintViolations().stream()
                .map(violacao -> violacao.getPropertyPath() + ": " + violacao.getMessage()).toList();
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos.", erros);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiErrorResponse> tratarPedidoMalformado(Exception exception) {
        return resposta(HttpStatus.BAD_REQUEST, "Pedido inválido.", List.of("Verifique o formato e os parâmetros enviados."));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> tratarUploadGrande(MaxUploadSizeExceededException exception) {
        return resposta(HttpStatus.PAYLOAD_TOO_LARGE, "Ficheiro demasiado grande.", List.of("O limite de tamanho do pedido foi excedido."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> tratarConflitoDados(DataIntegrityViolationException exception) {
        return resposta(HttpStatus.CONFLICT, "Conflito com os dados existentes.", List.of("Verifique valores únicos e relações obrigatórias."));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> tratarAutenticacao(AuthenticationException exception) {
        return resposta(HttpStatus.UNAUTHORIZED, "Não autenticado.", List.of("Credenciais ou token inválidos."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> tratarAcessoNegado(AccessDeniedException exception) {
        return resposta(HttpStatus.FORBIDDEN, "Acesso negado.", List.of("Não tem permissão para esta operação."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> tratarErroInterno(Exception exception) {
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor.", List.of("Tente novamente mais tarde."));
    }

    private ResponseEntity<ApiErrorResponse> resposta(HttpStatus status, String mensagem, List<String> erros) {
        return ResponseEntity.status(status).body(ApiErrorResponse.erro(mensagem, erros));
    }
}
